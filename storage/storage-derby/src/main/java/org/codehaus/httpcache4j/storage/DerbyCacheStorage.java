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

import org.apache.derby.jdbc.EmbeddedDataSource40;
import org.codehaus.httpcache4j.storage.jdbc.JdbcCacheStorage;

import javax.sql.DataSource;
import java.io.File;

/**
 * We have one table, Response.
 * The tables are created on startup if they do not exist.
 * <p/>
 * NOTE:
 * There is generally no way of throwing stuff out of cache at the moment.
 * Stuff will be thrown out on "insert" and "clear".
 * This storage also requires Java 6 or higher.
 *
 * @author <a href="mailto:erlend@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DerbyCacheStorage extends JdbcCacheStorage {

    public DerbyCacheStorage(File storageDirectory) {
        this(storageDirectory, false);
    }

    public DerbyCacheStorage(File storageDirectory, boolean dropTables) {
        super(storageDirectory, createDataSource(new File(storageDirectory, "database")));
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
}
