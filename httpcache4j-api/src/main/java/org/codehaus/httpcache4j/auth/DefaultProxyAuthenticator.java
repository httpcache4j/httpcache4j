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

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Status;
import org.codehaus.httpcache4j.Challenge;
import org.apache.commons.lang.Validate;

import java.util.List;
import java.util.Collections;
import java.net.URI;

import com.google.common.collect.Lists;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DefaultProxyAuthenticator implements ProxyAuthenticator {

    private final ProxyConfiguration configuration;
    private final List<AuthenticatorStrategy> strategies = Lists.newArrayList();
    private final SchemeRegistry registry = new SchemeRegistry();

    private Challenge proxyChallenge;

    public DefaultProxyAuthenticator(ProxyConfiguration configuration) {
        Validate.notNull(configuration, "Configuration may not be null");
        this.configuration = configuration;
        strategies.addAll(createStrategies());
    }

    protected List<AuthenticatorStrategy> createStrategies() {
        return Collections.<AuthenticatorStrategy>singletonList(new BasicAuthenticatorStrategy());
    }

    public final HTTPRequest prepareAuthentication(final HTTPRequest request, final HTTPResponse response) {        
        if (configuration.getHost() != null) {
            if (response == null && registry.matches(configuration.getHost())) {
                //preemtive auth.
                AuthScheme authScheme = registry.get(configuration.getHost());
                return doAuth(request, authScheme);
            }
            if (response != null && response.getStatus() == Status.PROXY_AUTHENTICATION_REQUIRED) {
                if (proxyChallenge == null) {
                    proxyChallenge = configuration.getProvider().getChallenge();
                }
                if (proxyChallenge != null) {
                    AuthScheme scheme = new AuthScheme(response.getHeaders().getFirstHeader("Proxy-Authenticate"));
                    registry.register(configuration.getHost(), scheme);
                    return doAuth(request, scheme);
                }
            }
        }
        return request;
    }

    public HTTPRequest preparePreemptiveAuthentication(HTTPRequest request) {
        return prepareAuthentication(request, null);
    }

    private HTTPRequest doAuth(HTTPRequest request, AuthScheme scheme) {
        if (!configuration.isHostIgnored(request.getRequestURI().getHost())) {
            for (AuthenticatorStrategy strategy : strategies) {
                if (strategy.supports(scheme)) {
                    return strategy.prepareWithProxy(request, proxyChallenge, scheme);
                }
            }
        }
        return request;
    }

    public void invalidateAuthentication() {
        proxyChallenge = null;
    }

    public ProxyConfiguration getConfiguration() {
        return configuration;
    }
}
