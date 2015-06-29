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

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.auth.DefaultAuthenticator;
import org.codehaus.httpcache4j.auth.DefaultProxyAuthenticator;
import org.codehaus.httpcache4j.payload.DelegatingInputStream;
import org.codehaus.httpcache4j.util.Base64;
import org.codehaus.httpcache4j.util.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author <a href="mailto:erlend@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class JavaNetResponseResolver extends AbstractResponseResolver {

    public JavaNetResponseResolver(ResolverConfiguration configuration) {
        super(configuration);
        if (configuration.getConnectionConfiguration().getMaxConnections().isPresent()) {
            throw new UnsupportedOperationException("Single Connection only resolver");
        }
        if (configuration.getConnectionConfiguration().getDefaultConnectionsPerHost().isPresent()) {
            throw new UnsupportedOperationException("Single Connection only resolver");
        }
        if (!configuration.getConnectionConfiguration().getConnectionsPerHost().isEmpty()) {
            throw new UnsupportedOperationException("This Resolver does not support connections per host");
        }
    }

    public JavaNetResponseResolver(ConnectionConfiguration connectionConfiguration) {
        this(new ResolverConfiguration(new DefaultProxyAuthenticator(), new DefaultAuthenticator(), connectionConfiguration));
    }

    @Override
    protected HTTPResponse resolveImpl(HTTPRequest request) throws IOException {
        URL url = request.getNormalizedURI().toURL();
        URLConnection openConnection = url.openConnection();
        if (openConnection instanceof HttpsURLConnection) {
            HttpsURLConnection connection = (HttpsURLConnection) openConnection;
            doRequest(request, connection);
            return convertResponse(connection);
        }
        else if (openConnection instanceof HttpURLConnection) {
            HttpURLConnection connection = (HttpURLConnection) openConnection;
            doRequest(request, connection);
            return convertResponse(connection);
        }

        throw new HTTPException("This resolver only supports HTTP");
    }

    public void shutdown() {        
    }

    private void doRequest(HTTPRequest request, HttpURLConnection connection) throws IOException {
        configureConnection(connection);
        connection.setRequestMethod(request.getMethod().getMethod());
        connection.setDoOutput(request.getMethod().canHavePayload());
        Headers requestHeaders = request.getAllHeaders();
        connection.addRequestProperty(HeaderConstants.USER_AGENT, getConfiguration().getUserAgent());

        for (Header header : requestHeaders) {
            connection.addRequestProperty(header.getName(), header.getValue());
        }

        connection.connect();
        writeRequest(request, connection);
    }

    private HTTPResponse convertResponse(HttpURLConnection connection) throws IOException {
        Status status = Status.valueOf(connection.getResponseCode());
        String message = connection.getResponseMessage();
        Headers responseHeaders = getResponseHeaders(connection);
        return ResponseCreator.createResponse(new StatusLine(status, message), responseHeaders, wrapResponseStream(connection, status).orElse(null));
    }

    private Optional<InputStream> wrapResponseStream(HttpURLConnection connection, Status status) {
        try {
            Optional<InputStream> stream = Optional.ofNullable(status.isClientError() || status.isServerError() ? connection.getErrorStream() : connection.getInputStream());
            return stream.map(is -> new HttpURLConnectionStream(connection, is));
        } catch (IOException e) {
            connection.disconnect();
            throw new HTTPException(e);
        }
    }

    private void writeRequest(HTTPRequest request, HttpURLConnection connection) throws IOException {
        if (request.hasPayload()) {
            try(InputStream requestStream = request.getPayload().getInputStream()) {
                if (getConfiguration().isUseChunked()) {
                    connection.setChunkedStreamingMode(2048);
                }
                OutputStream connectionStream = connection.getOutputStream();
                IOUtils.copy(requestStream, connectionStream);
            }
        }
    }

    private Headers getResponseHeaders(HttpURLConnection connection) {
        Headers headers = new Headers();
        Map<String, List<String>> headerFields = connection.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            for (String headerValue : entry.getValue()) {
                if (entry.getKey() != null) {
                    headers = headers.add(entry.getKey(), headerValue);
                }
            }
        }
        return headers;
    }

    private void configureConnection(HttpURLConnection connection) {
        ConnectionConfiguration configuration = getConfiguration().getConnectionConfiguration();
        if (configuration.getSocketTimeout().isPresent()) {
            connection.setConnectTimeout(configuration.getSocketTimeout().get());
        }
        if (configuration.getTimeout().isPresent()) {
            connection.setReadTimeout(configuration.getTimeout().get());
        }
        connection.setAllowUserInteraction(false);
    }

    private static class HttpURLConnectionStream extends DelegatingInputStream {
        private final HttpURLConnection connection;

        public HttpURLConnectionStream(final HttpURLConnection connection, InputStream is) {
            super(is);
            this.connection = connection;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                connection.disconnect();
            }
        }

    }
}
