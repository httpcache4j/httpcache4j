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

import com.google.common.base.Preconditions;
import org.codehaus.httpcache4j.*;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

/**
 * This is an internal class, and should not be used by clients.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class DefaultCacheItem implements CacheItem {

    protected DateTime cachedTime;
    protected HTTPResponse response;
    protected int ttl;

    public DefaultCacheItem(HTTPResponse response) {
        this(response, new DateTime());
    }

    public DefaultCacheItem(HTTPResponse response, DateTime cachedTime) {
        this.response = Preconditions.checkNotNull(response, "Response may not be null");
        this.cachedTime = Preconditions.checkNotNull(cachedTime, "CacheTime may not be null");
        this.ttl = getTTL(response, 0);
    }

    public int getTTL() {
        return ttl;
    }

    public boolean isStale(HTTPRequest request) {
        if (response.hasPayload() && !response.getPayload().isAvailable()) {
            return true;
        }
        return ttl - getAge(request) <= 0;
    }

    public int getAge(HTTPRequest request) {
        return Seconds.secondsBetween(cachedTime, request.getRequestTime()).getSeconds();
    }

    public static int getTTL(HTTPResponse response, int defaultTTLinSeconds) {
        final CacheControl cc = response.getCacheControl();
        if (cc != null) {
            int maxAge = cc.getMaxAge();
            if (maxAge > 0) {
                return maxAge;
            }
        }
        /**
         * HTTP/1.1 clients and caches MUST treat other invalid date formats, especially including the value "0", as in the past (i.e., "already expired").
         * To mark a response as "already expired," an origin server sends an Expires date that is equal to the Date header value.
         * (See the rules for expiration calculations in section 13.2.4.)
         * To mark a response as "never expires," an origin server sends an Expires date approximately one year from the time the response is sent.
         * HTTP/1.1 servers SHOULD NOT send Expires dates more than one year in the future.
         */
        if (response.getExpires() != null) {
            DateTime expiryDate = response.getExpires();
            if (expiryDate != null) {
                DateTime date = response.getDate();
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

        DefaultCacheItem cacheItem = (DefaultCacheItem) o;

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
