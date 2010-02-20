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

import org.codehaus.httpcache4j.payload.DelegatingInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;

/**
* @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
* @version $Revision: $
*/
class ResultSetInputStream extends DelegatingInputStream {
    private final ResultSet rs;
    private final Connection connection;

    public ResultSetInputStream(ResultSet rs, Connection connection, InputStream stream) {
        super(stream);
        this.rs = rs;
        this.connection = connection;
    }

    @Override
    public void close() throws IOException {
        super.close();
        JdbcUtil.close(rs);
        JdbcUtil.close(connection);
    }
}
