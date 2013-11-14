package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.HTTPResponse;
import org.joda.time.DateTime;

public interface CacheItem {
    int getTTL();

    boolean isStale(DateTime requestTime);

    int getAge(DateTime dateTime);

    DateTime getCachedTime();

    HTTPResponse getResponse();
}
