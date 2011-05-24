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
import org.apache.commons.io.IOUtils;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.cache.CacheItem;
import org.codehaus.httpcache4j.cache.CacheStorage;
import org.codehaus.httpcache4j.cache.Key;
import org.codehaus.httpcache4j.cache.Vary;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.*;
import java.util.*;

import static org.codehaus.httpcache4j.storage.jdbc.JdbcUtil.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class JdbcCacheStorage implements CacheStorage {
    private final static String[] TABLES = {"response"};
    private final DataSource datasource;
    private final ResponseMapper mapper = new ResponseMapper();
    
    public JdbcCacheStorage(DataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    public HTTPResponse insert(HTTPRequest request, HTTPResponse response) {
        Key key = Key.create(request, response);
        Connection connection = getConnection();

        String sql = "insert into response(uri, vary, status, headers, payload, mimeType, cachetime) values (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = null;
        try {
            JdbcUtil.startTransaction(connection);
            invalidate(key, connection);
            statement = connection.prepareStatement(sql);
            statement.setString(1, key.getURI().toString());
            statement.setString(2, key.getVary().toJSON());
            statement.setInt(3, response.getStatus().getCode());
            statement.setString(4, response.getHeaders().toJSON());
            InputStream inputStream = null;
            if (response.hasPayload() && response.getPayload().isAvailable()) {
                statement.setString(6, response.getPayload().getMimeType().toString());
                inputStream = response.getPayload().getInputStream();
                statement.setBinaryStream(5, inputStream);
            }
            else {
                statement.setNull(5, Types.BLOB);
                statement.setNull(6, Types.VARCHAR);
            }
            statement.setTimestamp(7, new Timestamp(DateTimeUtils.currentTimeMillis()));
            try {
                statement.executeUpdate();
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
            connection.commit();
            return getImpl(connection, key);
        } catch (SQLException e) {
            JdbcUtil.rollback(connection);
            JdbcUtil.close(connection);
            throw new DataAccessException(e);
        }
        finally {
            JdbcUtil.endTransaction(connection);
            JdbcUtil.close(statement);
        }
    }

    @Override
    public HTTPResponse update(HTTPRequest request, HTTPResponse response) {
        Key key = Key.create(request, response);        
        Connection connection = getConnection();

        PreparedStatement statement = null;
        try {
            JdbcUtil.startTransaction(connection);
            statement = connection.prepareStatement("update response set headers = ?, cachetime = ? where uri = ? and vary = ?");
            statement.setString(1, response.getHeaders().toJSON());
            statement.setTimestamp(2, new Timestamp(DateTimeUtils.currentTimeMillis()));
            statement.setString(3, key.getURI().toString());
            statement.setString(4, key.getVary().toJSON());
            statement.executeUpdate();
            connection.commit();
            return getImpl(connection, key);
        } catch (SQLException e) {
            JdbcUtil.rollback(connection);
            JdbcUtil.close(connection);
            throw new DataAccessException(e);
        } finally {
            JdbcUtil.endTransaction(connection);
            JdbcUtil.close(statement);
        }

    }

    private HTTPResponse getImpl(Connection connection, final Key key) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("select * from response where uri = ? and vary = ?");
            statement.setString(1, key.getURI().toString());
            statement.setString(2, key.getVary().toJSON());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                CacheItemHolder holder = mapper.mapRow(rs, connection);
                return holder.getCacheItem().getResponse();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        finally {
            JdbcUtil.close(statement);
        }
        return null;
    }


    @Override
    public CacheItem get(HTTPRequest request) {
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("select * from response where uri = ?");
            statement.setString(1, request.getRequestURI().toString());            
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                CacheItemHolder holder = mapper.mapRow(rs, connection);
                if (holder.getVary().matches(request)) {
                    return holder.getCacheItem();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        finally {
            JdbcUtil.close(statement);
        }
        return null;
    }

    private void invalidate(final Key key, final Connection connection) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("delete from response where uri = ? and vary = ?");
            statement.setString(1, key.getURI().toString());
            statement.setString(2, key.getVary().toJSON());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        } finally {
            JdbcUtil.close(statement);
        }
    }


    @Override
    public void invalidate(final URI uri) {
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try {
            JdbcUtil.startTransaction(connection);
            statement = connection.prepareStatement("delete from response where uri = ?");
            statement.setString(1, uri.toString());
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            JdbcUtil.rollback(connection);
            throw new DataAccessException("Unable to invalidate", e);
        } finally {
            JdbcUtil.endTransaction(connection);
            JdbcUtil.close(statement);
            JdbcUtil.close(connection);
        }        
    }

    @Override
    public void clear() {
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try {
            JdbcUtil.startTransaction(connection);
            statement = connection.prepareStatement("delete from response");
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to clear", e);
        } finally {
            JdbcUtil.endTransaction(connection);
            JdbcUtil.close(statement);
            JdbcUtil.close(connection);
        }
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
            while(rs.next()) {
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

    protected HTTPResponse rewriteResponse(HTTPResponse response) {
        return response;
    }

    protected void maybeCreateTables(boolean dropTables) {
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

    private class ResponseMapper {
        private Vary convertToVary(String vary) {
            return Vary.fromJSON(vary);
        }

        private Headers convertToHeaders(String input) {
            return Headers.fromJSON(input);
        }

        public CacheItemHolder mapRow(ResultSet rs, Connection connection) throws SQLException {
            URI uri = URI.create(rs.getString("uri"));
            Vary vary = convertToVary(rs.getString("vary"));
            Blob blob = rs.getBlob("payload");

            Payload payload = null;
            if (blob != null && !rs.wasNull()) {
                payload = new InputStreamPayload(new ResultSetInputStream(rs, connection, blob.getBinaryStream()), MIMEType.valueOf(rs.getString("mimetype")));
            }
            Status status = Status.valueOf(rs.getInt("status"));
            Headers headers = convertToHeaders(rs.getString("headers"));
            DateTime cacheTime = new DateTime(rs.getTimestamp("cachetime").getTime());
            HTTPResponse response = new HTTPResponse(payload, status, headers);
            return new CacheItemHolder(uri, vary, new CacheItem(rewriteResponse(response), cacheTime));
        }
    }
}
