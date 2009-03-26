package org.codehaus.httpcache4j.cache;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

import java.net.URI;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public abstract class CacheStorageAbstractTest {
    protected CacheStorage storage;

    @Before
    public void setup() {
        storage = createCacheStorage();
    }

    protected abstract CacheStorage createCacheStorage();
    protected abstract void afterTest();

    @After
    public void after() {
        storage.clear();
        afterTest();
    }

    @Test
    public void testPutCacheItem() {
        HTTPResponse response = new HTTPResponse(null, Status.OK, new Headers());
        CacheItem item = new CacheItem(response);
        storage.put(URI.create("foo"), new Vary(), item);
        assertEquals(1, storage.size());
    }

    @Test
    public void testPutAndGetCacheItem() {
        HTTPResponse response = new HTTPResponse(null, Status.OK, new Headers());
        CacheItem item = new CacheItem(response);
        HTTPRequest request = Mockito.mock(HTTPRequest.class);
        URI requestURI = URI.create("foo");
        Mockito.when(request.getRequestURI()).thenReturn(requestURI);
        storage.put(requestURI, new Vary(), item);
        assertEquals(1, storage.size());
        CacheItem outItem = storage.get(request);
        assertEquals(item, outItem);
    }

    @Test
    public void testPutUpdatedCacheItem() {
        testPutAndGetCacheItem();
        HTTPRequest request = Mockito.mock(HTTPRequest.class);
        URI requestURI = URI.create("foo");
        Mockito.when(request.getRequestURI()).thenReturn(requestURI);
        HTTPResponse response = new HTTPResponse(null, Status.OK, new Headers());
        CacheItem item = new CacheItem(response);
        final CacheItem cacheItem = storage.get(request);
        storage.put(requestURI, new Vary(), item);
        assertNotSame("Items were the same", cacheItem, item);
    }

    @Test
    public void testInvalidate() {
        HTTPResponse response = new HTTPResponse(null, Status.OK, new Headers());
        CacheItem item = new CacheItem(response);
        URI requestURI = URI.create("foo");
        storage.put(requestURI, new Vary(), item);
        assertEquals(1, storage.size());
        storage.invalidate(requestURI);
        assertEquals(0, storage.size());
    }

    @Test
    public void testInvalidateSingleItem() {
        HTTPResponse response = new HTTPResponse(null, Status.OK, new Headers());
        CacheItem item = new CacheItem(response);
        URI requestURI = URI.create("foo");
        storage.put(requestURI, new Vary(), item);
        assertEquals(1, storage.size());
        storage.invalidate(requestURI, item);
        assertEquals(0, storage.size());
    }
}
