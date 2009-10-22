/*
 * Copyright (c) 2009. The Codehaus. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.codehaus.httpcache4j.storage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.cache.CacheItem;
import org.codehaus.httpcache4j.cache.CacheStorage;
import org.codehaus.httpcache4j.cache.Key;
import org.codehaus.httpcache4j.cache.Vary;
import org.codehaus.httpcache4j.payload.DelegatingInputStream;
import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.*;
import java.util.*;

/**
 * We have one table, Response.
 * The tables are created on startup if they do not exist.
 * <p/>
 * NOTE:
 * This is experimental and should not be used in production.
 * There is generally no way of throwing stuff out of cache at the moment.
 * Stuff will be thrown out on "insert" and "clear".
 * This storage also requires Java 6 or higher.
 *
 * @author <a href="mailto:erlend@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DerbyCacheStorage implements CacheStorage {
    private final static String[] TABLES = {"response"};
    private final SimpleJdbcTemplate jdbcTemplate;
    private final TransactionTemplate transaction;
    private final ResponseMapper responseMapper;

    public DerbyCacheStorage(File storageDirectory) {
        this(storageDirectory, false);
    }

    public DerbyCacheStorage(File storageDirectory, boolean dropTables) {
        File database = new File(storageDirectory, "database");
        DataSource ds = createDataSource(database);
        jdbcTemplate = new SimpleJdbcTemplate(ds);
        maybeCreateTables(dropTables);
        responseMapper = new ResponseMapper();
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        transaction = new TransactionTemplate(new DataSourceTransactionManager(ds), definition);
    }

    private static DataSource createDataSource(File database) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        ds.setUrl(String.format("jdbc:derby:%s;create=true", database.getAbsolutePath()));
        ds.setUsername("");
        ds.setPassword("");
        return ds;
    }

    private void maybeCreateTables(boolean dropTables) {
        boolean createTables = false;
        try {
            jdbcTemplate.queryForInt("select count(*) from response");
        } catch (DataAccessException e) {
            createTables = true;
        }
        if (dropTables && !createTables) {
            //TODO: Logging....
            System.err.println("--- dropping tables:");
            List<String> tables = new ArrayList<String>(Arrays.asList(TABLES));
            Collections.reverse(tables);
            for (String table : tables) {
                System.err.print("Dropping table " + table);
                jdbcTemplate.update("drop table " + table);
                System.err.println("ok!");
            }
            createTables = true;
        }
        if (createTables) {
            System.err.println("--- creating " + TABLES.length + " tables:");

            for (String table : TABLES) {
                System.err.print("--- creating table " + table + "...");
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ddl/" + table + ".ddl");
                if (inputStream == null) {
                    System.err.println("Could not find DDL file for table " + table + "!");
                    return;
                }

                try {
                    String sql = IOUtils.toString(inputStream);
                    jdbcTemplate.update(sql);
                } catch (IOException e) {
                    System.err.println("Failed!");
                    e.printStackTrace();
                    return;
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }

                System.err.println("ok");
            }
        }

    }

    protected HTTPResponse rewriteResponse(HTTPResponse response) {
        if (response.hasPayload()) {
            Headers headers = response.getHeaders();
            Status status = response.getStatus();
            Payload payload = response.getPayload();
            try {
                File file = writeStreamToTempFile(payload.getInputStream());
                return new HTTPResponse(new DerbyFilePayload(file, payload.getMimeType()), status, headers);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return response;
    }

    public HTTPResponse insert(final HTTPRequest request, final HTTPResponse response) {
        final Key key = Key.create(request, response);
        transaction.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                invalidate(key);
                JdbcOperations jdbcOperations = jdbcTemplate.getJdbcOperations();
                jdbcOperations.execute("insert into response(uri, vary, status, headers, payload, mimeType, cachetime) values (?, ?, ?, ?, ?, ?, ?)", new PreparedStatementCallback() {
                    public Object doInPreparedStatement(PreparedStatement preparedStatement) throws SQLException, DataAccessException {
                        preparedStatement.setString(1, key.getURI().toString());
                        preparedStatement.setString(2, key.getVary().toString());
                        preparedStatement.setInt(3, response.getStatus().getCode());
                        preparedStatement.setString(4, response.getHeaders().toString());
                        InputStream inputStream = null;
                        if (response.hasPayload() && response.getPayload().isAvailable()) {
                            preparedStatement.setString(6, response.getPayload().getMimeType().toString());
                            inputStream = response.getPayload().getInputStream();
                            preparedStatement.setBinaryStream(5, inputStream);
                        }
                        else {
                            preparedStatement.setNull(5, Types.BLOB);
                            preparedStatement.setNull(6, Types.VARCHAR);
                        }
                        preparedStatement.setTimestamp(7, new Timestamp(DateTimeUtils.currentTimeMillis()));

                        try {
                            return preparedStatement.executeUpdate();
                        } catch (SQLException e) {
                            e.printStackTrace();
                            // throw e;
                        }
                        finally {
                            IOUtils.closeQuietly(inputStream);
                        }
                        return null;
                    }
                });
            }
        });
        return getImpl(key);
    }

    private static File writeStreamToTempFile(InputStream stream) throws IOException {
        FileOutputStream out = null;
        File tempFile = null;
        try {
            tempFile = File.createTempFile("foo", "bar");
            out = FileUtils.openOutputStream(tempFile);
            IOUtils.copy(stream, out);
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(stream);
        }
        return tempFile;
    }

    @Override
    public HTTPResponse update(final HTTPRequest request, final HTTPResponse response) {
        final Key key = Key.create(request, response);
        transaction.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                jdbcTemplate.update("update response set headers = ?, cachetime = ? where uri = ? and vary = ?",
                                    response.getHeaders().toString(),
                                    new Timestamp(DateTimeUtils.currentTimeMillis()),
                                    key.getURI().toString(),
                                    key.getVary().toString()
                );
            }
        });
        return getImpl(key);
    }

    private HTTPResponse getImpl(final Key key) {
        try {
            CacheItemHolder holder = jdbcTemplate.queryForObject(
                    "select * from response where uri = ? and vary = ?",
                    responseMapper,
                    key.getURI().toString(),
                    key.getVary().toString()
            );
            return rewriteResponse(holder.getCacheItem().getResponse());
        } catch (DataAccessException e) {
            return null;
        }
    }

    protected void invalidate(final Key key) {
        jdbcTemplate.update("delete from response where uri = ? and vary = ?", key.getURI().toString(), key.getVary().toString());
    }


    @Override
    public void invalidate(final URI uri) {
        transaction.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                jdbcTemplate.update("delete from response where uri = ?", uri.toString());
            }
        });
    }

    @Override
    public CacheItem get(final HTTPRequest request) {
        List<CacheItemHolder> list = jdbcTemplate.query("select * from response where uri = ?", responseMapper, request.getRequestURI().toString());
        for (CacheItemHolder holder : list) {
            if (holder.getVary().matches(request)) {
                return holder.getCacheItem();
            }
        }
        return null;
    }

    @Override
    public void clear() {
        transaction.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                jdbcTemplate.update("delete from response");

            }
        });
    }

    @Override
    public int size() {
        return jdbcTemplate.queryForInt("select count(*) from response");
    }

    @Override
    public Iterator<Key> iterator() {
        List<Key> keyList = jdbcTemplate.query("select uri,vary from response", new ParameterizedRowMapper<Key>() {
            public Key mapRow(ResultSet rs, int row) throws SQLException {
                return Key.create(URI.create(rs.getString("uri")), convertToVary(rs.getString("vary")));
            }
        });
        return keyList.iterator();
    }

    private static Vary convertToVary(String vary) {
        Map<String, String> map = new LinkedHashMap<String, String>();
        String[] lines = vary.split("\r\n");
        if (lines != null && lines.length > 0) {
            for (String line : lines) {
                String[] varyParts = line.split(":");
                if (varyParts != null && varyParts.length == 2) {
                    map.put(varyParts[0], varyParts[1]);
                }
            }
        }
        return new Vary(map);
    }

    private static class ResponseMapper implements ParameterizedRowMapper<CacheItemHolder> {

        private Headers convertToHeaders(String input) {
            Headers headers = new Headers();
            String[] lines = input.split("\r\n");
            if (lines != null && lines.length > 0) {
                for (String line : lines) {
                    String[] headerparts = line.split(":");
                    if (headerparts != null && headerparts.length == 2) {
                        headers.add(headerparts[0], headerparts[1]);
                    }
                }
            }
            return headers;
        }

        public CacheItemHolder mapRow(ResultSet rs, int row) throws SQLException {
            URI uri = URI.create(rs.getString("uri"));
            Vary vary = convertToVary(rs.getString("vary"));
            Blob stream = rs.getBlob("payload");

            Payload payload = null;
            if (stream != null && stream.length() > 0) {
                try {
                    payload = new DerbyFilePayload(writeStreamToTempFile(stream.getBinaryStream()), MIMEType.valueOf(rs.getString("mimetype")));
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
            Status status = Status.valueOf(rs.getInt("status"));
            Headers headers = convertToHeaders(rs.getString("headers"));
            DateTime cacheTime = new DateTime(rs.getTimestamp("cachetime").getTime());
            return new CacheItemHolder(uri, vary, new CacheItem(new HTTPResponse(payload, status, headers), cacheTime));
        }
    }

    private static class CacheItemHolder {
        private URI uri;
        private Vary vary;
        private CacheItem cacheItem;

        private CacheItemHolder(URI uri, Vary vary, CacheItem response) {
            this.uri = uri;
            this.vary = vary;
            this.cacheItem = response;
        }

        public URI getUri() {
            return uri;
        }

        public Vary getVary() {
            return vary;
        }

        public CacheItem getCacheItem() {
            return cacheItem;
        }
    }

    private static class DerbyFilePayload extends FilePayload {
        public DerbyFilePayload(File file, final MIMEType mimeType) {
            super(file, mimeType);
        }

        @Override
        public InputStream getInputStream() {
            return new DelegatingInputStream(super.getInputStream()) {
                @Override
                public void close() throws IOException {
                    super.close();
                    getFile().delete();
                }
            };
        }
    }
}
