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

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.util.OptionalUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * This is an internal class, and should not be used by clients.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class DefaultCacheItem implements CacheItem {

    protected LocalDateTime cachedTime;
    protected HTTPResponse response;
    protected long ttl;

    public DefaultCacheItem(HTTPResponse response) {
        this(response, LocalDateTime.now());
    }

    public DefaultCacheItem(HTTPResponse response, LocalDateTime cachedTime) {
        this.response = Objects.requireNonNull(response, "Response may not be null");
        this.cachedTime = Objects.requireNonNull(cachedTime, "CacheTime may not be null");
        this.ttl = getTTL(response, 0);
    }

    public long getTTL() {
        return ttl;
    }

    public boolean isStale(LocalDateTime requestTime) {
        if (response.hasPayload() && !response.getPayload().isAvailable()) {
            return true;
        }
        return ttl - getAge(requestTime) <= 0;
    }

    public long getAge(LocalDateTime requestTime) {
        return Duration.between(cachedTime, requestTime).getSeconds();
    }

    public static long getTTL(HTTPResponse response, int defaultTTLinSeconds) {
        final Optional<CacheControl> cc = response.getHeaders().getCacheControl();

        if (cc.isPresent()) {
            int maxAge = cc.get().getMaxAge();
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
        Optional<LocalDateTime> expires = response.getHeaders().getExpires();
        if (expires.isPresent()) {
            LocalDateTime expiryDate = expires.get();
            Optional<LocalDateTime> date = response.getHeaders().getDate();
            if (OptionalUtils.exists(date, dt -> dt.isBefore(expiryDate))) {
                return Duration.between(date.get(), expiryDate).getSeconds();
            }
        }

        return defaultTTLinSeconds;
    }

    public LocalDateTime getCachedTime() {
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
