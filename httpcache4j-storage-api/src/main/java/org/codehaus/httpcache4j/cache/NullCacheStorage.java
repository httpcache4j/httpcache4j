package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.HTTPRequest;

import java.net.URI;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public class NullCacheStorage implements CacheStorage {
    public void put(final URI requestURI, final Vary vary, final CacheItem cacheItem) {
    }

    public CacheItem get(final HTTPRequest request) {
        return null;
    }

    public void invalidate(final URI uri) {
    }

    public void clear() {
    }

    public int size() {
        return 0;
    }

    public void invalidate(final URI requestURI, final CacheItem item) {        
    }
}
