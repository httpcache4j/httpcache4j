package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.resolver.ResponseResolver;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPMethod;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Status;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.Header;
import org.codehaus.httpcache4j.HeaderConstants;
import org.codehaus.httpcache4j.payload.Payload;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.URI;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public class HTTPCacheTest {
    private ResponseResolver responseResolver;
    private CacheStorage cacheStorage;
    private HTTPCache cache;
    private static final URI REQUEST_URI = URI.create("http://some/uri/123");

    @Before
    public void init() {
        responseResolver = mock(ResponseResolver.class);
        cacheStorage = mock(CacheStorage.class);
        cache = new HTTPCache(cacheStorage, responseResolver);
    }

    @Test
    public void testCacheResponse() {
        Headers responseHeaders = new Headers();
        responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private;max-age=60"));
        doGet(responseHeaders,  1);
    }

    @Test
    public void testNoCacheResponse() {
        doGet(new Headers(), 0);
    }

    @Test
    public void testCacheResponseWithInvalidationPUT() {
        Headers responseHeaders = new Headers();
        responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private;max-age=60"));
        doGet(responseHeaders, 1);
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.PUT);
        cache.doCachedRequest(request);
        stub(cacheStorage.size()).toReturn(0);
        assertEquals(0, cacheStorage.size());
    }

    private HTTPResponse doGet(Headers responseHeaders, int numberItemsInCache) {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.GET);
        stub(responseResolver.resolve(request)).toReturn(new HTTPResponse(mock(Payload.class),  Status.OK, responseHeaders));
        HTTPResponse response = cache.doCachedRequest(request);
        stub(cacheStorage.size()).toReturn(numberItemsInCache);
        assertEquals(numberItemsInCache, cacheStorage.size());
        return response;
    }
}
