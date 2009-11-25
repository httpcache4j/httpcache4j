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
import org.codehaus.httpcache4j.Header;
import org.codehaus.httpcache4j.auth.ChallengeProvider;
import org.codehaus.httpcache4j.auth.ProxyConfiguration;
import org.codehaus.httpcache4j.payload.DelegatingInputStream;
import org.apache.http.client.HttpClient;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.methods.*;
import org.apache.http.*;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.auth.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultProxyAuthenticationHandler;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.apache.http.entity.InputStreamEntity;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class HTTPClientResponseResolver extends AbstractResponseResolver {
    private HttpClient httpClient;

    public HTTPClientResponseResolver(HttpClient httpClient, ProxyConfiguration proxyConfiguration) {
        super(proxyConfiguration);
        this.httpClient = httpClient;
        HTTPHost proxyHost = proxyConfiguration.getHost();
        if (proxyHost != null) {
            HttpHost host = new HttpHost(proxyHost.getHost(), proxyHost.getPort(), "http");
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, host);
        }
    }

    public HTTPClientResponseResolver(HttpClient httpClient) {
        this(httpClient, new ProxyConfiguration());
    }

    public HTTPResponse resolve(final HTTPRequest request) throws IOException {
        HTTPRequest req = request;
        if (isPreemptiveAuthentication()) {
            req = getAuthenticator().preparePreemptiveAuthentication(request);
            req = getProxyAuthenticator().preparePreemptiveAuthentication(req);
        }

        HttpUriRequest realRequest = convertRequest(req);        
        HttpResponse response = httpClient.execute(realRequest);
        HTTPResponse convertedResponse = convertResponse(realRequest, response);

        if (convertedResponse.getStatus() == Status.PROXY_AUTHENTICATION_REQUIRED) {
            req = getProxyAuthenticator().prepareAuthentication(req, convertedResponse);
            if (req != request) {
                convertedResponse.consume();
                realRequest = convertRequest(req);
                response = httpClient.execute(realRequest);
                convertedResponse = convertResponse(realRequest, response);
                if (convertedResponse.getStatus() == Status.PROXY_AUTHENTICATION_REQUIRED) { //We failed
                    getProxyAuthenticator().invalidateAuthentication();
                    setPreemptiveAuthentication(false);
                }
                else {
                    setPreemptiveAuthentication(true);
                }
            }
        }
        if (convertedResponse.getStatus() == Status.UNAUTHORIZED) {
            req = getAuthenticator().prepareAuthentication(req, convertedResponse);
            if (req != request) {
                convertedResponse.consume();
                realRequest = convertRequest(req);
                response = httpClient.execute(realRequest);
                convertedResponse = convertResponse(realRequest, response);
                if (convertedResponse.getStatus() == Status.UNAUTHORIZED) { //We failed
                    setPreemptiveAuthentication(false);
                }
                else {
                    setPreemptiveAuthentication(true);
                }
            }
        }

        return convertedResponse;
    }

    private HttpUriRequest convertRequest(HTTPRequest request) {
        HttpUriRequest realRequest = getMethod(request.getMethod(), request.getRequestURI());

        Headers headers = request.getAllHeaders();
        for (Header header : headers) {
            realRequest.addHeader(header.getName(), header.getValue());
        }

        if (request.hasPayload() && realRequest instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest req = (HttpEntityEnclosingRequest) realRequest;
            InputStreamEntity e = new InputStreamEntity(request.getPayload().getInputStream(), -1) {
                @Override
                public void writeTo(OutputStream outstream) throws IOException {
                    IOUtils.copy(getContent(), outstream);
                }
            };
            e.setContentType(request.getPayload().getMimeType().toString());
            req.setEntity(e);
        }
        return realRequest;
    }

    /**
     * Determines the HttpClient's request method from the HTTPMethod enum.
     *
     * @param method     the HTTPCache enum that determines
     * @param requestURI the request URI.
     * @return a new HttpMethod subclass.
     */
    HttpUriRequest getMethod(HTTPMethod method, URI requestURI) {
        switch (method) {
            case GET:
                return new HttpGet(requestURI);
            case HEAD:
                return new HttpHead(requestURI);
            case OPTIONS:
                return new HttpOptions(requestURI);
            case TRACE:
                return new HttpTrace(requestURI);
            case PUT:
                return new HttpPut(requestURI);
            case POST:
                return new HttpPost(requestURI);
            case DELETE:
                return new HttpDelete(requestURI);
            default:
                throw new IllegalArgumentException("Uknown method");
        }
    }

    private HTTPResponse convertResponse(HttpUriRequest request, HttpResponse response) throws IOException {
        Status status = Status.valueOf(response.getStatusLine().getStatusCode());
        Headers headers = new Headers();
        org.apache.http.Header[] realHeaders = response.getAllHeaders();
        for (org.apache.http.Header header : realHeaders) {
            headers = headers.add(header.getName(), header.getValue());
        }

        InputStream stream = getStream(request, response);
        return getResponseCreator().createResponse(status, headers, stream);
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
            entity.consumeContent();
        }
    }
}
