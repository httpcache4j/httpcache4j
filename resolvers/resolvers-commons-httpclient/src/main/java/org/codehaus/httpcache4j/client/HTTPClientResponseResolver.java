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

package org.codehaus.httpcache4j.client;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.lang.Validate;

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.auth.*;
import org.codehaus.httpcache4j.payload.DelegatingInputStream;
import org.codehaus.httpcache4j.resolver.AbstractResponseResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * An implementation of the ResponseResolver using the Commons HTTPClient (http://hc.apache.org/httpclient-3.x/)
 * <p/>
 * If you need to use SSL, please follow the guide here.
 * http://hc.apache.org/httpclient-3.x/sslguide.html
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
//TODO: add default user agent. This should maybe only be the cache? Maybe the client type as well. Add support for the client of the cache???
public class HTTPClientResponseResolver extends AbstractResponseResolver {
    private final HttpClient client;

    protected HTTPClientResponseResolver(HttpClient client, ProxyAuthenticator proxyAuthenticator, Authenticator authenticator) {
        super(proxyAuthenticator, authenticator);
        Validate.notNull(client, "You may not create with a null HttpClient");
        this.client = client;
        HTTPHost proxyHost = proxyAuthenticator.getConfiguration().getHost();
        if (proxyHost != null) {
            this.client.getHostConfiguration().setProxy(proxyHost.getHost(), proxyHost.getPort());
        }
    }

    public HTTPClientResponseResolver(HttpClient client, ProxyConfiguration configuration) {
        this(client, new DefaultProxyAuthenticator(configuration), new DefaultAuthenticator());
    }

    /**
     * If you use this the cache from multiple threads you need to create a multithreaded HttpClient.
     * example: <br/>
     * {@code
     * HttpClient client = new HttpClient(new MultiThreadedConnectionManager());
     * ResponseResolver resolver = HTTPClientResponseResolver(client);
     * }
     *
     * @param client the HttpClient instance to use. may not be {@code null}
     */
    public HTTPClientResponseResolver(HttpClient client) {
        this(client, new ProxyConfiguration());
    }

    public static HTTPClientResponseResolver createMultithreadedInstance() {
        return new HTTPClientResponseResolver(new HttpClient(new MultiThreadedHttpConnectionManager()));
    }

    public final HttpClient getClient() {
        return client;
    }

    public HTTPResponse resolve(final HTTPRequest request) throws IOException {
        HTTPRequest req = request;
        if (getAuthenticator().canAuthenticatePreemptively(request)) {
            req = getAuthenticator().preparePreemptiveAuthentication(request);
        }
        if (getProxyAuthenticator().canAuthenticatePreemptively()) {
            req = getProxyAuthenticator().preparePreemptiveAuthentication(req);            
        }


        HttpMethod method = convertRequest(req);
        client.executeMethod(method);
        HTTPResponse response = convertResponse(method);

        if (response.getStatus() == Status.PROXY_AUTHENTICATION_REQUIRED) {
            req = getProxyAuthenticator().prepareAuthentication(req, response);
            if (req != request) {
                response.consume();
                method = convertRequest(req);
                method.setDoAuthentication(true);
                client.executeMethod(method);
                response = convertResponse(method);
                if (response.getStatus() == Status.PROXY_AUTHENTICATION_REQUIRED) { //We failed
                    getProxyAuthenticator().afterFailedAuthentication(response.getHeaders());
                }
                else {
                    getProxyAuthenticator().afterSuccessfulAuthentication(response.getHeaders());
                }
            }
        }       
        if (response.getStatus() == Status.UNAUTHORIZED) {
            req = getAuthenticator().prepareAuthentication(req, response);

            if (req != request) {
                response.consume();
                method = convertRequest(req);
                method.setDoAuthentication(true);
                client.executeMethod(method);
                response = convertResponse(method);
                if (response.getStatus() == Status.UNAUTHORIZED) {
                    getAuthenticator().afterFailedAuthentication(req, response.getHeaders());
                }
                else {
                    getAuthenticator().afterSuccessfulAuthentication(req, response.getHeaders());
                }
            }
        }

        return response;
        
    }

    private HttpMethod convertRequest(HTTPRequest request) {
        URI requestURI = request.getRequestURI();
        HttpMethod method = getMethod(request.getMethod(), requestURI);
        Headers requestHeaders = request.getAllHeaders();
        addHeaders(requestHeaders, method);
        method.setDoAuthentication(true);
        if (method instanceof EntityEnclosingMethod && request.hasPayload()) {
            InputStream payload = request.getPayload().getInputStream();
            EntityEnclosingMethod carrier = (EntityEnclosingMethod) method;
            if (payload != null) {
                carrier.setRequestEntity(new InputStreamRequestEntity(payload));
            }
        }

        return method;
    }

    private void addHeaders(Headers headers, HttpMethod method) {
        if (!headers.isEmpty()) {
            for (org.codehaus.httpcache4j.Header header : headers) {
                method.addRequestHeader(header.getName(), header.getValue());
            }
        }
    }

    private HTTPResponse convertResponse(HttpMethod method) {
        Headers headers = new Headers();
        for (Header header : method.getResponseHeaders()) {
            headers = headers.add(header.getName(), header.getValue());
        }
        InputStream stream = null;
        HTTPResponse response;
        try {
            stream = getInputStream(method);
            response = getResponseCreator().createResponse(Status.valueOf(method.getStatusCode()), headers, stream);
        } finally {
            if (stream == null) {
                method.releaseConnection();
            }
        }
        return response;
    }

    private InputStream getInputStream(HttpMethod method) {
        try {
            return method.getResponseBodyAsStream() != null ? new HttpMethodStream(method) : null;
        }
        catch (IOException e) {
            method.releaseConnection();
            throw new HTTPException("Unable to get InputStream from HttpClient", e);
        }
    }
    
    /**
     * Determines the HttpClient's request method from the HTTPMethod enum.
     *
     * @param method     the HTTPCache enum that determines
     * @param requestURI the request URI.
     * @return a new HttpMethod subclass.
     */
    HttpMethod getMethod(HTTPMethod method, URI requestURI) {
        switch (method) {
            case GET:
                return new GetMethod(requestURI.toString());
            case HEAD:
                return new HeadMethod(requestURI.toString());
            case OPTIONS:
                return new OptionsMethod(requestURI.toString());
            case TRACE:
                return new TraceMethod(requestURI.toString());
            case PUT:
                return new PutMethod(requestURI.toString());
            case POST:
                return new PostMethod(requestURI.toString());
            case DELETE:
                return new DeleteMethod(requestURI.toString());
            default:
                throw new IllegalArgumentException("Uknown method");
        }
    }

    private static class HttpMethodStream extends DelegatingInputStream {
        private final HttpMethod method;

        public HttpMethodStream(final HttpMethod method) throws IOException {
            super(method.getResponseBodyAsStream());
            this.method = method;
        }

        public void close() throws IOException {
            method.releaseConnection();
        }
    }

}
