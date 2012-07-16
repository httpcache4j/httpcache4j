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

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class BasicAuthenticatorStrategy implements AuthenticatorStrategy {

    public boolean supports(final AuthScheme scheme) {
        return "basic".equalsIgnoreCase(scheme.getType());
    }

    public HTTPRequest prepare(HTTPRequest request, AuthScheme scheme) {
        return prepare(request, request.getChallenge(), false);
    }

    public HTTPRequest prepareWithProxy(HTTPRequest request, Challenge challenge, AuthScheme scheme) {
        return prepare(request, challenge, true);
    }

    private HTTPRequest prepare(final HTTPRequest request, Challenge challenge, boolean proxy) {
        HTTPRequest req = request;        
        if (challenge instanceof UsernamePasswordChallenge) {
            UsernamePasswordChallenge upc = (UsernamePasswordChallenge) challenge;
            String authValue = BasicAuthentication.getHeaderValue(upc);
            if (proxy) {
                req = request.addHeader(HeaderConstants.PROXY_AUTHORIZATION, authValue);
            }
            else {
                req = request.addHeader(HeaderConstants.AUTHORIZATION, authValue);
            }
        }
        return req;
    }

    public AuthScheme afterSuccessfulAuthentication(AuthScheme scheme, Headers headers) {
        return scheme;
    }

    public AuthScheme afterSuccessfulProxyAuthentication(AuthScheme scheme, Headers headers) {
        return scheme;
    }
}
