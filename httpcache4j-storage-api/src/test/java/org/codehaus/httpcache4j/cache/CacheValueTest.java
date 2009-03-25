package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Status;
import org.codehaus.httpcache4j.Headers;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.HashMap;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Id $
 */
public class CacheValueTest {
    private CacheValue cacheValue;

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFail() {
        new CacheValue(null);
    }

    @Test
    public void testCreate() {
        cacheValue = new CacheValue(new HashMap<Vary, CacheItem>());
        assertNotNull("CacheValue was null",cacheValue);
        assertTrue("CacheValue was not empty", cacheValue.isEmpty());
    }
    
    @Test
    public void testNotEmpty() {
        HashMap<Vary, CacheItem> map = new HashMap<Vary, CacheItem>();
        map.put(new Vary(), new CacheItem(createResponse()));
        cacheValue = new CacheValue(map);
        assertFalse("CacheValue was empty", cacheValue.isEmpty());
    }

    @Test
    public void testRemove() {
        HashMap<Vary, CacheItem> map = new HashMap<Vary, CacheItem>();
        Vary key = new Vary();
        map.put(key, new CacheItem(createResponse()));
        cacheValue = new CacheValue(map);
        assertFalse("CacheValue was empty", cacheValue.isEmpty());
        cacheValue.remove(key);
        assertTrue("CacheValue was not empty", cacheValue.isEmpty());
    }
    
    @Test
    public void testAdd() {
        HashMap<Vary, CacheItem> map = new HashMap<Vary, CacheItem>();
        map.put(new Vary(), new CacheItem(createResponse()));
        cacheValue = new CacheValue(map);
        assertFalse("CacheValue was empty", cacheValue.isEmpty());
        cacheValue.add(createPopulatedVary(), new CacheItem(createResponse()));
        assertEquals("CacheValue was not empty", 2,  cacheValue.size());
    }

    private Vary createPopulatedVary() {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Accept-Language", "en");
        return new Vary(headers);
    }

    private HTTPResponse createResponse() {
        return new HTTPResponse(null, Status.OK, new Headers());
    }
}
