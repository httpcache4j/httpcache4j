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

import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.HTTPUtils;
import org.codehaus.httpcache4j.Header;
import static org.codehaus.httpcache4j.HeaderConstants.CACHE_CONTROL;
import static org.codehaus.httpcache4j.HeaderConstants.EXPIRES;
import org.codehaus.httpcache4j.Headers;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Map;

/**
 * This is an interal class, and should not be subclassed or used by clients.
 * It is not final because of test cases.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class CacheItem implements Serializable {
    private final DateTime cachedTime;
    private final HTTPResponse response;

    public CacheItem(HTTPResponse response) {
        this.response = response;
        cachedTime = new DateTime();
    }

    public boolean isStale() {
        if (response.getPayload() != null && !response.getPayload().isAvailable()) {
            return true;
        }
        Headers headers = response.getHeaders();
        long now = new DateTime().getMillis();
        if (headers.hasHeader(CACHE_CONTROL)) {
            Header ccHeader = headers.getFirstHeader(CACHE_CONTROL);
            Map<String, String> directives = ccHeader.getDirectives();
            String maxAgeDirective = directives.get("max-age");
            if (maxAgeDirective != null) {
                int maxAge = NumberUtils.toInt(maxAgeDirective, -1);
                long age = now - cachedTime.getMillis();
                long remainingLife = (maxAge * 1000) - age;
                if (maxAge == -1 || remainingLife <= 0) {
                    return true;
                }
            }
        }
        /**
         *  HTTP/1.1 clients and caches MUST treat other invalid date formats, especially including the value "0", as in the past (i.e., "already expired").
         * To mark a response as "already expired," an origin server sends an Expires date that is equal to the Date header value.
         * (See the rules for expiration calculations in section 13.2.4.)
         * To mark a response as "never expires," an origin server sends an Expires date approximately one year from the time the response is sent.
         * HTTP/1.1 servers SHOULD NOT send Expires dates more than one year in the future.
         */
        if (headers.hasHeader(EXPIRES)) {
            long expiryDate = HTTPUtils.getHeaderAsDate(headers.getFirstHeader(EXPIRES));
            if (expiryDate == -1 || now >= expiryDate) {
                return true;
            }
        }

        return false;
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