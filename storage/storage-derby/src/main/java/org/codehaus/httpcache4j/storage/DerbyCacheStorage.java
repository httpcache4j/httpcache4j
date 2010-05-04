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
import org.apache.derby.impl.jdbc.EmbedCallableStatement40;
import org.apache.derby.jdbc.EmbeddedDataSource40;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.cache.CacheItem;
import org.codehaus.httpcache4j.cache.CacheStorage;
import org.codehaus.httpcache4j.cache.Key;
import org.codehaus.httpcache4j.cache.Vary;
import org.codehaus.httpcache4j.payload.DelegatingInputStream;
import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.storage.jdbc.DataAccessException;
import org.codehaus.httpcache4j.storage.jdbc.JdbcCacheStorage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.*;
import java.util.*;

import static org.codehaus.httpcache4j.storage.jdbc.JdbcUtil.*;

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
public class DerbyCacheStorage extends JdbcCacheStorage {
    private final static String[] TABLES = {"response"};

    public DerbyCacheStorage(File storageDirectory) {
        this(storageDirectory, false);
    }

    /**
     * Creates an In Memory Derby database.
     */
    public DerbyCacheStorage() {
        this(null, false);
    }

    public DerbyCacheStorage(File storageDirectory, boolean dropTables) {
        super(storageDirectory != null ? createDataSource(new File(storageDirectory, "database")) : createMemoryDataSource());
        maybeCreateTables(dropTables);
    }

    private static DataSource createDataSource(File database) {
        EmbeddedDataSource40 ds = new EmbeddedDataSource40();
        ds.setCreateDatabase("create");
        ds.setDatabaseName(database.getAbsolutePath());
        ds.setUser("");
        ds.setPassword("");
        return ds;
    }

    private static DataSource createMemoryDataSource() {        
        EmbeddedDataSource40 ds = new EmbeddedDataSource40();
        ds.setCreateDatabase("create");
        ds.setDatabaseName("memory:httpcache4j");
        ds.setUser("");
        ds.setPassword("");
        return ds;
    }

    private void maybeCreateTables(boolean dropTables) {
        boolean createTables = false;
        try {
            size();
        } catch (DataAccessException e) {
            createTables = true;
        }
        Connection connection = getConnection();
        try {
            startTransaction(connection);
            if (dropTables && !createTables) {
                //TODO: Logging....
                System.err.println("--- dropping tables:");
                List<String> tables = new ArrayList<String>(Arrays.asList(TABLES));
                Collections.reverse(tables);
                for (String table : tables) {
                    System.err.print("Dropping table " + table);
                    dropTable(table, connection);
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
                        createTable(sql, connection);
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
        catch (DataAccessException e) {
            rollback(connection);
        }

        finally {
            endTransaction(connection);
            close(connection);
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

    private void dropTable(String table, Connection connection) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("drop table " + table);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        finally {
            close(statement);
        }
    }

    private void createTable(String sql, Connection connection) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        finally {
            close(statement);
        }
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
