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

import org.apache.commons.io.IOUtils;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
public class H2CacheStorage extends JdbcCacheStorage {
    private final static String[] TABLES = {"response"};

    public H2CacheStorage(File storageDirectory) {
        this(storageDirectory, false);
    }

    public H2CacheStorage(File storageDirectory, boolean dropTables) {
        super(createDataSource(new File(storageDirectory, "database")));

        maybeCreateTables(dropTables);
    }

    private static DataSource createDataSource(File database) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setUser("sa");
        ds.setPassword("");
        ds.setURL("jdbc:h2:" + database);
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
}