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

package org.codehaus.httpcache4j.urlconnection;

import com.google.common.base.Preconditions;
import org.codehaus.httpcache4j.auth.ProxyConfiguration;

/**
 * @author <a href="mailto:erlend@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class URLConnectionConfigurator {
    private final int readTimeout;
    private final int connectTimeout;
    private final ProxyConfiguration proxyConfiguration;

    public URLConnectionConfigurator() {
        this.proxyConfiguration = new ProxyConfiguration();
        readTimeout = 0;
        connectTimeout = 0;
    }

    public URLConnectionConfigurator(int readTimeout, int connectTimeout, ProxyConfiguration proxyConfiguration) {
        this.proxyConfiguration = proxyConfiguration;
        Preconditions.checkArgument(readTimeout > 0, "Read timeout must be positive");
        Preconditions.checkArgument(connectTimeout > 0, "Connect timeout must be positive");
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public ProxyConfiguration getProxyConfiguration() {
        return proxyConfiguration;
    }
}
