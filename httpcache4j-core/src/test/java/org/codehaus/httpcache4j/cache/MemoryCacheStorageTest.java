package org.codehaus.httpcache4j.cache;

import static junit.framework.Assert.assertEquals;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

import java.net.URI;
import java.util.HashMap;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public class MemoryCacheStorageTest {
    private CacheStorage storage;

    @Before
    public void setup() {
        storage = new MemoryCacheStorage();
    }

    @After
    public void after() {
        storage.clear();
    }

    @Test
    public void testPutCacheItem() {
        CacheItem item = mock(CacheItem.class);
        HTTPResponse response = new HTTPResponse(null, Status.OK, new Headers());
        stub(item.getResponse()).toReturn(response);
        storage.put(URI.create("foo"), new Vary(new HashMap<String, String>()), item);
        assertEquals(1, storage.size());
    }

    @Test
    public void testPutAndGetCacheItem() {
        CacheItem item = mock(CacheItem.class);
        HTTPResponse response = new HTTPResponse(null, Status.OK, new Headers());
        stub(item.getResponse()).toReturn(response);
        HTTPRequest request = mock(HTTPRequest.class);
        URI requestURI = URI.create("foo");
        stub(request.getRequestURI()).toReturn(requestURI);
        storage.put(requestURI, new Vary(new HashMap<String, String>()), item);
        assertEquals(1, storage.size());
        CacheItem outItem = storage.get(request);
        assertEquals(item, outItem);
    }

    @Test
    public void testInvalidate() {
        CacheItem item = mock(CacheItem.class);
        HTTPResponse response = new HTTPResponse(null, Status.OK, new Headers());
        stub(item.getResponse()).toReturn(response);
        URI requestURI = URI.create("foo");
        storage.put(requestURI, new Vary(new HashMap<String, String>()), item);
        assertEquals(1, storage.size());
        storage.invalidate(requestURI);
        assertEquals(0, storage.size());
    }

    @Test
    public void testInvalidateSingleItem() {
        CacheItem item = mock(CacheItem.class);
        HTTPResponse response = new HTTPResponse(null, Status.OK, new Headers());
        stub(item.getResponse()).toReturn(response);
        URI requestURI = URI.create("foo");
        storage.put(requestURI, new Vary(new HashMap<String, String>()), item);
        assertEquals(1, storage.size());
        storage.invalidate(requestURI, item);
        assertEquals(0, storage.size());
    }
}
