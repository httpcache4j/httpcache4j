package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.HTTPResponse;

import java.time.LocalDateTime;

public interface CacheItem {
    long getTTL();

    boolean isStale(LocalDateTime requestTime);

    long getAge(LocalDateTime dateTime);

    LocalDateTime getCachedTime();

    HTTPResponse getResponse();
}
