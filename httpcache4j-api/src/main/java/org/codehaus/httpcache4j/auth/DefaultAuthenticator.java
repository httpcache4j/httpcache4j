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

import com.google.common.collect.ImmutableList;
import org.codehaus.httpcache4j.*;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DefaultAuthenticator implements Authenticator {
    private final List<AuthenticatorStrategy> strategies = Lists.newArrayList();
    private final SchemeRegistry registry = new SchemeRegistry();

    public DefaultAuthenticator() {
        this(defaultStrategies());
    }

    public DefaultAuthenticator(List<AuthenticatorStrategy> strategies) {
        this.strategies.addAll(strategies);
    }

    private static List<AuthenticatorStrategy> defaultStrategies() {
        return ImmutableList.of(new DigestAuthenticatorStrategy(), new BasicAuthenticatorStrategy());
    }

    public final HTTPRequest prepareAuthentication(final HTTPRequest request, final HTTPResponse response) {
        HTTPHost host = new HTTPHost(request.getRequestURI());
        if (response == null && registry.matches(host)) {
            //preemptive auth.
            AuthScheme authScheme = registry.get(host);
            for (AuthenticatorStrategy strategy : strategies) {
                if (strategy.supports(authScheme)) {
                    return strategy.prepare(request, authScheme);
                }
            }
        }
        if (response != null && response.getStatus() == Status.UNAUTHORIZED) {
            Header authenticateHeader = response.getHeaders().getFirstHeader(HeaderConstants.WWW_AUTHENTICATE);
            if (authenticateHeader != null && request.getChallenge() != null) {
                AuthScheme scheme = new AuthScheme(authenticateHeader);
                for (AuthenticatorStrategy strategy : strategies) {
                    if (strategy.supports(scheme)) {
                        return strategy.prepare(request, scheme);
                    }
                }
                registry.register(host, scheme);
            }
        }
        return request;
    }

    public HTTPRequest preparePreemptiveAuthentication(HTTPRequest request) {
        return prepareAuthentication(request, null);
    }
}
