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

package org.codehaus.httpcache4j.auth;

import org.codehaus.httpcache4j.Directives;
import org.codehaus.httpcache4j.Header;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class AuthScheme {
    private final Header header;
    private String type;
    private final Directives directives;

    public AuthScheme(final Header header) {
        this.header = header;
        String headervalue = header.getValue();
        if (headervalue.contains(" ")) {
            final int index = headervalue.indexOf(" ");
            type = headervalue.substring(0, index);
            directives = new Directives(headervalue.substring(index + 1));
        }
        else {
            directives = new Directives();
        }
    }

    public Header getHeader() {
        return header;
    }

    public String getType() {
        return type;
    }

    public String getRealm() {
        return directives.get("realm");
    }

    public Directives getDirectives() {
        return directives;
    }
}
