/*
 * Copyright (c) 2010. The Codehaus. All Rights Reserved.
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

package org.codehaus.httpcache4j.storage.jdbc;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.cache.*;
import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.codehaus.httpcache4j.storage.jdbc.JdbcUtil.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class JdbcCacheStorage implements CacheStorage {
    private final static String[] TABLES = {"response"};
    private final DataSource datasource;
    private final ResponseMapper mapper = new ResponseMapper();
    private FileManager manager;
    private TransactionExecutor executor = new TransactionExecutor(Connection.TRANSACTION_SERIALIZABLE);

    public JdbcCacheStorage(File dataDirectory, DataSource datasource) {
        this.datasource = datasource;
        manager = new FileManager(dataDirectory);
    }

    @Override
    public synchronized HTTPResponse insert(final HTTPRequest request, final HTTPResponse response) {
        final AtomicReference<Payload> payload = new AtomicReference<Payload>();
        executor.execute(new DoInTransaction() {
            @Override
            public void doWith(Connection connection) throws SQLException {
                Key key = Key.create(request, response);
                String sql = "insert into response(uri, vary, status, headers, mimeType, cachetime) values (?, ?, ?, ?, ?, ?)";
                PreparedStatement statement = null;
                try {
                    invalidate(key, connection);
                    statement = connection.prepareStatement(sql);
                    statement.setString(1, key.getURI().toString());
                    statement.setString(2, key.getVary().toString());
                    statement.setInt(3, response.getStatus().getCode());
                    statement.setString(4, response.getHeaders().toString());
                    if (response.hasPayload() && response.getPayload().isAvailable()) {
                        statement.setString(5, response.getPayload().getMimeType().toString());
                        try {
                            File file = manager.createFile(key, response.getPayload().getInputStream());
                            if (file != null && file.exists()) {
                                payload.set(new FilePayload(file, response.getPayload().getMimeType()));
                            }
                        } catch (IOException e) {
                            throw new SQLException(e);
                        }
                    } else {
                        statement.setNull(5, Types.VARCHAR);
                    }
                    statement.setTimestamp(6, new Timestamp(DateTimeUtils.currentTimeMillis()));
                    statement.executeUpdate();
                    connection.commit();
                } finally {
                    JdbcUtil.close(statement);
                }
            }
        });
        return new HTTPResponse(payload.get(), response.getStatus(), response.getHeaders());
    }

    @Override
    public synchronized HTTPResponse update(final HTTPRequest request, final HTTPResponse response) {
        final Key key = Key.create(request, response);

        executor.execute(new DoInTransaction() {
            @Override
            public void doWith(Connection connection) throws SQLException {
                PreparedStatement statement = null;
                try {
                    statement = connection.prepareStatement("update response set headers = ?, cachetime = ? where uri = ? and vary = ?");
                    statement.setString(1, response.getHeaders().toString());
                    statement.setTimestamp(2, new Timestamp(DateTimeUtils.currentTimeMillis()));
                    statement.setString(3, key.getURI().toString());
                    statement.setString(4, key.getVary().toString());
                    statement.executeUpdate();

                } finally {
                    JdbcUtil.close(statement);
                }
            }
        });

        return getImpl(key);
    }

    private synchronized HTTPResponse getImpl(final Key key) {
        Connection connection = getConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement("select * from response where uri = ? and vary = ?");
            statement.setString(1, key.getURI().toString());
            statement.setString(2, key.getVary().toString());
            rs = statement.executeQuery();
            if (rs.next()) {
                CacheItemHolder holder = mapper.mapRow(rs);
                return holder.getCacheItem().getResponse();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            JdbcUtil.close(rs);
            JdbcUtil.close(statement);
            JdbcUtil.close(connection);
        }
        return null;
    }


    @Override
    public synchronized CacheItem get(HTTPRequest request) {
        Connection connection = getConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement("select * from response where uri = ?");
            statement.setString(1, request.getNormalizedURI().toString());
            rs = statement.executeQuery();
            while (rs.next()) {
                CacheItemHolder holder = mapper.mapRow(rs);
                if (holder.getVary().matches(request)) {
                    return holder.getCacheItem();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            JdbcUtil.close(rs);
            JdbcUtil.close(statement);
            JdbcUtil.close(connection);
        }
        return null;
    }

    @Override
    public synchronized CacheItem get(Key key) {
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("select * from response where uri = ? and vary = ?");
            statement.setString(1, key.getURI().toString());
            statement.setString(2, key.getVary().toString());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                CacheItemHolder holder = mapper.mapRow(rs);
                return holder.getCacheItem();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            JdbcUtil.close(statement);
        }
        return null;
    }


    //This is part of a transaction.
    private synchronized void invalidate(final Key key, final Connection connection) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("delete from response where uri = ? and vary = ?");
            statement.setString(1, key.getURI().toString());
            statement.setString(2, key.getVary().toString());
            manager.remove(key);
            statement.executeUpdate();
        } finally {
            JdbcUtil.close(statement);
        }
    }


    @Override
    public synchronized void invalidate(final URI uri) {
        Connection connection = getConnection();
        executor.execute(connection, new DoInTransaction() {
            @Override
            public void doWith(Connection connection) throws SQLException {
                PreparedStatement statement = null;
                try {
                    statement = connection.prepareStatement("delete from response where uri = ?");
                    statement.setString(1, uri.toString());
                    statement.executeUpdate();
                    connection.commit();
                    manager.clear(uri);
                } finally {
                    JdbcUtil.close(statement);
                }
            }
        });
    }

    @Override
    public synchronized void clear() {
        executor.execute(new DoInTransaction() {
            @Override
            public void doWith(Connection connection) throws SQLException {
                PreparedStatement statement = null;
                try {
                    JdbcUtil.startTransaction(connection);
                    statement = connection.prepareStatement("delete from response");
                    statement.executeUpdate();
                    manager.clear();
                    connection.commit();
                } finally {
                    JdbcUtil.close(statement);
                }
            }
        });

    }

    @Override
    public int size() {
        Connection connection = getConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement("select count(*) from response");
            rs = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to query for count", e);
        } finally {
            JdbcUtil.close(rs);
            JdbcUtil.close(statement);
            JdbcUtil.close(connection);
        }
        return 0;
    }

    @Override
    public Iterator<Key> iterator() {
        Connection connection = getConnection();
        PreparedStatement statement = null;
        ResultSet rs = null;
        List<Key> keys = new ArrayList<Key>();
        try {
            statement = connection.prepareStatement("select uri,vary from response");
            rs = statement.executeQuery();
            while (rs.next()) {
                String uri = rs.getString(1);
                String vary = rs.getString(2);
                keys.add(Key.create(URI.create(uri), mapper.convertToVary(vary)));
            }
        } catch (SQLException ignore) {
        } finally {
            JdbcUtil.close(rs);
            JdbcUtil.close(statement);
            JdbcUtil.close(connection);
        }
        return ImmutableList.copyOf(keys).iterator();
    }

    protected Connection getConnection() {
        try {
            return datasource.getConnection();
        } catch (SQLException e) {
            throw new DataAccessException("Unable to get new connection", e);
        }
    }

    @Override
    public void shutdown() {
        try {
            getConnection().close();
        } catch (SQLException ignore) {
        }
    }

    protected void maybeCreateTables(final boolean dropTables) {
        final boolean createTables = shouldWeCreateTables();
        Connection connection = getConnection();

        executor.execute(connection, new DoInTransaction() {
            @Override
            public void doWith(Connection connection) throws SQLException {
                boolean tablesCreate = createTables;
                if (dropTables && !tablesCreate) {
                    //TODO: Logging....
                    System.err.println("--- dropping tables:");
                    List<String> tables = new ArrayList<String>(Arrays.asList(TABLES));
                    Collections.reverse(tables);
                    for (String table : tables) {
                        System.err.print("Dropping table " + table);
                        dropTable(table, connection);
                        System.err.println("ok!");
                    }
                    tablesCreate = true;
                }
                if (tablesCreate) {
                    System.err.println("--- creating " + TABLES.length + " tables:");

                    for (String table : TABLES) {
                        System.err.print("--- creating table " + table + "...");
                        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("ddl/" + table + ".ddl");
                        if (inputStream == null) {
                            System.err.println("Could not find DDL file for table " + table + "!");
                            return;
                        }
                        try {
                            String sql = CharStreams.toString(new InputStreamReader(inputStream));
                            createTable(sql, connection);
                        } catch (IOException e) {
                            System.err.println("Failed!");
                            e.printStackTrace();
                            return;
                        } finally {
                            Closeables.closeQuietly(inputStream);
                        }

                        System.err.println("ok");
                    }
                }
            }
        });
    }

    private boolean shouldWeCreateTables() {
        boolean createTables = false;
        try {
            size();
        } catch (DataAccessException e) {
            createTables = true;
        }
        return createTables;
    }

    private void dropTable(String table, Connection connection) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("drop table " + table);
            statement.executeUpdate();
        } finally {
            close(statement);
        }
    }

    private void createTable(String sql, Connection connection) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        } finally {
            close(statement);
        }
    }

    private class ResponseMapper {
        private Vary convertToVary(String vary) {
            return Vary.parse(vary);
        }

        private Headers convertToHeaders(String input) {
            return Headers.parse(input);
        }

        public CacheItemHolder mapRow(ResultSet rs) throws SQLException {
            URI uri = URI.create(rs.getString("uri"));
            Vary vary = convertToVary(rs.getString("vary"));
            Key key = Key.create(uri, vary);
            File file = manager.resolve(key);
            Payload payload = null;
            if (file.exists()) {
                payload = new FilePayload(file, MIMEType.valueOf(rs.getString("mimetype")));
            }
            Status status = Status.valueOf(rs.getInt("status"));
            Headers headers = convertToHeaders(rs.getString("headers"));
            DateTime cacheTime = new DateTime(rs.getTimestamp("cachetime").getTime());
            HTTPResponse response = new HTTPResponse(payload, status, headers);
            return new CacheItemHolder(uri, vary, new DefaultCacheItem(response, cacheTime));
        }
    }

    private class TransactionExecutor {

        private int isolationLevel;

        public TransactionExecutor(int defaultIsolationLevel) {
            this.isolationLevel = defaultIsolationLevel;
        }

        void execute(DoInTransaction callback) {
            execute(getConnection(), callback);
        }

        void execute(Connection connection, DoInTransaction callback) {
            int isolationLevel = -1;
            try {
                isolationLevel = connection.getTransactionIsolation();
                JdbcUtil.startTransaction(connection);
                connection.setTransactionIsolation(this.isolationLevel);

                callback.doWith(connection);

                connection.commit();
            } catch (SQLException e) {
                JdbcUtil.rollback(connection);
                throw new DataAccessException(e);
            } finally {
                JdbcUtil.endTransaction(connection, isolationLevel);
                JdbcUtil.close(connection);
            }
        }
    }

    private static interface DoInTransaction {
        void doWith(Connection connection) throws SQLException;
    }
}
