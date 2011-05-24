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

import org.codehaus.httpcache4j.*;
import org.apache.commons.lang.Validate;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DefaultProxyAuthenticator extends AuthenticatorBase implements ProxyAuthenticator {

    private final ProxyConfiguration configuration;

    private Challenge proxyChallenge;

    public DefaultProxyAuthenticator() {
        this(new ProxyConfiguration());
    }

    public DefaultProxyAuthenticator(ProxyConfiguration configuration) {
        super();
        this.configuration = configuration; 
    }

    public DefaultProxyAuthenticator(ProxyConfiguration configuration, final List<AuthenticatorStrategy> strategies) {
        super(strategies);
        Validate.notNull(configuration, "Configuration may not be null");
        this.configuration = configuration;
    }

    public final HTTPRequest prepareAuthentication(final HTTPRequest request, final HTTPResponse response) {        
        if (configuration.getHost() != null) {
            if (response == null && registry.matches(configuration.getHost())) {
                //preemptive auth.
                AuthScheme authScheme = registry.get(configuration.getHost());
                return doAuth(request, authScheme);
            }
            if (response != null && response.getStatus() == Status.PROXY_AUTHENTICATION_REQUIRED) {
                if (proxyChallenge == null) {
                    proxyChallenge = configuration.getProvider().getChallenge();
                }
                if (proxyChallenge != null) {
                    AuthScheme scheme = new AuthScheme(response.getHeaders().getFirstHeader(HeaderConstants.PROXY_AUTHENTICATE));
                    registry.register(configuration.getHost(), scheme);
                    return doAuth(request, scheme);
                }
            }
        }
        return request;
    }

    public boolean canAuthenticatePreemptively() {
        return canAuthenticatePreemptively(configuration.getHost());
    }

    public HTTPRequest preparePreemptiveAuthentication(HTTPRequest request) {
        return prepareAuthentication(request, null);
    }

    private HTTPRequest doAuth(HTTPRequest request, AuthScheme scheme) {
        if (!configuration.isHostIgnored(request.getRequestURI().getHost())) {
            return select(scheme).prepareWithProxy(request, proxyChallenge, scheme);
        }
        return request;
    }

    public void afterSuccessfulAuthentication(Headers responseHeaders) {
        if (registry.matches(configuration.getHost())) {
            AuthScheme scheme = registry.get(configuration.getHost());
            select(scheme).afterSuccessfulProxyAuthentication(scheme, responseHeaders);
        }
    }


    public void afterFailedAuthentication(Headers responseHeaders) {
        invalidateAuthentication();
        registry.remove(configuration.getHost());
    }

    public void invalidateAuthentication() {
        proxyChallenge = null;
    }

    public ProxyConfiguration getConfiguration() {
        return configuration;
    }
}
