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

package org.codehaus.httpcache4j;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class HTTPHost {
    private final String scheme;
    private final String host;
    private final int port;

    public HTTPHost(String scheme, String host, int port) {
        this.scheme = scheme;
        this.host = host;
        if (port == -1) {
            if ("http".equals(scheme) || "https".equals(scheme)) {
                this.port = port;
            }
            else {
                throw new IllegalArgumentException("Unknown scheme for host " + scheme);
            }
        }
        else {
            this.port = port;            
        }
    }

    public HTTPHost(String scheme, String host) {
        this(scheme, host, -1);
    }

    public HTTPHost(URI uri) {
        this(uri.getScheme(), uri.getHost(), uri.getPort());
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public URI toURI() {
        try {
            return new URI(scheme, null, host, port, null, null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Unable to create uri");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HTTPHost httpHost = (HTTPHost) o;

        if (port != httpHost.port) {
            return false;
        }
        if (host != null ? !host.equals(httpHost.host) : httpHost.host != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return toURI().toString();
    }
}
