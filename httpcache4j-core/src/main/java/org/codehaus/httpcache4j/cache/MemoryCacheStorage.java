package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.payload.Payload;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class MemoryCacheStorage implements CacheStorage {

    private Map<URI, CacheValue> cache;

    public MemoryCacheStorage() {
        cache = new HashMap<URI, CacheValue>(1000);
    }

    public synchronized void put(URI requestURI, Vary vary, CacheItem cacheItem) {
        if (cache.containsKey(requestURI)) {
            CacheValue value = cache.get(requestURI);
            Map<Vary, CacheItem> variations = new HashMap<Vary, CacheItem>(value.getVariations());
            variations.put(vary, cacheItem);
            value = new CacheValue(variations);
            value.getVariations().put(vary, cacheItem);
        } else {
            cache.put(requestURI, new CacheValue(Collections.singletonMap(vary, cacheItem)));
        }
    }

    public synchronized CacheItem get(HTTPRequest request) {
        CacheValue cacheValue = cache.get(request.getRequestURI());
        if (cacheValue != null) {
            for (Map.Entry<Vary, CacheItem> entry : cacheValue) {
                if (entry.getKey().matches(request)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public synchronized void invalidate(URI uri) {
        if (cache.containsKey(uri)) {
            CacheValue value = cache.remove(uri);
            invalidate(value, null);
        }

    }

    public void invalidate(URI requestURI, CacheItem item) {
        if (cache.containsKey(requestURI) && item != null) {
            CacheValue value = cache.get(requestURI);
            invalidate(value, item);
            if (value.getVariations().isEmpty()) {
                cache.remove(requestURI);
            }
        }
    }

    private void invalidate(CacheValue value, CacheItem item) {
        Vary found = null;
        for (Map.Entry<Vary, CacheItem> entry : value) {
            if (item == null) {
                Payload payload = entry.getValue().getResponse().getPayload();
                if (payload instanceof CleanablePayload) {
                    ((CleanablePayload) payload).clean();
                }
            } else {
                if (entry.getValue() == item) {
                    found = entry.getKey();
                }
            }
        }
        if (found != null) {
            value.getVariations().remove(found);
            Payload payload = item.getResponse().getPayload();
            if (payload instanceof CleanablePayload) {
                ((CleanablePayload) payload).clean();
            }
        }
    }

    public synchronized void clear() {
        for (CacheValue value : cache.values()) {
            invalidate(value, null);
        }
        cache.clear();
    }

    public synchronized int size() {
        return cache.size();
    }
}
