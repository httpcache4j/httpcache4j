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

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Status;
import org.codehaus.httpcache4j.auth.*;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementors should implement this instead of using the ResponseResolver interface directly.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public abstract class AbstractResponseResolver implements ResponseResolver {
    private final ResolverConfiguration configuration;

    protected AbstractResponseResolver(ResolverConfiguration configuration) {
        this.configuration = Objects.requireNonNull(configuration, "Configuration may not be null");
    }

    protected final ProxyAuthenticator getProxyAuthenticator() {
        return configuration.getProxyAuthenticator();
    }

    protected final Authenticator getAuthenticator() {
        return configuration.getAuthenticator();
    }

    protected final ResolverConfiguration getConfiguration() {
        return configuration;
    }

    public final CompletableFuture<HTTPResponse> resolve(HTTPRequest request) {
        return resolveAuthenticated(request);
    }

    private CompletableFuture<HTTPResponse> resolveAuthenticated(final HTTPRequest request) {
        HTTPRequest req = request;
        if (getAuthenticator().canAuthenticatePreemptively(request)) {
            req = getAuthenticator().preparePreemptiveAuthentication(request);
        }
        if (getProxyAuthenticator().canAuthenticatePreemptively()) {
            req = getProxyAuthenticator().preparePreemptiveAuthentication(req);
        }
        HTTPRequest finalReq = req;
        return resolveImpl(req).thenCompose(convertedResponse -> {
            if (convertedResponse.getStatus() == Status.PROXY_AUTHENTICATION_REQUIRED) {
                return resolveProxy(finalReq, convertedResponse);
            }
            if (convertedResponse.getStatus() == Status.UNAUTHORIZED) {
                return resolveUnauthorized(finalReq, convertedResponse);
            }
            return CompletableFuture.completedFuture(convertedResponse);
        });


    }

    protected CompletableFuture<HTTPResponse> resolveProxy(final HTTPRequest request, final HTTPResponse response) {
        HTTPRequest req = getProxyAuthenticator().prepareAuthentication(request, response);
        if (req != request) {
            response.consume();

            return resolveImpl(req).thenApply(res -> {
                if (res != null) {
                    if (res.getStatus() == Status.PROXY_AUTHENTICATION_REQUIRED) { //We failed
                        getProxyAuthenticator().afterFailedAuthentication(res.getHeaders());
                    } else {
                        getProxyAuthenticator().afterSuccessfulAuthentication(res.getHeaders());
                    }
                }
                return res;
            });
        }
        return CompletableFuture.completedFuture(response);
    }

    protected CompletableFuture<HTTPResponse> resolveUnauthorized(final HTTPRequest request, final HTTPResponse response) {
        HTTPRequest req = getAuthenticator().prepareAuthentication(request, response);
        if (req != request) {
            response.consume();

            return resolveImpl(req).thenApply(res -> {
                if (res != null) {
                    if (res.getStatus() == Status.UNAUTHORIZED) { //We failed
                        getAuthenticator().afterFailedAuthentication(req, res.getHeaders());
                    } else {
                        getAuthenticator().afterSuccessfulAuthentication(req, res.getHeaders());
                    }
                }
                return res;
            });
        }
        return CompletableFuture.completedFuture(response);
    }

    protected static ExecutorService defaultExecutor() {
        return Executors.newFixedThreadPool(4);
    }

    protected abstract CompletableFuture<HTTPResponse> resolveImpl(HTTPRequest request);

    @Override
    public final void close() throws Exception {
        shutdown();
    }
}
