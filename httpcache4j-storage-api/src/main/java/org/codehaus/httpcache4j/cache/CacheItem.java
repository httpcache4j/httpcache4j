/*
 * Copyright (c) 2008, The Codehaus. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.codehaus.httpcache4j.cache;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.Validate;

import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.HeaderUtils;
import org.codehaus.httpcache4j.Header;
import static org.codehaus.httpcache4j.HeaderConstants.*;
import org.codehaus.httpcache4j.Headers;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.DateTimeUtils;

import java.io.Serializable;
import java.util.Map;

/**
 * This is an internal class, and should not be used by clients.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class CacheItem implements Serializable {
    private static final long serialVersionUID = 5891522215450656044L;

    private final DateTime cachedTime;
    private final HTTPResponse response;
    private final int ttl;

    public CacheItem(HTTPResponse response) {
        this(response, new DateTime());
    }

    public CacheItem(HTTPResponse response, DateTime cachedTime) {
        Validate.notNull(response, "Response may not be null");
        Validate.notNull(cachedTime, "CacheTime may not be null");
        this.response = response;
        this.cachedTime = cachedTime;
        this.ttl = getTTL(response, 0);
    }


    public boolean isStale() {
        if (response.hasPayload() && !response.getPayload().isAvailable()) {
            return true;
        }
        return ttl - getAge() <= 0;
    }

    public int getAge() {
        return Seconds.secondsBetween(cachedTime, new DateTime()).getSeconds();
    }

    public static int getTTL(HTTPResponse response, int defaultTTLinSeconds) {
        final Headers headers = response.getHeaders();
        if (headers.hasHeader(CACHE_CONTROL)) {
            Header ccHeader = headers.getFirstHeader(CACHE_CONTROL);
            Map<String, String> directives = ccHeader.getDirectives();
            String maxAgeDirective = directives.get("max-age");
            if (maxAgeDirective != null) {
                int maxAge = NumberUtils.toInt(maxAgeDirective, -1);
                if (maxAge > 0) {
                    return maxAge;
                }
            }
        }
        /**
         * HTTP/1.1 clients and caches MUST treat other invalid date formats, especially including the value "0", as in the past (i.e., "already expired").
         * To mark a response as "already expired," an origin server sends an Expires date that is equal to the Date header value.
         * (See the rules for expiration calculations in section 13.2.4.)
         * To mark a response as "never expires," an origin server sends an Expires date approximately one year from the time the response is sent.
         * HTTP/1.1 servers SHOULD NOT send Expires dates more than one year in the future.
         */
        if (headers.hasHeader(EXPIRES)) {
            DateTime expiryDate = HeaderUtils.fromHttpDate(headers.getFirstHeader(EXPIRES));
            if (expiryDate != null) {
                DateTime date = HeaderUtils.fromHttpDate(headers.getFirstHeader(DATE));
                if (date != null && date.isBefore(expiryDate)) {
                    return Seconds.secondsBetween(date, expiryDate).getSeconds();
                }
            }

        }

        return defaultTTLinSeconds;
    }

    public DateTime getCachedTime() {
        return cachedTime;
    }

    public HTTPResponse getResponse() {
        return response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CacheItem cacheItem = (CacheItem) o;

        if (cachedTime != null ? !cachedTime.equals(cacheItem.cachedTime) : cacheItem.cachedTime != null) {
            return false;
        }
        if (response != null ? !response.equals(cacheItem.response) : cacheItem.response != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = cachedTime != null ? cachedTime.hashCode() : 0;
        result = 31 * result + (response != null ? response.hashCode() : 0);
        return result;
    }
}