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
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.*;

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.resolver.PayloadCreator;
import org.codehaus.httpcache4j.resolver.AbstractResponseResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * An implementation of the ResponseResolver using the Commons HTTPClient (http://hc.apache.org/httpclient-3.x/)
 * <p/>
 * If you need to use SSL, please follow the guide here.
 * http://hc.apache.org/httpclient-3.x/sslguide.html
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
//TODO: add default user agent. This should maybe only be the cache? Maybe the client type as well. Add support for the client of the cache???
public class HTTPClientResponseResolver extends AbstractResponseResolver {
    private final HttpClient client;
    private boolean useRequestChallenge = true;

    public HTTPClientResponseResolver(HttpClient client, PayloadCreator payloadCreator) {
        super(payloadCreator);
        this.client = client;
    }

    public HTTPResponse resolve(HTTPRequest request) {
        HttpMethod method = convertRequest(request);
        try {
            client.executeMethod(method);
            return convertResponse(method);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isUseRequestChallenge() {
        return useRequestChallenge;
    }

    public void setUseRequestChallenge(boolean useRequestChallenge) {
        this.useRequestChallenge = useRequestChallenge;
    }

    private HttpMethod convertRequest(HTTPRequest request) {
        URI requestURI = request.getRequestURI();
        HttpMethod method = getMethod(request.getMethod(), requestURI);
        Headers defaultHeaders = request.getHeaders();
        Headers conditionalHeaders = request.getConditionals().toHeaders();
        Headers preferencesHeaders = request.getPreferences().toHeaders();
        addHeaders(conditionalHeaders, method);
        addHeaders(preferencesHeaders, method);
        //We don't want to add headers more than once.
        addHeaders(removePotentialDuplicates(removePotentialDuplicates(defaultHeaders, conditionalHeaders), preferencesHeaders), method);
        if (isUseRequestChallenge()) {
            Challenge challenge = request.getChallenge();
            if (challenge != null) {
                method.setDoAuthentication(true);
                Credentials usernamePassword = new UsernamePasswordCredentials(challenge.getIdentifier(), challenge.getPassword() != null ? new String(challenge.getPassword()) : null);
                client.getState().setCredentials(new AuthScope(requestURI.getHost(), requestURI.getPort(), AuthScope.ANY_REALM), usernamePassword);
            }
        }
        List<Parameter> parameters = request.getParameters();
        List<NameValuePair> query = new ArrayList<NameValuePair>(parameters.size());
        for (Parameter parameter : parameters) {
            query.add(new NameValuePair(parameter.getName(), parameter.getValue()));
        }
        method.setQueryString(query.toArray(new NameValuePair[query.size()]));
        if (method instanceof EntityEnclosingMethod && request.hasPayload()) {
            InputStream payload = request.getPayload().getInputStream();
            EntityEnclosingMethod carrier = (EntityEnclosingMethod) method;
            if (payload != null) {
                carrier.setRequestEntity(new InputStreamRequestEntity(payload));
            }
            if (!defaultHeaders.hasHeader(HeaderConstants.CONTENT_TYPE)) {
                carrier.setRequestHeader(HeaderConstants.CONTENT_TYPE, request.getPayload().getMimeType().toString());
            }
        }

        method.setDoAuthentication(true);
        return method;
    }

    //TODO: Maybe this should be in the abstract implementation...
    private Headers removePotentialDuplicates(final Headers headersToRemoveFrom, final Headers headers) {
        Map<String, List<org.codehaus.httpcache4j.Header>> map = new HashMap<String, List<org.codehaus.httpcache4j.Header>>(headersToRemoveFrom.getHeadersAsMap());
        for (String key : headers.getHeadersAsMap().keySet()) {
            if (map.containsKey(key)) {
                map.remove(key);
            }
        }
        if (map.isEmpty()) {
            return new Headers();
        }
        return new Headers(map);
    }

    private void addHeaders(Headers headers, HttpMethod method) {
        if (!headers.isEmpty()) {
            for (Map.Entry<String, List<org.codehaus.httpcache4j.Header>> entry : headers) {
                for (org.codehaus.httpcache4j.Header header : entry.getValue()) {
                    method.addRequestHeader(header.getName(), header.getValue());
                }
            }
        }
    }

    private HTTPResponse convertResponse(HttpMethod method) {
        Headers headers = new Headers();
        for (Header header : method.getResponseHeaders()) {
            headers.add(header.getName(), header.getValue());
        }        
        InputStream stream = getInputStream(method);
        Payload payload;
        if (stream != null) {
            payload = getPayloadCreator().createPayload(headers, stream);
        }
        else {
            payload = null;
        }

        return new HTTPResponse(payload, Status.valueOf(method.getStatusCode()), headers);
    }

    private InputStream getInputStream(HttpMethod method) {
        try {
            return method.getResponseBodyAsStream() != null ? new HttpMethodStream(method) : null;
        }
        catch (IOException e) {
            return null;
        }
    }

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

    private static class HttpMethodStream extends InputStream {
        private final HttpMethod method;
        private final InputStream delegate;

        public HttpMethodStream(final HttpMethod method) throws IOException {
            this.method = method;
            this.delegate = method.getResponseBodyAsStream();
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
            delegate.close();
            method.releaseConnection();
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
