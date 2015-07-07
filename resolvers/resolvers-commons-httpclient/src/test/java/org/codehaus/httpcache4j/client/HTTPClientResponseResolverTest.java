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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.payload.ClosedInputStreamPayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a> */
public class HTTPClientResponseResolverTest {
    private HttpClient httpClient;
    private MultiThreadedHttpConnectionManager connectionManager;

    @Before
    public void init() {
        httpClient = mock(HttpClient.class);
        connectionManager = new MultiThreadedHttpConnectionManager();
        when(httpClient.getHttpConnectionManager()).thenReturn(connectionManager);
    }

    @After
    public void after() {
        connectionManager.shutdown();
    }

    @Test
    public void testResolveGETWithNoHeaders() throws IOException {
        HTTPRequest request = new HTTPRequest(URI.create("http://dummy/uri/123"), HTTPMethod.GET);
        final HttpMethod method = mock(GetMethod.class);
        HTTPClientResponseResolver resolver = createResponseResolver(method, Status.valueOf(200), new Header[0]);
        
        HTTPResponse response = resolver.resolve(request);
        assertNotNull("Response was null", response);
        assertEquals(200, response.getStatus().getCode());
        assertEquals(0, response.getHeaders().size());
    }

    @Test(expected = IOException.class)
    public void testResolveFailingGET() throws IOException {
        HTTPRequest request = new HTTPRequest(URI.create("http://dummy/uri/123"), HTTPMethod.GET);
        final HttpMethod method = mock(GetMethod.class);
        HTTPClientResponseResolver resolver = new TestableHTTPClientResponseResolver(method);
        when(httpClient.executeMethod(method)).thenThrow(new IOException("Connection error"));
        resolver.resolve(request);
        fail("No exception was thrown...");
    }

    @Test
    public void testResolvePOSTWithNoHeaders() throws IOException {
        HTTPRequest request = new HTTPRequest(URI.create("http://dummy/uri/123"), HTTPMethod.POST);
        request = request.withPayload(new ClosedInputStreamPayload(new MIMEType("text/plain")));
        final HttpMethod method = mock(PostMethod.class);
        HTTPClientResponseResolver resolver = createResponseResolver(method, Status.valueOf(201), new Header[0]);

        HTTPResponse response = resolver.resolve(request);
        assertNotNull(response);
        assertEquals(201, response.getStatus().getCode());
        assertEquals(0, response.getHeaders().size());
    }

    @Test
    public void testResolvePUTWithNoHeaders() throws IOException {
        HTTPRequest request = new HTTPRequest(URI.create("http://dummy/uri/123"), HTTPMethod.PUT);
        request = request.withPayload(new ClosedInputStreamPayload(new MIMEType("text/plain")));
        final HttpMethod method = mock(PostMethod.class);
        HTTPClientResponseResolver resolver = createResponseResolver(method, Status.valueOf(200), new Header[0]);

        HTTPResponse response = resolver.resolve(request);
        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());
        assertEquals(0, response.getHeaders().size());
    }

    private HTTPClientResponseResolver createResponseResolver(final HttpMethod httpMethod, final Status status, final Header[] headers) {
        try {
            when(httpMethod.getStatusLine()).thenReturn(new org.apache.commons.httpclient.StatusLine(String.format("HTTP/1.1 %s %s\r\n", status.getCode(), status.getName())));
        } catch (HttpException e) {
            throw new RuntimeException(e);
        }
        when(httpMethod.getStatusCode()).thenReturn(status.getCode());
        when(httpMethod.getResponseHeaders()).thenReturn(headers);
        return new TestableHTTPClientResponseResolver(httpMethod);
    }

    private class TestableHTTPClientResponseResolver extends HTTPClientResponseResolver {
        private final HttpMethod httpMethod;

        public TestableHTTPClientResponseResolver(HttpMethod httpMethod) {
            super(HTTPClientResponseResolverTest.this.httpClient);
            this.httpMethod = httpMethod;
        }

        @Override
        protected HttpMethod getMethod(final HTTPMethod method, final URI requestURI) {
            return httpMethod;
        }
    }       
}
