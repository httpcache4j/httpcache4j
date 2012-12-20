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

import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.StatusLine;
import org.codehaus.httpcache4j.auth.*;
import org.codehaus.httpcache4j.payload.DelegatingInputStream;
import org.codehaus.httpcache4j.resolver.AbstractResponseResolver;
import org.codehaus.httpcache4j.resolver.ConnectionConfiguration;
import org.codehaus.httpcache4j.resolver.ResolverConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import static org.codehaus.httpcache4j.HTTPMethod.*;

/**
 * An implementation of the ResponseResolver using the Commons HTTPClient (http://hc.apache.org/httpclient-3.x/)
 * <p/>
 * If you need to use SSL, please follow the guide here.
 * http://hc.apache.org/httpclient-3.x/sslguide.html
 * Note that his disables the built in authentication mechanism.
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
        configureConnections(client, configuration);
        params.setAuthenticationPreemptive(false);
        client.setState(new HttpState());
        params.setParameter(HttpClientParams.USER_AGENT, getConfiguration().getUserAgent());        
    }

    private void configureConnections(HttpClient client, ResolverConfiguration configuration) {
        ConnectionConfiguration connectionConfiguration = configuration.getConnectionConfiguration();
        HttpConnectionManagerParams connectionsParams = client.getHttpConnectionManager().getParams();
        if (connectionsParams == null) {
            connectionsParams = new HttpConnectionManagerParams();
            client.getHttpConnectionManager().setParams(connectionsParams);
        }
        if (connectionConfiguration.getDefaultConnectionsPerHost().isPresent()) {
            connectionsParams.setDefaultMaxConnectionsPerHost(connectionConfiguration.getDefaultConnectionsPerHost().get());
        }
        if (connectionConfiguration.getMaxConnections().isPresent()) {
            connectionsParams.setMaxTotalConnections(connectionConfiguration.getMaxConnections().get());
        }
        if (connectionConfiguration.getSocketTimeout().isPresent()) {
            connectionsParams.setSoTimeout(connectionConfiguration.getSocketTimeout().get());
        }
        if (connectionConfiguration.getTimeout().isPresent()) {
            connectionsParams.setConnectionTimeout(connectionConfiguration.getTimeout().get());
        }
        for (Map.Entry<HTTPHost, Integer> entry : connectionConfiguration.getConnectionsPerHost().entrySet()) {
            HostConfiguration hostConfig = new HostConfiguration();
            HTTPHost host = entry.getKey();
            hostConfig.setHost(new HttpHost(host.getHost(), host.getPort(), Protocol.getProtocol(host.getScheme())));
            connectionsParams.setMaxConnectionsPerHost(hostConfig, entry.getValue());
        }
    }

    protected HTTPClientResponseResolver(HttpClient client, ProxyAuthenticator proxyAuthenticator, Authenticator authenticator) {
        this(client, new ResolverConfiguration(proxyAuthenticator, authenticator, new ConnectionConfiguration()));
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

    public static HTTPClientResponseResolver createMultithreadedInstance(ResolverConfiguration configuration) {
        return new HTTPClientResponseResolver(
                new HttpClient(new MultiThreadedHttpConnectionManager()),
                configuration
        );
    }

    public static HTTPClientResponseResolver createMultithreadedInstance(ConnectionConfiguration configuration) {
        return createMultithreadedInstance(new ResolverConfiguration().withConnectionConfiguration(configuration));
    }

    public static HTTPClientResponseResolver createMultithreadedInstance() {
        return createMultithreadedInstance(new ConnectionConfiguration());
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
        URI requestURI = request.getNormalizedURI();
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
    protected HttpMethod getMethod(final HTTPMethod method, URI requestURI) {
        if (method.canHavePayload()) {
            return new MethodWithBody(method, requestURI);
        } else {
            return new Method(method, requestURI);
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

    private static class MethodWithBody extends EntityEnclosingMethod {
        private final HTTPMethod method;

        public MethodWithBody(HTTPMethod method, URI uri) {
            super(uri.toString());
            if (!method.canHavePayload()) {
                throw new IllegalArgumentException("Wrong method created for " + method.getMethod())
            }
            this.method = method;
        }

        @Override
        public String getName() {
            return method.getMethod();
        }
    }

    private static class Method extends HttpMethodBase {
        private final HTTPMethod method;

        public Method(HTTPMethod method, URI uri) {
            super(uri.toString());
            this.method = method;
            setFollowRedirects(method.isSafe());
        }

        @Override
        public String getName() {
            return method.getMethod();
        }
    }
}
