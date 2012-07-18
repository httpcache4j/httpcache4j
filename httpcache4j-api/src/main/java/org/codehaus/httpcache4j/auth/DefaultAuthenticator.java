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

import org.codehaus.httpcache4j.util.Pair;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DefaultAuthenticator extends AuthenticatorBase implements Authenticator {

    public DefaultAuthenticator() {
        super();
    }

    public DefaultAuthenticator(List<AuthenticatorStrategy> strategies) {
        super(strategies);
    }

    public final HTTPRequest prepareAuthentication(final HTTPRequest request, final HTTPResponse response) {
        HTTPRequest req = request;
        HTTPHost host = new HTTPHost(request.getRequestURI());
        if (response == null && registry.matches(host)) {
            //preemptive auth.
            AuthScheme authScheme = registry.get(host);
            Pair<AuthenticatorStrategy, AuthScheme> selected = select(ImmutableList.of(authScheme));
            return selected.getKey().prepare(request, authScheme);

        }
        if (response != null && response.getStatus() == Status.UNAUTHORIZED) {
            List<AuthScheme> schemes = toAuthSchemes(response, HeaderConstants.WWW_AUTHENTICATE);
            if (!schemes.isEmpty() && request.getChallenge() != null) {
                Pair<AuthenticatorStrategy, AuthScheme> selected = select(schemes);
                if (selected.getValue() != null) {
                    req = selected.getKey().prepare(request, selected.getValue());
                    if (req != request) { //If authentication header was added
                        registry.register(host, selected.getValue());
                    }
                }
            }
        }
        return req;
    }

    public boolean canAuthenticatePreemptively(HTTPRequest request) {
        return canAuthenticatePreemptively(new HTTPHost(request.getRequestURI()));
    }

    public HTTPRequest preparePreemptiveAuthentication(HTTPRequest request) {
        return prepareAuthentication(request, null);
    }

    public void afterSuccessfulAuthentication(HTTPRequest request, Headers responseHeaders) {
        HTTPHost host = new HTTPHost(request.getRequestURI());
        if (registry.matches(host)) {
            AuthScheme scheme = registry.get(host);
            Pair<AuthenticatorStrategy, AuthScheme> select = select(ImmutableList.of(scheme));
            AuthScheme updatedScheme = select.getKey().afterSuccessfulAuthentication(scheme, responseHeaders);
            if (updatedScheme != scheme) {
                registry.register(host, updatedScheme);                
            }
        }
    }

    public void afterFailedAuthentication(HTTPRequest request, Headers responseHeaders) {
        HTTPHost host = new HTTPHost(request.getRequestURI());
        registry.remove(host);
    }
}
