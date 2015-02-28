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

import java.net.URI;
import java.io.IOException;
import java.time.LocalDateTime;


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
        this.cache = new HTTPCache(cacheStorage, responseResolver);
        testCacheResponse();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRequestWithInvalidConfiguredCache() {
        this.cache = new HTTPCache(cacheStorage, null);
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
        CacheItem item = new DefaultCacheItem(new HTTPResponse(payload, Status.OK, new Headers()));
        assertTrue("The cached item was not stale", item.isStale(LocalDateTime.now()));
        when(cacheStorage.get(request)).thenReturn(item);
        HTTPResponse resolvedResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, new Headers());
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(resolvedResponse);
        when(cacheStorage.insert(isA(HTTPRequest.class), eq(resolvedResponse))).thenReturn(resolvedResponse);
        HTTPResponse response = cache.execute(request);
        assertTrue("None match was not empty",request.getHeaders().getConditionals().getNoneMatch().isEmpty());
        verify(responseResolver, atLeast(1)).resolve(isA(HTTPRequest.class));
        assertTrue("response did not have payload", response.hasPayload());
        assertTrue(response.getPayload().isAvailable());
        //verify(cacheStorage, times(1)).put(eq(REQUEST_URI), eq(new Vary()), any(CacheItem.class));
    }

    @Test
    public void testConditionalRequestWhereResponsePayloadHasBeenRemoved() throws IOException {
        HTTPRequest request = new HTTPRequest(DUMMY_URI);

        Headers headers = new Headers();
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, LocalDateTime.now()));
        headers = headers.add(HeaderConstants.ETAG, new Tag("foo", false).format());
        headers = headers.add(HeaderConstants.CACHE_CONTROL, "max-age=10");
        Payload payload = mock(Payload.class);
        when(payload.isAvailable()).thenReturn(false);

        CacheItem item = new DefaultCacheItem(new HTTPResponse(payload, Status.OK, headers));
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(item);
        final HTTPResponse resolvedResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, headers);
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(resolvedResponse);
        when(cacheStorage.insert(isA(HTTPRequest.class), eq(resolvedResponse))).thenReturn(resolvedResponse);

        HTTPResponse response = cache.execute(request);
        assertTrue("None match was not empty",request.getHeaders().getConditionals().getNoneMatch().isEmpty());
        verify(responseResolver, atLeast(1)).resolve(isA(HTTPRequest.class));
        verify(cacheStorage, times(1)).insert(isA(HTTPRequest.class), eq(resolvedResponse));
        assertTrue("Response did not have a payload", response.hasPayload());
        assertTrue("Payload was not available", response.getPayload().isAvailable());
    }       

    @Test
    public void testExternalConditionalRequestWhereResponsePayloadHasBeenRemoved() throws IOException {
        HTTPRequest request = new HTTPRequest(DUMMY_URI);
        Tag tag = new Tag("foo", false);
        request = request.addIfNoneMatch(tag);
        Headers headers = new Headers();
        headers = headers.add(HeaderConstants.ETAG, tag.format());
        headers = headers.add(HeaderConstants.CACHE_CONTROL, "max-age=10");
        Payload payload = mock(Payload.class);
        when(payload.isAvailable()).thenReturn(false);
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(new DefaultCacheItem(new HTTPResponse(payload, Status.OK, headers)));
        HTTPResponse resolvedResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, new Headers());
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(resolvedResponse);
        when(cacheStorage.insert(isA(HTTPRequest.class), eq(resolvedResponse))).thenReturn(resolvedResponse);
        HTTPResponse response = cache.execute(request);
//        assertTrue("None match was not empty",request.getConditionals().getNoneMatch().isEmpty());
        verify(responseResolver, atLeast(1)).resolve(isA(HTTPRequest.class));
        assertTrue(response.hasPayload());
        assertTrue(response.getPayload().isAvailable());
        //verify(cacheStorage, times(1)).put(eq(REQUEST_URI), eq(new Vary()), any(CacheItem.class));
    }

    @Test
    public void testConditionalGet() throws IOException {
        HTTPRequest request = new HTTPRequest(URI.create("foo"));
        
        LocalDateTime prev = LocalDateTime.now();
        Headers responseHeaders = new Headers();
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, prev));
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.ETAG, "\"1234\""));
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(new DefaultCacheItem(new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, responseHeaders)));

        Headers updatedHeaders = new Headers(responseHeaders);
        updatedHeaders = updatedHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, prev.plusMinutes(2)));
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(new HTTPResponse(null, Status.NOT_MODIFIED, updatedHeaders));
        HTTPResponse response = cache.execute(request);
        Assert.assertNotNull("Response was null", response);
        Assert.assertNotNull("The payload was null", response.getPayload());
        Assert.assertNotNull("The payload had a null inputstream", response.getPayload().getInputStream());
    }
    
    @Test
    public void testExternalConditionalGet() throws IOException {
        HTTPRequest request = new HTTPRequest(URI.create("foo"));
        request = request.addIfNoneMatch(new Tag("1234"));
        Headers responseHeaders = new Headers();
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, LocalDateTime.now()));
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.ETAG, "\"1234\""));
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(new DefaultCacheItem(new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, responseHeaders)));
        HTTPResponse response = cache.execute(request);
        Assert.assertNotNull("Response was null", response);
        Assert.assertNull("The payload was not null", response.getPayload());
        Assert.assertEquals("Wrong status", Status.NOT_MODIFIED, response.getStatus());
    }

    @Test
    public void testConditionalGetWithLastModified() throws IOException {
        HTTPRequest request = new HTTPRequest(URI.create("foo"));        
        Headers responseHeaders = new Headers();
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        LocalDateTime dateHeader = LocalDateTime.now();
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, dateHeader));
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.LAST_MODIFIED, dateHeader.minusSeconds(2)));
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(new DefaultCacheItem(new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, responseHeaders)));
        Headers updatedHeaders = new Headers(responseHeaders);
        updatedHeaders = updatedHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, dateHeader.plusMinutes(2)));
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(new HTTPResponse(null, Status.NOT_MODIFIED, updatedHeaders));

        HTTPResponse response = cache.execute(request);
        Assert.assertNotNull("Response was null", response);
        Assert.assertNotNull("The payload was null", response.getPayload());
        Assert.assertEquals("Wrong status", Status.OK, response.getStatus());
    }
    
    @Test
    public void testExternalConditionalGetWithLastModified() throws IOException {
        LocalDateTime dateHeader = LocalDateTime.now();
        LocalDateTime lastModified = dateHeader.minusSeconds(2);
        HTTPRequest request = new HTTPRequest(URI.create("foo"));
        request = request.withIfModifiedSince(lastModified);
        Headers responseHeaders = new Headers();
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, dateHeader));
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.LAST_MODIFIED, lastModified));
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(new DefaultCacheItem(new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, responseHeaders)));
        
        HTTPResponse response = cache.execute(request);
        Assert.assertNotNull("Response was null", response);
        Assert.assertNull("The payload was not null", response.getPayload());
        Assert.assertEquals("Wrong status", Status.NOT_MODIFIED, response.getStatus());
    }

    @Test
    public void testExternalConditionalGetWitchDoesNotMatch() throws IOException {
        HTTPRequest request = new HTTPRequest(URI.create("foo"));
        request = request.addIfNoneMatch(new Tag("12345"));
        Headers responseHeaders = new Headers();
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, LocalDateTime.now()));
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.ETAG, "\"1234\""));
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(new DefaultCacheItem(new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, responseHeaders)));
        HTTPResponse response = cache.execute(request);
        Assert.assertNotNull("Response was null", response);
        Assert.assertNotNull("The payload was null", response.getPayload());
        Assert.assertEquals("Wrong status", Status.OK, response.getStatus());
    }

    @Test
    public void testUnconditionalGET() throws IOException {
        HTTPRequest request = new HTTPRequest(REQUEST_URI);
        request = request.addIfNoneMatch(Tag.ALL);
        Headers responseHeaders = new Headers();
        responseHeaders = responseHeaders.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, LocalDateTime.now()));
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        responseHeaders = responseHeaders.add(new Header(HeaderConstants.ETAG, "\"1234\""));
        HTTPResponse resolvedResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, responseHeaders);
        when(responseResolver.resolve(request)).thenReturn(resolvedResponse);
        when(cacheStorage.insert(eq(request), eq(resolvedResponse))).thenReturn(resolvedResponse);
        HTTPResponse response = cache.execute(request);
        verify(cacheStorage, times(1)).insert(eq(request), eq(resolvedResponse));
        Assert.assertNotNull("Response was null", response);
        Assert.assertEquals("Wrong status", Status.OK, response.getStatus());
        Assert.assertNotNull("The payload was null", response.getPayload());
    }

    @Test
    public void testEndToEndReload() throws IOException {
        String initialETagVal = "\"1\"";
        String updatedETagVal = "\"2\"";

        HTTPRequest request = new HTTPRequest(REQUEST_URI).addHeader(HeaderConstants.CACHE_CONTROL, "no-cache");

        // Setup initial response
        Headers initialResponseHeaders = new Headers();
        initialResponseHeaders = initialResponseHeaders.add(new Header(HeaderConstants.ETAG, initialETagVal));
        initialResponseHeaders = initialResponseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        HTTPResponse initialResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, initialResponseHeaders);

        // Setup response returned on end-to-end reload request
        Headers updatedResponseHeaders = new Headers();
        updatedResponseHeaders = updatedResponseHeaders.add(new Header(HeaderConstants.ETAG, updatedETagVal));
        HTTPResponse updatedResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, updatedResponseHeaders);

        // Setup cache mock
        when(cacheStorage.insert(eq(request), eq(initialResponse))).thenReturn(initialResponse);
        when(cacheStorage.insert(eq(request), eq(updatedResponse))).thenReturn(updatedResponse);

        // When attempting to look the request up in the cache, return the initial response
        when(cacheStorage.get(request)).thenReturn(new DefaultCacheItem(initialResponse));

        // When attempting to resolve the request against the origin server, return the updated response
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(updatedResponse);

        HTTPResponse response = cache.execute(request);

        Assert.assertEquals("Wrong status", Status.OK, response.getStatus());
        Assert.assertFalse("Got the cached response instead of the updated one.", Tag.parse(initialETagVal).equals(response.getHeaders().getETag()));
        Assert.assertEquals("Did not get the updated response (ETag does not match)", Tag.parse(updatedETagVal), response.getHeaders().getETag());
    }

    @Test
    public void testCacheResponseWithInvalidationPUT() {
        Headers responseHeaders = new Headers();
        responseHeaders.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        doGet(responseHeaders, Status.OK, 1);
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.PUT);
        cache.execute(request);
        when(cacheStorage.size()).thenReturn(0);
        assertEquals(0, cacheStorage.size());
    }

    @Test
    public void testPOST() throws IOException {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.POST);
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(new HTTPResponse(null, Status.CREATED, new Headers()));
        cache.execute(request);
        when(cacheStorage.size()).thenReturn(0);
        assertEquals(0, cacheStorage.size());
    }

    @Test
    public void testTRACE() throws IOException {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.TRACE);
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(new HTTPResponse(null, Status.CREATED, new Headers()));
        cache.execute(request);
        when(cacheStorage.size()).thenReturn(0);
        assertEquals(0, cacheStorage.size());
    }
    @Test
    public void testGetANDTRACE() {
        doGet(new Headers(), Status.OK, 1);
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.TRACE);
        cache.execute(request);
        when(cacheStorage.size()).thenReturn(1);
        assertEquals(1, cacheStorage.size());
    }

    @Test
    public void testHEADAndGET() throws IOException {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.HEAD);
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(new HTTPResponse(null, Status.OK, new Headers()));
        HTTPResponse response = cache.execute(request);
        assertFalse(response.hasPayload());
        assertEquals(0, request.getHeaders().size());
        when(cacheStorage.size()).thenReturn(1);
        assertEquals(1, cacheStorage.size());
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
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, LocalDateTime.now()));
        HTTPResponse cachedResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, headers);
        //when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(cachedResponse);
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(new DefaultCacheItem(cachedResponse) {
            @Override
            public boolean isStale(LocalDateTime requestTime) {
                return false;
            }
        });
        HTTPResponse response = cache.execute(request);
        assertEquals("Conditionals was set, incorrect", 0, request.getHeaders().getConditionals().toHeaders().size());
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
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, LocalDateTime.now()));
        HTTPResponse cachedResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, headers);

        CacheItem item = new DefaultCacheItem(cachedResponse);
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(item);
        assertFalse(item.isStale(LocalDateTime.now()));
        HTTPResponse response = cache.execute(request);
        assertNull(response.getPayload());
        verify(responseResolver, never()).resolve(request);        
    }

    @Test
    public void testCacheWithRequestAllowingStaleReponse() throws IOException {
        LocalDateTime now = LocalDateTime.of(2010, 2, 3, 10, 0, 0, 0);
        Headers headers = new Headers();
        headers = headers.add(new Header("Cache-Control", "private, max-age=5"));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, now));
        HTTPResponse cachedResponse = new HTTPResponse(null, Status.OK, headers);

        CacheItem item = new DefaultCacheItem(cachedResponse, now);
        now = LocalDateTime.of(2010, 2, 3, 10, 0, 10, 0);
        HTTPRequest request = new HTTPRequest(REQUEST_URI).headers(new Headers().add(HeaderConstants.CACHE_CONTROL, "max-stale=10"));
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(item);
        assertTrue("Item was not stale",item.isStale(now));
        HTTPResponse response = cache.execute(request);
        verify(responseResolver, never()).resolve(request);
        assertTrue("No warn header", response.getHeaders().hasHeader("warning"));
    }

    @Test
    public void makeSureHEADRequestInvalidatesCacheIfNot304() throws Exception {
        LocalDateTime base = LocalDateTime.of(2010, 2, 3, 10, 0, 0, 0);
        Headers headers = new Headers();
        headers = headers.add(new CacheControl.Builder().maxAge(50).withPrivate().build().toHeader());
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, base));
        HTTPResponse cachedResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, headers);
        CacheItem item = new DefaultCacheItem(cachedResponse) {
            @Override
            public boolean isStale(LocalDateTime requestTime) {
                return true;
            }
        };
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(item);
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.HEAD);
        base = base.plusHours(1);
        when(responseResolver.resolve(request)).thenReturn(new HTTPResponse(null, Status.NOT_FOUND, new Headers().withDate(base)));
        HTTPResponse response = cache.execute(request);
        verify(cacheStorage, atLeastOnce()).invalidate(REQUEST_URI);
        assertNull(response.getPayload());
    }

    @Test
    public void makeSureHEADRequestUpdatesCacheI304() throws Exception {
        LocalDateTime base = LocalDateTime.of(2010, 2, 3, 10, 0, 0, 0);
        Headers headers = new Headers();
        CacheControl cacheControl = new CacheControl.Builder().maxAge(50).withPrivate().build();
        headers = headers.add(cacheControl.toHeader());
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, base));
        headers = headers.withETag(new Tag("foo"));
        HTTPResponse cachedResponse = new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, headers);
        CacheItem item = new DefaultCacheItem(cachedResponse) {
            @Override
            public boolean isStale(LocalDateTime requestTime) {
                return true;
            }
        };
        when(cacheStorage.get(isA(HTTPRequest.class))).thenReturn(item);

        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.HEAD);
        base = base.plusHours(1);

        HTTPResponse HEADResponse = new HTTPResponse(null, Status.NOT_MODIFIED, new Headers().withDate(base).withCacheControl(cacheControl));
        when(responseResolver.resolve(request)).thenReturn(HEADResponse);
        when(cacheStorage.update(isA(HTTPRequest.class), isA(HTTPResponse.class))).thenReturn(new HTTPResponse(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM), Status.OK, HEADResponse.getHeaders()));
        HTTPResponse response = cache.execute(request);
        verify(cacheStorage, never()).invalidate(REQUEST_URI);
        verify(cacheStorage, atLeastOnce()).update(isA(HTTPRequest.class), isA(HTTPResponse.class));
        assertNull(response.getPayload());
    }



    @Test
    public void testUpdateHeadersFromResolvedUpdatesHeaders() throws Exception {
        Headers headers = new Headers().add("Link", "<foo>");
        Headers updatedHeaders = new Headers().add("Link", "<bar>");
        Headers merged = dryCleanHeaders(headers, updatedHeaders);
        assertEquals(1, merged.getHeaders("Link").size());
        assertEquals("<bar>", merged.getFirstHeaderValue("Link").get());
    }


    @Test
    public void testUpdateHeadersFromResolvedRetainsHeaders() throws Exception {
        Headers headers = new Headers().add("Link", "<foo>");
        Headers updatedHeaders = new Headers().add("Allow", "GET");
        Headers merged = dryCleanHeaders(headers, updatedHeaders);
        assertEquals("<foo>", merged.getFirstHeaderValue("link").get());
    }

    @Test
    public void updateHeadersFromResolvedOverwritesHeadersThereCanOnlyBeOneOf() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        Headers headers = new Headers().add(HeaderUtils.toHttpDate("Date", now)).add("Allow", "GET, PUT");
        Headers updatedHeaders = new Headers().add("Allow", "GET").add(HeaderUtils.toHttpDate("Date", now));
        Headers merged = dryCleanHeaders(headers, updatedHeaders);
        assertEquals(1, merged.getHeaders("Date").size());
        assertEquals(1, merged.getHeaders("Allow").size());
    }

    @Test
    public void testContentLocationAndLocationUriInvalidatedOnPUT() throws IOException {
        testContentLocationAndLocationInvalidationOnSuccess(HTTPMethod.PUT);
    }

    @Test
    public void testContentLocationAndLocationUriInvalidatedOnPOST() throws IOException {
        testContentLocationAndLocationInvalidationOnSuccess(HTTPMethod.POST);
    }

    @Test
    public void testContentLocationAndLocationUriInvalidatedOnDELETE() throws IOException {
        testContentLocationAndLocationInvalidationOnSuccess(HTTPMethod.DELETE);
    }

    @Test
    public void testNeitherContentLocationNorLocationInvalidatedIfHostNotSameAsRequest() throws IOException {
        URI requestUri = URI.create("http://host1/some");
        URI contentLocationUri = URI.create("http://host2/some/content/location");
        URI locationUri = URI.create("http://host3/some/location");

        HTTPRequest request = new HTTPRequest(requestUri, HTTPMethod.POST);
        Headers responseHeaders = new Headers().
                add(new Header(HeaderConstants.CONTENT_LOCATION, contentLocationUri.toString())).
                add(new Header(HeaderConstants.LOCATION, locationUri.toString()));
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(new HTTPResponse(null, Status.CREATED, responseHeaders));

        cache.execute(request);

        verify(cacheStorage, never()).invalidate(contentLocationUri);
        verify(cacheStorage, never()).invalidate(locationUri);
    }

    private void testContentLocationAndLocationInvalidationOnSuccess(HTTPMethod httpMethod) throws IOException {
        URI requestUri = URI.create("http://foo/some");
        URI contentLocationUri = URI.create("http://foo/some/content/location");
        URI locationUri = URI.create("http://foo/some/location");

        HTTPRequest request = new HTTPRequest(requestUri, httpMethod);
        Headers responseHeaders = new Headers().
                add(new Header(HeaderConstants.CONTENT_LOCATION, contentLocationUri.toString())).
                add(new Header(HeaderConstants.LOCATION, locationUri.toString()));
        when(responseResolver.resolve(isA(HTTPRequest.class))).thenReturn(new HTTPResponse(null, Status.CREATED, responseHeaders));

        cache.execute(request);

        verify(cacheStorage).invalidate(contentLocationUri);
        verify(cacheStorage).invalidate(locationUri);
    }


    private Headers dryCleanHeaders(Headers headers, Headers updatedHeaders) {
        CacheStorage storage = new NullCacheStorage();
        cache = new HTTPCache(storage, responseResolver);
        cacheStorage = storage;
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.GET);
        HTTPResponse cachedResponse = new HTTPResponse(null, Status.OK, headers);
        CacheItem item = new DefaultCacheItem(cachedResponse);
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
        HTTPResponse response = cache.execute(request);
        when(cacheStorage.size()).thenReturn(numberItemsInCache);
        assertEquals(numberItemsInCache, cacheStorage.size());
        return response;
    }
}
