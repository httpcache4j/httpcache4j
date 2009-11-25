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

import org.codehaus.httpcache4j.HTTPHost;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.net.URI;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class SchemeRegistry {
    private ConcurrentMap<HTTPHost, AuthScheme> registry = new ConcurrentHashMap<HTTPHost, AuthScheme>();

    public void register(HTTPHost host, AuthScheme scheme) {
        registry.put(host, scheme);
    }

    public boolean matches(HTTPHost host) {
        return registry.containsKey(host);
    }

    public void clear() {
        registry.clear();
    }

    public AuthScheme get(HTTPHost host) {
        return registry.get(host);
    }
}
