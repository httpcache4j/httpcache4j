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
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.payload.ClosedInputStreamPayload;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.Matchers.eq;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.DateTimeUtils;

import java.net.URI;
import java.io.IOException;


/** @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a> */
public class HTTPCacheTest {
    private ResponseResolver responseResolver;
    private CacheStorage cacheStorage;
    private HTTPCache cache;
    private static final URI REQUEST_URI = URI.create("http://some/uri/123");
    private static final URI DUMMY_URI = URI.create("dummy://url");

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
        Headers responseHeaders = new Headers().add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        doGet(responseHeaders, Status.OK, 1);
    }

    @Test
    public void testNoCacheResponse() {
        doGet(new Headers(), Status.OK, 0);
    }


    @Test
    public void testResponseWherePayloadHasBeenRemoved() throws IOException {
        HTTPRequest request = new HTTPRequest(DUMMY_URI);
        Payload payload = mock(Payload.class);
        when(payload.isAvailable()).thenReturn(false);
        CacheItem item = new CacheItem(new HTTPResponse(payload, Status.OK, new Headers()));
        assertTrue("The cached item was not stale", item.isStale(request));
        when(cacheStorage.get(request)).thenReturn(item);
        HTTPResponse resolvedResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, new Headers());
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(resolvedResponse);
        when(cacheStorage.insert(isA(HTTPRequest.class), eq(resolvedResponse))).thenReturn(resolvedResponse);
        HTTPResponse response = cache.doCachedRequest(request);
        assertTrue("None match was not empty",request.getConditionals().getNoneMatch().isEmpty());
        verify(responseResolver, atLeast(1)).resolve(isA(HTTPRequest.class));
        assertTrue("response did not have payload", response.hasPayload());
        assertTrue(response.getPayload().isAvailable());
        //verify(cacheStorage, times(1)).put(eq(REQUEST_URI), eq(new Vary()), any(CacheItem.class));
    }

    @Test
    public void testConditionalRequestWhereResponsePayloadHasBeenRemoved() throws IOException {
        HTTPRequest request = new HTTPRequest(DUMMY_URI);

        Headers headers = new Headers();
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, new DateTime()));
        headers = headers.add(HeaderConstants.ETAG, new Tag("foo", false).format());
        headers = headers.add(HeaderConstants.CACHE_CONTROL, "max-age=10");
        Payload payload = mock(Payload.class);
        when(payload.isAvailable()).thenReturn(false);

        CacheItem item = new CacheItem(new HTTPResponse(payload, Status.OK, headers));
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(item);
        final HTTPResponse resolvedResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, headers);
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(resolvedResponse);
        when(cacheStorage.insert(isA(HTTPRequest.class), eq(resolvedResponse))).thenReturn(resolvedResponse);

        HTTPResponse response = cache.doCachedRequest(request);
        assertTrue("None match was not empty",request.getConditionals().getNoneMatch().isEmpty());
        verify(responseResolver, atLeast(1)).resolve(isA(HTTPRequest.class));
        verify(cacheStorage, times(1)).insert(isA(HTTPRequest.class), eq(resolvedResponse));
        assertTrue("Response did not have a payload", response.hasPayload());
        assertTrue("Payload was not available", response.getPayload().isAvailable());
    }       

    @Test
    public void testExternalConditionalRequestWhereResponsePayloadHasBeenRemoved() throws IOException {
        HTTPRequest request = new HTTPRequest(DUMMY_URI);
        Tag tag = new Tag("foo", false);
        Conditionals conditionals = request.getConditionals().addIfNoneMatch(tag);
        request = request.conditionals(conditionals);
        Headers headers = new Headers();
        headers = headers.add(HeaderConstants.ETAG, tag.format());
        headers = headers.add(HeaderConstants.CACHE_CONTROL, "max-age=10");
        Payload payload = mock(Payload.class);
        when(payload.isAvailable()).thenReturn(false);
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(new CacheItem(new HTTPResponse(payload, Status.OK, headers)));
        HTTPResponse resolvedResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, new Headers());
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(resolvedResponse);
        when(cacheStorage.insert(isA(HTTPRequest.class), eq(resolvedResponse))).thenReturn(resolvedResponse);
        HTTPResponse response = cache.doCachedRequest(request);
//        assertTrue("None match was not empty",request.getConditionals().getNoneMatch().isEmpty());
        verify(responseResolver, atLeast(1)).resolve(isA(HTTPRequest.class));
        assertTrue(response.hasPayload());
        assertTrue(response.getPayload().isAvailable());
        //verify(cacheStorage, times(1)).put(eq(REQUEST_URI), eq(new Vary()), any(CacheItem.class));
    }

    @Test
    public void testConditionalGet() throws IOException {
        HTTPRequest request = new HTTPRequest(URI.create("foo"));
        
        DateTime prev = new DateTime();
        Headers responseHeaders = new Headers();
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, prev));
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.ETAG, "\"1234\""));
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(new CacheItem(new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, responseHeaders)));
        MutableDateTime changedHeader = prev.toMutableDateTime();
        changedHeader.addMinutes(2);

        Headers updatedHeaders = new Headers(responseHeaders);
        updatedHeaders = updatedHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, changedHeader.toDateTime()));
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(new HTTPResponse(null, Status.NOT_MODIFIED, updatedHeaders));
        HTTPResponse response = cache.doCachedRequest(request);
        Assert.assertNotNull("Response was null", response);
        Assert.assertNotNull("The payload was null", response.getPayload());
        Assert.assertNotNull("The payload had a null inputstream", response.getPayload().getInputStream());
    }
    
    @Test
    public void testExternalConditionalGet() throws IOException {
        HTTPRequest request = new HTTPRequest(URI.create("foo"));
        request = request.conditionals(request.getConditionals().addIfNoneMatch(new Tag("1234")));
        Headers responseHeaders = new Headers();
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, new DateTime()));
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.ETAG, "\"1234\""));
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(new CacheItem(new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, responseHeaders)));
        HTTPResponse response = cache.doCachedRequest(request);
        Assert.assertNotNull("Response was null", response);
        Assert.assertNull("The payload was not null", response.getPayload());
        Assert.assertEquals("Wrong status", Status.NOT_MODIFIED, response.getStatus());
    }

    @Test
    public void testConditionalGetWithLastModified() throws IOException {
        HTTPRequest request = new HTTPRequest(URI.create("foo"));        
        Headers responseHeaders = new Headers();
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        DateTime dateHeader = new DateTime();
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, dateHeader));
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.LAST_MODIFIED, dateHeader.minusSeconds(2)));
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(new CacheItem(new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, responseHeaders)));
        Headers updatedHeaders = new Headers(responseHeaders);
        updatedHeaders = updatedHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, dateHeader.plusMinutes(2).toDateTime()));
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(new HTTPResponse(null, Status.NOT_MODIFIED, updatedHeaders));

        HTTPResponse response = cache.doCachedRequest(request);
        Assert.assertNotNull("Response was null", response);
        Assert.assertNotNull("The payload was null", response.getPayload());
        Assert.assertEquals("Wrong status", Status.OK, response.getStatus());
    }
    
    @Test
    public void testExternalConditionalGetWithLastModified() throws IOException {
        DateTime dateHeader = new DateTime();
        DateTime lastModified = dateHeader.minusSeconds(2);
        HTTPRequest request = new HTTPRequest(URI.create("foo"));
        request = request.conditionals(request.getConditionals().ifModifiedSince(lastModified));
        Headers responseHeaders = new Headers();
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, dateHeader));
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.LAST_MODIFIED, lastModified));
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(new CacheItem(new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, responseHeaders)));
        
        HTTPResponse response = cache.doCachedRequest(request);
        Assert.assertNotNull("Response was null", response);
        Assert.assertNull("The payload was not null", response.getPayload());
        Assert.assertEquals("Wrong status", Status.NOT_MODIFIED, response.getStatus());
    }

    @Test
    public void testExternalConditionalGetWitchDoesNotMatch() throws IOException {
        HTTPRequest request = new HTTPRequest(URI.create("foo"));
        request.getConditionals().addIfNoneMatch(new Tag("12345"));
        Headers responseHeaders = new Headers();
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, new DateTime()));
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.ETAG, "\"1234\""));
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(new CacheItem(new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, responseHeaders)));
        HTTPResponse response = cache.doCachedRequest(request);
        Assert.assertNotNull("Response was null", response);
        Assert.assertNotNull("The payload was null", response.getPayload());
        Assert.assertEquals("Wrong status", Status.OK, response.getStatus());
    }

    @Test
    public void testUnconditionalGET() throws IOException {
        HTTPRequest request = new HTTPRequest(REQUEST_URI);
        request.getConditionals().addIfNoneMatch(Tag.ALL);
        Headers responseHeaders = new Headers();
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, new DateTime()));
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.ETAG, "\"1234\""));
        HTTPResponse resolvedResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, responseHeaders);
        when(responseResolver.resolve(request)).thenReturn(resolvedResponse);
        when(cacheStorage.insert(eq(request), eq(resolvedResponse))).thenReturn(resolvedResponse);
        HTTPResponse response = cache.doCachedRequest(request);
        verify(cacheStorage, times(1)).insert(eq(request), eq(resolvedResponse));
        Assert.assertNotNull("Response was null", response);
        Assert.assertEquals("Wrong status", Status.OK, response.getStatus());
        Assert.assertNotNull("The payload was null", response.getPayload());
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
    public void testPOST() throws IOException {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.POST);
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(new HTTPResponse(null, Status.CREATED, new Headers()));
        cache.doCachedRequest(request);
        when(cacheStorage.size()).thenReturn(0);
        assertEquals(0, cacheStorage.size());
    }

    @Test
    public void testTRACE() throws IOException {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.TRACE);
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(new HTTPResponse(null, Status.CREATED, new Headers()));
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
    public void testHEADWithETagAndGETWithItemInCache() throws IOException {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.HEAD);
        Headers headers = new Headers();
        headers = headers.add(new Header("ETag", "\"foo\""));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, new DateTime()));
        HTTPResponse cachedResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, headers);
        //when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(cachedResponse);
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(new CacheItem(cachedResponse) {
            @Override
            public boolean isStale(HTTPRequest request) {
                return false;
            }
        });
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
        headers = headers.add(new Header("Cache-Control", "private, max-age=65000"));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, new DateTime()));
        HTTPResponse cachedResponse = new HTTPResponse(null, Status.OK, headers);

        CacheItem item = new CacheItem(cachedResponse);
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(item);
        assertFalse(item.isStale(request));
        cache.doCachedRequest(request);
        verify(responseResolver, never()).resolve(request);        
    }

    @Test
    public void testCacheWithRequestAllowingStaleReponse() throws IOException {
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2010, 2, 3, 10, 0, 0,0).getMillis());
        Headers headers = new Headers();
        headers = headers.add(new Header("Cache-Control", "private, max-age=5"));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, new DateTime()));
        HTTPResponse cachedResponse = new HTTPResponse(null, Status.OK, headers);

        CacheItem item = new CacheItem(cachedResponse);
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2010, 2, 3, 10, 0, 10, 0).getMillis());
        HTTPRequest request = new HTTPRequest(REQUEST_URI).headers(new Headers().add(HeaderConstants.CACHE_CONTROL, "max-stale=10"));
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(item);
        assertTrue("Item was not stale",item.isStale(request));
        HTTPResponse response = cache.doCachedRequest(request);
        verify(responseResolver, never()).resolve(request);
        assertTrue("No warn header", response.getHeaders().hasHeader("warning"));
        DateTimeUtils.setCurrentMillisSystem();
    }



    @Test
    public void testUpdateHeadersFromResolvedUpdatesHeaders() throws Exception {
        Headers headers = new Headers().add("Link", "<foo>");
        Headers updatedHeaders = new Headers().add("Link", "<bar>");
        Headers merged = dryCleanHeaders(headers, updatedHeaders);
        assertEquals(1, merged.getHeaders("Link").size());
        assertEquals("<bar>", merged.getFirstHeaderValue("Link"));
    }


    @Test
    public void testUpdateHeadersFromResolvedRetainsHeaders() throws Exception {
        Headers headers = new Headers().add("Link", "<foo>");
        Headers updatedHeaders = new Headers().add("Allow", "GET");
        Headers merged = dryCleanHeaders(headers, updatedHeaders);
        assertEquals("<foo>", merged.getFirstHeaderValue("link"));
    }

    @Test
    public void updateHeadersFromResolvedOverwritesHeadersThereCanOnlyBeOneOf() throws Exception {
        Headers headers = new Headers().add(HeaderUtils.toHttpDate("Date", new DateTime())).add("Allow", "GET, PUT");
        Headers updatedHeaders = new Headers().add("Allow", "GET").add(HeaderUtils.toHttpDate("Date", new DateTime()));
        Headers merged = dryCleanHeaders(headers, updatedHeaders);
        assertEquals(1, merged.getHeaders("Date").size());
        assertEquals(1, merged.getHeaders("Allow").size());
    }


    private Headers dryCleanHeaders(Headers headers, Headers updatedHeaders) {
        CacheStorage storage = new NullCacheStorage();
        cache = new HTTPCache(storage, responseResolver);
        cacheStorage = storage;
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.GET);
        HTTPResponse cachedResponse = new HTTPResponse(null, Status.OK, headers);
        CacheItem item = new CacheItem(cachedResponse);
        HTTPResponse updatedResponse = new HTTPResponse(null, Status.OK, updatedHeaders);
        HTTPResponse washedResponse = cache.updateHeadersFromResolved(request, item, updatedResponse);
        return washedResponse.getHeaders();
    }

    private HTTPResponse doGet(Headers responseHeaders, Status status, int numberItemsInCache) {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.GET);
        Payload payload = mock(Payload.class);
        try {
            HTTPResponse resolvedResponse = new HTTPResponse(payload, status, responseHeaders);
            when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(resolvedResponse);
            when(cacheStorage.insert(isA(HTTPRequest.class), eq(resolvedResponse))).thenReturn(resolvedResponse);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        HTTPResponse response = cache.doCachedRequest(request);
        when(cacheStorage.size()).thenReturn(numberItemsInCache);
        assertEquals(numberItemsInCache, cacheStorage.size());
        return response;
    }
}
