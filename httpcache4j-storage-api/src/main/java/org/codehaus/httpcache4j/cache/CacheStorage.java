package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.HTTPRequest;

import java.net.URI;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
//TODO: Document.
//TODO: Implement a version that is persistent.
public interface CacheStorage {

    void put(URI requestURI, Vary vary, CacheItem cacheItem);

    CacheItem get(final HTTPRequest request);

    void invalidate(URI uri);

    void clear();

    int size();

    //TODO: maybe not required any more...
    void invalidate(URI requestURI, CacheItem item);
}