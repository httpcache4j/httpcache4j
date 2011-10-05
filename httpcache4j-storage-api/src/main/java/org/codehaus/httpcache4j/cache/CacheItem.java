package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.joda.time.DateTime;

public interface CacheItem {
    int getTTL();

    boolean isStale(HTTPRequest request);

    int getAge(HTTPRequest request);

    DateTime getCachedTime();

    HTTPResponse getResponse();
}
