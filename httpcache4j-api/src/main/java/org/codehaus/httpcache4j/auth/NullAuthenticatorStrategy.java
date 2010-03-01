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

package org.codehaus.httpcache4j.auth;

import org.codehaus.httpcache4j.Challenge;
import org.codehaus.httpcache4j.HTTPHost;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.Headers;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
class NullAuthenticatorStrategy implements AuthenticatorStrategy {
    public boolean supports(AuthScheme scheme) {
        return true;
    }

    public HTTPRequest prepare(HTTPRequest request, AuthScheme scheme) {
        return request;
    }

    public HTTPRequest prepareWithProxy(HTTPRequest request, Challenge challenge, AuthScheme scheme) {
        return request;
    }

    public AuthScheme afterSuccessfulAuthentication(AuthScheme scheme, Headers headers) {
        return scheme;
    }

    public AuthScheme afterSuccessfulProxyAuthentication(AuthScheme scheme, Headers headers) {
        return scheme;
    }
}
