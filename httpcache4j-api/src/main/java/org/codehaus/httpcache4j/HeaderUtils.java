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

package org.codehaus.httpcache4j;

import static org.codehaus.httpcache4j.HeaderConstants.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

/**
 * A collection header utilities.
 */
public final class HeaderUtils {
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
    private static final String NO_STORE_HEADER_VALUE = "no-store";
    private static final String NO_CACHE_HEADER_VALUE = "no-cache";

    private HeaderUtils() {
    }

    public static DateTime fromHttpDate(Header header) {
        if (header == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormat.forPattern(PATTERN_RFC1123).
                withZone(DateTimeZone.forID("UTC")).
                withLocale(Locale.US);
        DateTime formattedDate = null;
        try {
            formattedDate = formatter.parseDateTime(header.getValue());
        } catch (IllegalArgumentException ignore) {            
        }

        return formattedDate;
    }


    public static Header toHttpDate(String headerName, DateTime time) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(PATTERN_RFC1123).
                withZone(DateTimeZone.forID("UTC")).
                withLocale(Locale.US);
        return new Header(headerName, formatter.print(time));
    }

    public static long getHeaderAsDate(Header header) {
        try {
            DateTime dateTime = fromHttpDate(header);
            if (dateTime != null) {
                return dateTime.getMillis();
            }
        }
        catch (Exception e) {
            return -1;
        }

        return -1;
    }

    /**
     * From http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.4.
     * 
     * Unless specifically constrained by a cache-control (section 14.9) directive,
     * a caching system MAY always store a successful response (see section 13.8)
     * as a cache entry, MAY return it without validation if it is fresh, and MAY return it
     * after successful validation. If there is neither a cache validator nor an explicit
     * expiration time associated with a response, we do not expect it to be cached,
     * but certain caches MAY violate this expectation
     * (for example, when little or no network connectivity is available).
     * A client can usually detect that such a response was taken from a cache by comparing
     * the Date header to the current time.
     *
     *  
     * @param headers the headers to analyze
     * @return {@code true} if the headers were cacheable, {@code false} if not. 
     */
    public static boolean hasCacheableHeaders(Headers headers) {
        if (headers.contains(new Header(VARY, "*")) || !headers.hasHeader(DATE)) {
            return false;
        }
        if (headers.hasHeader(CACHE_CONTROL)) {
            final Header header = headers.getFirstHeader(CACHE_CONTROL);
            if (header.getValue().contains(NO_STORE_HEADER_VALUE) || header.getValue().contains(NO_CACHE_HEADER_VALUE)) {
                return false;
            }
        }
        if (headers.hasHeader(EXPIRES)) {
          Header expires = headers.getFirstHeader(EXPIRES);
          DateTime expiresValue = HeaderUtils.fromHttpDate(expires);
          Header date = headers.getFirstHeader(DATE);
          if (expiresValue == null || date == null) {
            return false;
          }
          DateTime dateValue = HeaderUtils.fromHttpDate(date);
          if (expiresValue.isBefore(dateValue)) {
            return false;
          }
        }
        //To cache we need a cache-validator.
        return headers.getFirstHeader(ETAG) != null
                || headers.getFirstHeader(LAST_MODIFIED) != null;
    }
}