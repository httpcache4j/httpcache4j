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

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.codehaus.httpcache4j.HTTPMethod;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.HeaderUtils;
import org.codehaus.httpcache4j.util.NullInputStream;
import org.joda.time.DateTime;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class HTTPClientResponseResolverTest {

    private HttpClient client;

    @Before
    public void setUp() {
        client = mock(HttpClient.class);
    }


    @Test
    public void testSimpleGET() throws IOException {
        HTTPClientResponseResolver resolver = new TestableResolver();
        HttpResponse mockedResponse = mock(HttpResponse.class);
        when(mockedResponse.getAllHeaders()).thenReturn(new org.apache.http.Header[0]);
        when(mockedResponse.getStatusLine()).thenReturn(new BasicStatusLine(new HttpVersion(1, 1),200, "OK"));
        when(client.execute(Mockito.<HttpUriRequest>anyObject())).thenReturn(mockedResponse);
        HTTPResponse response = resolver.resolve(new HTTPRequest(URI.create("http://www.vg.no")));
        assertNotNull("Response was null", response);
        assertEquals("Wrong header size", 0, response.getHeaders().size());
        assertFalse("Response did have payload", response.hasPayload());
    }

    @Test
    public void testNotSoSimpleGET() throws IOException {
        HTTPClientResponseResolver resolver = new TestableResolver();
        HttpResponse mockedResponse = mock(HttpResponse.class);
        when(mockedResponse.getAllHeaders()).thenReturn(new org.apache.http.Header[] {new BasicHeader("Date", HeaderUtils.toHttpDate("Date", new DateTime()).getValue())});
        when(mockedResponse.getStatusLine()).thenReturn(new BasicStatusLine(new HttpVersion(1, 1),200, "OK"));
        when(mockedResponse.getEntity()).thenReturn(new InputStreamEntity(new NullInputStream(1), 1));
        when(client.execute(Mockito.<HttpUriRequest>anyObject())).thenReturn(mockedResponse);
        HTTPResponse response = resolver.resolve(new HTTPRequest(URI.create("http://www.vg.no")));
        assertNotNull("Response was null", response);
        assertEquals("Wrong header size", 1, response.getHeaders().size());
        assertTrue("Response did not have payload", response.hasPayload());
    }

    @Test
    public void testPUT() throws IOException {
        HTTPClientResponseResolver resolver = new TestableResolver();
        HttpResponse mockedResponse = mock(HttpResponse.class);
        when(mockedResponse.getAllHeaders()).thenReturn(new org.apache.http.Header[] {new BasicHeader("Date", HeaderUtils.toHttpDate("Date", new DateTime()).getValue())});
        when(mockedResponse.getStatusLine()).thenReturn(new BasicStatusLine(new HttpVersion(1, 1), 200, "OK"));
        when(mockedResponse.getEntity()).thenReturn(null);
        when(client.execute(Mockito.<HttpUriRequest>anyObject())).thenReturn(mockedResponse);
        HTTPResponse response = resolver.resolve(new HTTPRequest(URI.create("http://www.vg.no"), HTTPMethod.PUT));
        assertNotNull("Response was null", response);
        assertEquals("Wrong header size", 1, response.getHeaders().size());
        assertFalse("Response did have payload", response.hasPayload());
    }

    private class TestableResolver extends HTTPClientResponseResolver {
        public TestableResolver() {
            super(HTTPClientResponseResolverTest.this.client);
        }

        @Override
        protected HttpUriRequest getMethod(HTTPMethod method, URI requestURI) {
            HttpUriRequest request = mock(HttpUriRequest.class);
            when(request.getMethod()).thenReturn(method.toString());
            when(request.getURI()).thenReturn(requestURI);
            return request;
        }
    }
}
