package org.codehaus.httpcache4j.cache;

import java.net.URI;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a> */
public abstract class CacheStorageAbstractTest {
    protected CacheStorage storage;
    protected static final HTTPRequest REQUEST = new HTTPRequest(URI.create("foo"));

    @Before
    public void setup() {
        storage = createCacheStorage();
    }

    protected abstract CacheStorage createCacheStorage();
    protected abstract void afterTest();

    @After
    public void after() {
        if (storage != null) {
            storage.clear();
        }
        afterTest();
    }

    @Test
    public void testPutCacheItem() {
        HTTPResponse response = new HTTPResponse(null, Status.OK, new Headers());
        response = storage.insert(REQUEST, response);
        response.consume();
        assertEquals(1, storage.size());
    }

    @Test
    public void testPutAndGetCacheItem() {
        CacheItem outItem = putAndGet(REQUEST);
        assertNotNull("OutItem was null", outItem);
        assertNotNull("OutItem response was null", outItem.getResponse());
        outItem.getResponse().consume();
    }

    private CacheItem putAndGet(HTTPRequest request) {
        HTTPResponse response = new HTTPResponse(null, Status.OK, new Headers());
        response = storage.insert(REQUEST, response);
        response.consume();
        assertEquals(1, storage.size());
        return storage.get(request);
    }

    @Test
    public void testPutUpdatedCacheItem() {
        CacheItem item = putAndGet(REQUEST);
        item.getResponse().consume();
        HTTPResponse response = new HTTPResponse(null, Status.OK, new Headers());
        HTTPResponse res = storage.update(REQUEST, response);
        res.consume();
        final CacheItem cacheItem = storage.get(REQUEST);
        assertNotSame("Items were the same", cacheItem.getCachedTime(), item.getCachedTime());
        cacheItem.getResponse().consume();
    }

    @Test
    public void testInvalidate() {
        HTTPResponse response = new HTTPResponse(null, Status.OK, new Headers());
        URI requestURI = URI.create("foo");
        HTTPResponse res = storage.insert(REQUEST, response);
        res.consume();
        assertEquals(1, storage.size());
        storage.invalidate(requestURI);
        assertEquals(0, storage.size());
    }
}
