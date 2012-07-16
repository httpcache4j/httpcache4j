/*
 * Copyright (c) 2008, The Codehaus. All Rights Reserved.
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
 *
 */

package org.codehaus.httpcache4j.resolver;

import com.google.common.base.Preconditions;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Status;
import org.codehaus.httpcache4j.auth.*;

import java.io.IOException;

/**
 * Implementors should implement this instead of using the ResponseResolver interface directly.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public abstract class AbstractResponseResolver implements ResponseResolver {
    private final ResponseCreator responseCreator = new ResponseCreator();
    private final ResolverConfiguration configuration;

    protected AbstractResponseResolver(ResolverConfiguration configuration) {
        this.configuration = Preconditions.checkNotNull(configuration, "Configuration may not be null");
    }

    protected final ProxyAuthenticator getProxyAuthenticator() {
        return configuration.getProxyAuthenticator();
    }

    protected ResponseCreator getResponseCreator() {
        return responseCreator;
    }

    protected final Authenticator getAuthenticator() {
        return configuration.getAuthenticator();
    }

    protected final ResolverConfiguration getConfiguration() {
        return configuration;
    }

    public final HTTPResponse resolve(HTTPRequest request) throws IOException {
        return resolveAuthenticated(request, request);
    }

    private HTTPResponse resolveAuthenticated(HTTPRequest request, HTTPRequest req) throws IOException {
        HTTPResponse convertedResponse;
        if (getAuthenticator().canAuthenticatePreemptively(request)) {
            req = getAuthenticator().preparePreemptiveAuthentication(request);
        }
        if (getProxyAuthenticator().canAuthenticatePreemptively()) {
            req = getProxyAuthenticator().preparePreemptiveAuthentication(req);
        }
        convertedResponse = resolveImpl(req);

        if (convertedResponse.getStatus() == Status.PROXY_AUTHENTICATION_REQUIRED) {
            req = getProxyAuthenticator().prepareAuthentication(req, convertedResponse);
            if (req != request) {
                convertedResponse.consume();

                convertedResponse = resolveImpl(req);

                if (convertedResponse.getStatus() == Status.PROXY_AUTHENTICATION_REQUIRED) { //We failed
                    getProxyAuthenticator().afterFailedAuthentication(convertedResponse.getHeaders());
                }
                else {
                    getProxyAuthenticator().afterSuccessfulAuthentication(convertedResponse.getHeaders());
                }
            }
        }
        if (convertedResponse.getStatus() == Status.UNAUTHORIZED) {
            req = getAuthenticator().prepareAuthentication(req, convertedResponse);
            if (req != request) {
                convertedResponse.consume();
                convertedResponse = resolveImpl(req);
                if (convertedResponse.getStatus() == Status.UNAUTHORIZED) { //We failed
                    getAuthenticator().afterFailedAuthentication(req, convertedResponse.getHeaders());
                }
                else {
                    getAuthenticator().afterSuccessfulAuthentication(req, convertedResponse.getHeaders());
                }
            }
        }
        return convertedResponse;
    }

    protected abstract HTTPResponse resolveImpl(HTTPRequest request) throws IOException;
}
