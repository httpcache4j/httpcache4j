package org.codehaus.httpcache4j.client;

import org.codehaus.httpcache4j.resolver.PayloadCreator;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPMethod;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.payload.Payload;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public class HTTPClientResponseResolverTest {
    private HttpClient httpClient;
    private PayloadCreator creator;

    @Before
    public void init() {
        httpClient = mock(HttpClient.class);
        creator = mock(PayloadCreator.class);
    }

    @Test
    public void testResolveGETWithNoHeaders() {
        HTTPRequest request = new HTTPRequest(URI.create("http://dummy/uri/123"), HTTPMethod.GET);
        final HttpMethod method = mock(GetMethod.class);
        HTTPClientResponseResolver resolver = createResponseResolver(method, 200, new Header[0]);

        HTTPResponse response = resolver.resolve(request);
        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());
        assertEquals(0, response.getHeaders().size());
    }

    @Test
    public void testResolvePOSTWithNoHeaders() {
        HTTPRequest request = new HTTPRequest(URI.create("http://dummy/uri/123"), HTTPMethod.POST);
        final Payload payload = mock(Payload.class);
        request.setPayload(payload);
        stub(payload.getMimeType()).toReturn(new MIMEType("text/plain"));
        final HttpMethod method = mock(PostMethod.class);
        HTTPClientResponseResolver resolver = createResponseResolver(method, 201, new Header[0]);

        HTTPResponse response = resolver.resolve(request);
        assertNotNull(response);
        assertEquals(201, response.getStatus().getCode());
        assertEquals(0, response.getHeaders().size());
    }

    @Test
    public void testResolvePUTWithNoHeaders() {
        HTTPRequest request = new HTTPRequest(URI.create("http://dummy/uri/123"), HTTPMethod.PUT);
        final Payload payload = mock(Payload.class);
        request.setPayload(payload);
        stub(payload.getMimeType()).toReturn(new MIMEType("text/plain"));
        final HttpMethod method = mock(PostMethod.class);
        HTTPClientResponseResolver resolver = createResponseResolver(method, 200, new Header[0]);

        HTTPResponse response = resolver.resolve(request);
        assertNotNull(response);
        assertEquals(200, response.getStatus().getCode());
        assertEquals(0, response.getHeaders().size());
    }

    private HTTPClientResponseResolver createResponseResolver(final HttpMethod httpMethod, final int statusCode, final Header[] headers) {
        stub(httpMethod.getStatusCode()).toReturn(statusCode);
        stub(httpMethod.getResponseHeaders()).toReturn(headers);

        return new HTTPClientResponseResolver(httpClient, creator) {
            @Override
            HttpMethod getMethod(final HTTPMethod method, final URI requestURI) {
                return httpMethod;
            }
        };
    }
}
