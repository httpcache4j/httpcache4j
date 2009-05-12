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
import org.mockito.Mockito;

import java.net.URI;
import java.io.IOException;

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
    public void testCreate() {
        HTTPCache cache = new HTTPCache(cacheStorage);
        cache.setResolver(responseResolver);
        this.cache = cache;
        testCacheResponse();
    }

    @Test
    public void testRequestWithInvalidConfiguredCache() {
        this.cache = new HTTPCache(cacheStorage);
        try {
            testCacheResponse();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void testCacheResponse() {
        Headers responseHeaders = new Headers();
        responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        doGet(responseHeaders, Status.OK, 1);
    }

    @Test
    public void testNoCacheResponse() {
        doGet(new Headers(), Status.OK, 0);
    }

    @Test
    public void testCacheResponseWithInvalidationPUT() {
        Headers responseHeaders = new Headers();
        responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        doGet(responseHeaders, Status.OK, 1);
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.PUT);
        cache.doCachedRequest(request);
        when(cacheStorage.size()).thenReturn(0);
        assertEquals(0, cacheStorage.size());
    }

    @Test
    public void testPOST() {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.POST);
        cache.doCachedRequest(request);
        when(cacheStorage.size()).thenReturn(0);
        assertEquals(0, cacheStorage.size());
    }

    @Test
    public void testTRACE() {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.TRACE);
        cache.doCachedRequest(request);
        when(cacheStorage.size()).thenReturn(0);
        assertEquals(0, cacheStorage.size());
    }
    @Test
    public void testGetANDTRACE() {
        doGet(new Headers(), Status.OK, 1);
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.TRACE);
        cache.doCachedRequest(request);
        when(cacheStorage.size()).thenReturn(1);
        assertEquals(1, cacheStorage.size());
    }

    @Test
    public void testHEADAndGET() throws IOException {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.HEAD);
        when(responseResolver.resolve(request)).thenReturn(new HTTPResponse(null, Status.OK, new Headers()));
        HTTPResponse response = cache.doCachedRequest(request);
        assertFalse(response.hasPayload());
        assertEquals(0, request.getConditionals().toHeaders().size());
        response = doGet(new Headers(), Status.OK, 1);
        assertTrue("No payload on get", response.hasPayload());
        when(cacheStorage.size()).thenReturn(1);
        assertEquals(1, cacheStorage.size());
    }

    @Test
    public void testHEADWithETagAndGET() throws IOException {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.HEAD);
        Headers headers = new Headers();
        headers.add(new Header("ETag", "\"foo\""));
        HTTPResponse cachedResponse = new HTTPResponse(null, Status.OK, headers);
        when(responseResolver.resolve(request)).thenReturn(cachedResponse);
        when(cacheStorage.get(request)).thenReturn(new CacheItem(cachedResponse));
        HTTPResponse response = cache.doCachedRequest(request);
        assertEquals("Conditionals was set, incorrect", 0, request.getConditionals().toHeaders().size());        
        assertFalse(response.hasPayload());
        response = doGet(new Headers(), Status.OK, 1);
        assertTrue("No payload on get", response.hasPayload());
        when(cacheStorage.size()).thenReturn(1);
        assertEquals(1, cacheStorage.size());
    }

    @Test
    public void testCacheNoneStaleRequestRegression() throws IOException {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.HEAD);
        Headers headers = new Headers();
        headers.add(new Header("Cache-Control", "private, max-age=65000"));
        HTTPResponse cachedResponse = new HTTPResponse(null, Status.OK, headers);

        CacheItem item = new CacheItem(cachedResponse);
        when(cacheStorage.get(request)).thenReturn(item);
        assertFalse(item.isStale());
        cache.doCachedRequest(request);
        verify(responseResolver, never()).resolve(request);        
    }

    private HTTPResponse doGet(Headers responseHeaders, Status status, int numberItemsInCache) {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.GET);
        Payload payload = mock(Payload.class);
        try {
            when(responseResolver.resolve(request)).thenReturn(new HTTPResponse(payload, status, responseHeaders));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        HTTPResponse response = cache.doCachedRequest(request);
        when(cacheStorage.size()).thenReturn(numberItemsInCache);
        assertEquals(numberItemsInCache, cacheStorage.size());
        return response;
    }
}
