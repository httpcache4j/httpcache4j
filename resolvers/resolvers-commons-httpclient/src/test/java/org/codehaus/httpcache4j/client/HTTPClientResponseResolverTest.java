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
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.resolver.ResponseCreator;
import org.codehaus.httpcache4j.resolver.AbstractResponseCreator;
import org.codehaus.httpcache4j.resolver.StoragePolicy;
import org.junit.Before;
import org.junit.Test;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public class HTTPClientResponseResolverTest {
    private HttpClient httpClient;
    private ResponseCreator creator;
    
    @Before
    public void init() {
        httpClient = mock(HttpClient.class);
        creator = new AbstractResponseCreator(StoragePolicy.NULL) {
            @Override
            protected Payload createCachedPayload(HTTPRequest request, Headers responseHeaders, InputStream stream, MIMEType type) throws IOException {
                return null;
            }
        };
    }

    @Test
    public void testResolveGETWithNoHeaders() throws IOException {
        HTTPRequest request = new HTTPRequest(URI.create("http://dummy/uri/123"), HTTPMethod.GET);
        final HttpMethod method = mock(GetMethod.class);
        HTTPClientResponseResolver resolver = createResponseResolver(method, 200, new Header[0]);
        
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
        final Payload payload = mock(Payload.class);
        request = request.payload(payload);
        when(payload.getMimeType()).thenReturn(new MIMEType("text/plain"));
        final HttpMethod method = mock(PostMethod.class);
        HTTPClientResponseResolver resolver = createResponseResolver(method, 201, new Header[0]);

        HTTPResponse response = resolver.resolve(request);
        assertNotNull(response);
        assertEquals(201, response.getStatus().getCode());
        assertEquals(0, response.getHeaders().size());
    }

    @Test
    public void testResolvePUTWithNoHeaders() throws IOException {
        HTTPRequest request = new HTTPRequest(URI.create("http://dummy/uri/123"), HTTPMethod.PUT);
        final Payload payload = mock(Payload.class);
        request = request.payload(payload);
        when(payload.getMimeType()).thenReturn(new MIMEType("text/plain"));
        final HttpMethod method = mock(PostMethod.class);
        HTTPClientResponseResolver resolver = createResponseResolver(method, 200, new Header[0]);

        HTTPResponse response = resolver.resolve(request);
        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());
        assertEquals(0, response.getHeaders().size());
    }

    private HTTPClientResponseResolver createResponseResolver(final HttpMethod httpMethod, final int statusCode, final Header[] headers) {
        when(httpMethod.getStatusCode()).thenReturn(statusCode);
        when(httpMethod.getResponseHeaders()).thenReturn(headers);        
        return new TestableHTTPClientResponseResolver(httpMethod);
    }

    private class TestableHTTPClientResponseResolver extends HTTPClientResponseResolver {
        private final HttpMethod httpMethod;

        public TestableHTTPClientResponseResolver(HttpMethod httpMethod) {
            super(HTTPClientResponseResolverTest.this.httpClient, HTTPClientResponseResolverTest.this.creator);
            this.httpMethod = httpMethod;
        }

        @Override
        HttpMethod getMethod(final HTTPMethod method, final URI requestURI) {
            return httpMethod;
        }
    }       
}
