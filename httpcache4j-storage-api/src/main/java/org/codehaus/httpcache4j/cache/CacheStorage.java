package org.codehaus.httpcache4j.cache;


import org.codehaus.httpcache4j.HTTPRequest;

import java.net.URI;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public interface CacheStorage {

    void put(URI requestURI, Vary vary, CacheItem cacheItem);

    CacheItem get(final HTTPRequest request);

    void invalidate(URI uri);

    void clear();

    int size();

    void invalidate(URI requestURI, CacheItem item);
}