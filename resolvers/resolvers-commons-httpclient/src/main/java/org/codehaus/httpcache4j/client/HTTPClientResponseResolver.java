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

import com.google.common.base.Preconditions;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpClientParams;

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.StatusLine;
import org.codehaus.httpcache4j.auth.*;
import org.codehaus.httpcache4j.payload.DelegatingInputStream;
import org.codehaus.httpcache4j.resolver.AbstractResponseResolver;
import org.codehaus.httpcache4j.resolver.ResolverConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.codehaus.httpcache4j.HTTPMethod.*;

/**
 * An implementation of the ResponseResolver using the Commons HTTPClient (http://hc.apache.org/httpclient-3.x/)
 * <p/>
 * If you need to use SSL, please follow the guide here.
 * http://hc.apache.org/httpclient-3.x/sslguide.html
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class HTTPClientResponseResolver extends AbstractResponseResolver {
    private final HttpClient client;

    protected HTTPClientResponseResolver(HttpClient client, ResolverConfiguration configuration) {
        super(configuration);
        this.client = Preconditions.checkNotNull(client, "You may not create with a null HttpClient");
        HTTPHost proxyHost = getProxyAuthenticator().getConfiguration().getHost();
        if (proxyHost != null) {
            this.client.getHostConfiguration().setProxy(proxyHost.getHost(), proxyHost.getPort());
        }
        HttpClientParams params = client.getParams();
        if(params==null) { 
        	params = new HttpClientParams();
        	client.setParams(params);
        }        
        params.setParameter(HttpClientParams.USER_AGENT, getConfiguration().getUserAgent());        
    }

    protected HTTPClientResponseResolver(HttpClient client, ProxyAuthenticator proxyAuthenticator, Authenticator authenticator) {
        this(client, new ResolverConfiguration(proxyAuthenticator, authenticator));
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

    @Override
    protected HTTPResponse resolveImpl(HTTPRequest request) throws IOException {
        HttpMethod method = convertRequest(request);
        method.setDoAuthentication(true);
        client.executeMethod(method);
        return convertResponse(method);
    }

    private HttpMethod convertRequest(HTTPRequest request) throws IOException {
        URI requestURI = request.getRequestURI();
        HttpMethod method = getMethod(request.getMethod(), requestURI);
        Headers requestHeaders = request.getAllHeaders();
        addHeaders(requestHeaders, method);
        if (method instanceof EntityEnclosingMethod && request.hasPayload()) {
            InputStream payload = request.getPayload().getInputStream();
            EntityEnclosingMethod carrier = (EntityEnclosingMethod) method;
            if (payload != null) {
                carrier.setContentChunked(getConfiguration().isUseChunked());
                carrier.setRequestEntity(new InputStreamRequestEntity(payload, request.getPayload().length()));
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
            StatusLine line = new StatusLine(
                    HTTPVersion.get(method.getStatusLine().getHttpVersion()),
                    Status.valueOf(method.getStatusCode()),
                    method.getStatusText()
            );
            response = getResponseCreator().createResponse(line, headers, stream);
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
        } catch (IOException e) {
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
    protected HttpMethod getMethod(HTTPMethod method, URI requestURI) {
        if (CONNECT.equals(method)) {
            HostConfiguration config = new HostConfiguration();
            config.setHost(requestURI.getHost(), requestURI.getPort(), requestURI.getScheme());
            return new ConnectMethod(config);
        } else if (DELETE.equals(method)) {
            return new DeleteMethod(requestURI.toString());
        } else if (GET.equals(method)) {
            return new GetMethod(requestURI.toString());
        } else if (HEAD.equals(method)) {
            return new HeadMethod(requestURI.toString());
        } else if (OPTIONS.equals(method)) {
            return new OptionsMethod(requestURI.toString());
        } else if (POST.equals(method)) {
            return new PostMethod(requestURI.toString());
        } else if (PUT.equals(method)) {
            return new PutMethod(requestURI.toString());
        } else if (TRACE.equals(method)) {
            return new TraceMethod(requestURI.toString());
        } else {
            throw new IllegalArgumentException("Cannot handle method: " + method);
        }
    }

    public void shutdown() {
        HttpConnectionManager connmanager = client.getHttpConnectionManager();
        if (connmanager instanceof MultiThreadedHttpConnectionManager) {
            MultiThreadedHttpConnectionManager manager = (MultiThreadedHttpConnectionManager) connmanager;
            manager.shutdown();
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
