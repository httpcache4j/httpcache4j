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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Locale;

/**
 * A collection header utilities.
 */
public final class HeaderUtils {
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
    private static final String NO_CACHE_HEADER_VALUE = "no-cache";
    private static final Header VARY_ALL = new Header(VARY, "*");
    private static List<String> UNCACHEABLE_HEADERS = ImmutableList.of(
            HeaderConstants.SET_COOKIE,
            HeaderConstants.PROXY_AUTHENTICATE,
            HeaderConstants.WWW_AUTHENTICATE
    );

    private HeaderUtils() {
    }

    public static DateTime fromHttpDate(Header header) {
        if (header == null) {
            return null;
        }
        if ("0".equals(header.getValue().trim())) {
            return new DateTime(1970, 1, 1, 0, 0, 0, 0).withZone(DateTimeZone.forID("UTC"));
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


    static Headers cleanForCaching(Headers headers) {
        for (String headerName : UNCACHEABLE_HEADERS) {
            headers = headers.remove(headerName);
        }
        return headers;
    }

    /**
     * From http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.4.
     * <p/>
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
     * @param headers the headers to analyze
     * @return {@code true} if the headers were cacheable, {@code false} if not.
     */
    public static boolean hasCacheableHeaders(Headers headers) {
        if (headers.contains(VARY_ALL)) {
            return false;
        }
        if (headers.hasHeader(CACHE_CONTROL)) {
            final Header header = headers.getFirstHeader(CACHE_CONTROL);
            CacheControl cc = new CacheControl(header);
            if (cc.isNoCache() || cc.isNoStore()) {
               return false; 
            }
        }
        if (headers.hasHeader(PRAGMA)) {
            final Header header = headers.getFirstHeader(PRAGMA);
            if (header.getValue().contains(NO_CACHE_HEADER_VALUE)) {
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
            if (expiresValue.isBefore(dateValue) || expiresValue.equals(dateValue)) {
                return false;
            }
        }
        return true;
    }

    static String removeQuotes(String value) {
        if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public static Header toLinkHeader(List<LinkDirective> linkDirectives) {
        StringBuilder builder = new StringBuilder();
        for (LinkDirective linkDirective : linkDirectives) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(linkDirective);
        }
        return new Header(LINK_HEADER, builder.toString());
    }

    public static List<LinkDirective> toLinkDirectives(Header header) {
        Preconditions.checkArgument(!LINK_HEADER.equals(header.getName()), "This must be a \"Link\" header");
        ImmutableList.Builder<LinkDirective> links = ImmutableList.builder();
        for (Directive directive : header.getDirectives()) {
            if (directive instanceof LinkDirective) {
                links.add((LinkDirective) directive);
            } else {
                links.add(new LinkDirective(directive.getName(), directive.getParameters()));
            }
        }
        return links.build();
    }
}
