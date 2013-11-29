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

package org.codehaus.httpcache4j.resolver;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.payload.DelegatingInputStream;
import org.codehaus.httpcache4j.payload.Payload;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class HTTPClientResponseResolver extends AbstractResponseResolver {
    private final CloseableHttpClient httpClient;

    HTTPClientResponseResolver(CloseableHttpClient httpClient, ResolverConfiguration configuration) {
        super(configuration);
        this.httpClient = httpClient;
    }

    public static HTTPClientResponseResolver create(HttpClientFactory factory,
                                                    ResolverConfiguration configuration,
                                                    IdleConnectionMonitor.Configuration idleConfig) {
        HttpClientBuilder builder = factory.createHttpClientBuilder();
        CloseableHttpClient client = factory.configure(builder, configuration, idleConfig);
        return new HTTPClientResponseResolver(client, configuration);
    }

    public static HTTPClientResponseResolver create(ResolverConfiguration configuration, IdleConnectionMonitor.Configuration idleConfig) {
        return create(new HttpClientFactory(), configuration, idleConfig);
    }

    public static HTTPClientResponseResolver create(ResolverConfiguration configuration) {
        return create(configuration, null);
    }

    public static HTTPClientResponseResolver create(ConnectionConfiguration config) {
        return create(new ResolverConfiguration().withConnectionConfiguration(config));
    }

    public static HTTPClientResponseResolver create() {
        return create(new ConnectionConfiguration());
    }

    public final CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    protected HTTPResponse resolveImpl(HTTPRequest request) throws IOException {
        HttpUriRequest realRequest = convertRequest(request);
        HttpResponse response = httpClient.execute(realRequest);
        return convertResponse(realRequest, response);
    }

    public void shutdown() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                throw new HTTPException(e);
            }
        }
    }

    private HttpUriRequest convertRequest(HTTPRequest request) {
        HttpUriRequest realRequest = getMethod(request.getMethod(), request.getNormalizedURI());

        Headers headers = request.getAllHeaders();
        for (Header header : headers) {
            realRequest.addHeader(header.getName(), header.getValue());
        }

        if (request.hasPayload() && realRequest instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest req = (HttpEntityEnclosingRequest) realRequest;
            req.setEntity(new PayloadEntity(request.getPayload(), getConfiguration().isUseChunked()));
        }
        return realRequest;
    }

    protected HttpUriRequest getMethod(HTTPMethod method, URI requestURI) {
        if (method.canHavePayload()) {
            return new MethodWithBody(requestURI, method);
        } else {
            return new Method(requestURI, method);
        }
    }

    private HTTPResponse convertResponse(HttpUriRequest request, HttpResponse response) throws IOException {
        Headers headers = new Headers();
        org.apache.http.Header[] realHeaders = response.getAllHeaders();
        for (org.apache.http.Header header : realHeaders) {
            headers = headers.add(header.getName(), header.getValue());
        }

        InputStream stream = getStream(request, response);
        ProtocolVersion protocolversion = response.getStatusLine().getProtocolVersion();
        StatusLine line = new StatusLine(
                HTTPVersion.get(protocolversion.getMajor() + "." + protocolversion.getMinor()),
                Status.valueOf(response.getStatusLine().getStatusCode()),
                response.getStatusLine().getReasonPhrase());
        return getResponseCreator().createResponse(line, headers, stream);
    }

    private InputStream getStream(HttpUriRequest realRequest, HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        try {
            return new HttpEntityInputStream(entity);
        } catch (IOException e) {
            realRequest.abort();
            throw e;
        } catch (RuntimeException e) {
            realRequest.abort();
            throw e;
        }
    }

    private static class HttpEntityInputStream extends DelegatingInputStream {
        private final HttpEntity entity;

        public HttpEntityInputStream(HttpEntity entity) throws IOException {
            super(entity.getContent());
            this.entity = entity;
        }

        @Override
        public void close() throws IOException {
            EntityUtils.consume(entity);
        }
    }

    private static class PayloadEntity extends InputStreamEntity {
        public PayloadEntity(final Payload payload, boolean chunked) {
            super(payload.getInputStream(), chunked ? -1 : payload.length());
            setContentType(payload.getMimeType().toString());
            setChunked(chunked);
        }
    }

    @NotThreadSafe
    private static class Method extends HttpRequestBase {

        private HTTPMethod method;

        public Method(final URI uri, HTTPMethod method) {
            super();
            this.method = method;
            setURI(uri);
        }

        @Override
        public String getMethod() {
            return method.getMethod();
        }

    }

    @NotThreadSafe
    private static class MethodWithBody extends HttpEntityEnclosingRequestBase {

        private HTTPMethod method;

        public MethodWithBody(final URI uri, HTTPMethod method) {
            super();
            this.method = method;
            setURI(uri);
        }

        @Override
        public String getMethod() {
            return method.getMethod();
        }

    }
}
