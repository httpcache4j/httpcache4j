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
import org.codehaus.httpcache4j.resolver.ResponseCreator;
import org.codehaus.httpcache4j.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.codec.binary.Base64;

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
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class URLConnectionResponseResolver extends AbstractResponseResolver {
    private final URLConnectionConfigurator configuration;

    public URLConnectionResponseResolver(ResponseCreator responseCreator, URLConnectionConfigurator configuration) {
        super(responseCreator);
        this.configuration = configuration;
    }

    public HTTPResponse resolve(HTTPRequest request) throws IOException {
        URL url = request.getRequestURI().toURL();
        URLConnection openConnection = url.openConnection();
        if (openConnection instanceof HttpURLConnection) {
            HttpURLConnection connection = (HttpURLConnection) openConnection;
            if (configuration.isPreemtiveAuthentication() && request.getChallenge() != null) {
                addAuthorizationHeader(request);
            }
            doRequest(request, connection);
            Status status = Status.valueOf(connection.getResponseCode());
            if (status == Status.UNAUTHORIZED && request.getChallenge() != null && !configuration.isPreemtiveAuthentication()) {
                addAuthorizationHeader(request);
                connection.disconnect();
                connection = (HttpURLConnection) url.openConnection();
                doRequest(request, connection);                
            }
            return convertResponse(request, connection);
        }
        throw new HTTPException("This resolver only supports HTTP calls");
    }

    private void doRequest(HTTPRequest request, HttpURLConnection connection) throws IOException {
        configureConnection(connection);
        connection.setRequestMethod(request.getMethod().name());
        Headers requestHeaders = request.getAllHeaders();

        for (Map.Entry<String, List<Header>> entry : requestHeaders) {
            for (Header header : entry.getValue()) {
                connection.addRequestProperty(header.getName(), header.getValue());
            }
        }
        connection.connect();
        writeRequest(request, connection);
    }

    private HTTPResponse convertResponse(HTTPRequest request, HttpURLConnection connection) throws IOException {
        Status status = Status.valueOf(connection.getResponseCode());
        Headers responseHeaders = getResponseHeaders(connection);
        return getResponseCreator().createResponse(request, status, responseHeaders, wrapReponseStream(connection, status));
    }

    private void addAuthorizationHeader(HTTPRequest request) {
        if (request.getChallenge().getMethod() == ChallengeMethod.BASIC) {
            String basicString = request.getChallenge().getIdentifier() + ":" + new String(request.getChallenge().getPassword());
            try {
                basicString = new String(Base64.encodeBase64(basicString.getBytes("UTF-8")));
                request.getHeaders().add("Authorization", request.getChallenge().getMethod().name() + " " + basicString);
            } catch (UnsupportedEncodingException e) {
                throw new Error("UTF-8 is not supported on this platform", e);
            }
        }
        else if (request.getChallenge().getMethod() == ChallengeMethod.DIGEST) {
            throw new UnsupportedOperationException("Digest is not yet supported");
        }
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
                    headers.add(entry.getKey(), headerValue);
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

    private static class HttpURLConnectionStream extends InputStream {
        private final HttpURLConnection connection;
        private final InputStream delegate;

        public HttpURLConnectionStream(final HttpURLConnection connection, Status status) throws IOException {
            this.connection = connection;
            if (status.isClientError() || status.isServerError()) {
                delegate = connection.getErrorStream();
            }
            else {
                delegate = connection.getInputStream();
            }
        }

        public int read() throws IOException {
            return delegate.read();
        }

        public int read(final byte[] b) throws IOException {
            return delegate.read(b);
        }

        public int read(final byte[] b, final int off, final int len) throws IOException {
            return delegate.read(b, off, len);
        }

        public long skip(final long n) throws IOException {
            return delegate.skip(n);
        }

        public int available() throws IOException {
            return delegate.available();
        }

        public void close() throws IOException {
            try {
                delegate.close();
            } finally {
                connection.disconnect();
            }
        }

        public void mark(final int readlimit) {
            delegate.mark(readlimit);
        }

        public void reset() throws IOException {
            delegate.reset();
        }

        public boolean markSupported() {
            return delegate.markSupported();
        }
    }
}
