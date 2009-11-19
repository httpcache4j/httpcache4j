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

package org.codehaus.httpcache4j.urlconnection;

import org.codehaus.httpcache4j.resolver.AbstractResponseResolver;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.payload.DelegatingInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.Validate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:erlend@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class URLConnectionResponseResolver extends AbstractResponseResolver {
    private final URLConnectionConfigurator configuration;

    public URLConnectionResponseResolver(URLConnectionConfigurator configuration) {
        Validate.notNull(configuration, "Configuration may not be null");
        this.configuration = configuration;
    }

    public HTTPResponse resolve(HTTPRequest request) throws IOException {
        URL url = request.getRequestURI().toURL();
        URLConnection openConnection = url.openConnection();
        if (openConnection instanceof HttpURLConnection) {
            HttpURLConnection connection = (HttpURLConnection) openConnection;
            if (configuration.isPreemtiveAuthentication() && request.getChallenge() != null) {
                request = addAuthorizationHeader(request);
            }
            doRequest(request, connection);
            Status status = Status.valueOf(connection.getResponseCode());
            if (status == Status.UNAUTHORIZED && request.getChallenge() != null && !configuration.isPreemtiveAuthentication()) {
                request = addAuthorizationHeader(request);
                connection.disconnect();
                connection = (HttpURLConnection) url.openConnection();
                doRequest(request, connection);
            }
            return convertResponse(connection);
        }
        throw new HTTPException("This resolver only supports HTTP calls");
    }

    private void doRequest(HTTPRequest request, HttpURLConnection connection) throws IOException {
        configureConnection(connection);
        connection.setRequestMethod(request.getMethod().name());
        Headers requestHeaders = request.getAllHeaders();

        for (Header header : requestHeaders) {
            connection.addRequestProperty(header.getName(), header.getValue());
        }
        connection.connect();
        writeRequest(request, connection);
    }

    private HTTPResponse convertResponse(HttpURLConnection connection) throws IOException {
        Status status = Status.valueOf(connection.getResponseCode());
        Headers responseHeaders = getResponseHeaders(connection);
        return getResponseCreator().createResponse(status, responseHeaders, wrapReponseStream(connection, status));
    }

    private HTTPRequest addAuthorizationHeader(final HTTPRequest request) {
        if (request.getChallenge().getMethod() == ChallengeMethod.BASIC) {
            UsernamePasswordChallenge upc = (UsernamePasswordChallenge) request.getChallenge();
            String basicString = request.getChallenge().getIdentifier() + ":" + new String(upc.getPassword());
            try {
                basicString = new String(Base64.encodeBase64(basicString.getBytes("UTF-8")));
                return request.addHeader("Authorization", request.getChallenge().getMethod().name() + " " + basicString);
            } catch (UnsupportedEncodingException e) {
                throw new Error("UTF-8 is not supported on this platform", e);
            }
        }
        else if (request.getChallenge().getMethod() == ChallengeMethod.DIGEST) {
            throw new UnsupportedOperationException("Digest is not yet supported");
        }
        return request;
    }

    private InputStream wrapReponseStream(HttpURLConnection connection, Status status) {
        try {
            return new HttpURLConnectionStream(connection, status);
        } catch (IOException e) {
            connection.disconnect();
            throw new HTTPException(e);
        }
    }

    private void writeRequest(HTTPRequest request, HttpURLConnection connection) throws IOException {
        if (request.hasPayload()) {
            InputStream requestStream = request.getPayload().getInputStream();
            OutputStream connectionStream = null;
            try {
                connectionStream = connection.getOutputStream();
                IOUtils.copy(requestStream, connectionStream);
            } finally {
                IOUtils.closeQuietly(requestStream);
                IOUtils.closeQuietly(connectionStream);
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
        if (configuration.getConnectTimeout() > 0) {
            connection.setConnectTimeout(configuration.getConnectTimeout());
        }
        if (configuration.getReadTimeout() > 0) {
            connection.setReadTimeout(configuration.getReadTimeout());
        }
        connection.setAllowUserInteraction(false);
    }

    private static class HttpURLConnectionStream extends DelegatingInputStream {
        private final HttpURLConnection connection;

        public HttpURLConnectionStream(final HttpURLConnection connection, Status status) throws IOException {
            super(status.isClientError() || status.isServerError() ? connection.getErrorStream() : connection.getInputStream());
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
