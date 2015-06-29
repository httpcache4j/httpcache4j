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

import org.codehaus.httpcache4j.util.OptionalUtils;
import org.codehaus.httpcache4j.util.Preconditions;

import static org.codehaus.httpcache4j.HeaderConstants.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * A collection header utilities.
 */
public final class HeaderUtils {
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
    private static final String NO_CACHE_HEADER_VALUE = "no-cache";
    private static final Header VARY_ALL = new Header(VARY, "*");
    private static List<String> UNCACHEABLE_HEADERS = Arrays.asList(
            HeaderConstants.SET_COOKIE,
            HeaderConstants.PROXY_AUTHENTICATE,
            HeaderConstants.WWW_AUTHENTICATE
    );

    private HeaderUtils() {
    }

    public static Optional<LocalDateTime> fromHttpDate(Header header) {
        if (header == null) {
            return null;
        }
        if ("0".equals(header.getValue().trim())) {
            return Optional.of(LocalDateTime.of(LocalDate.of(1970, 1, 1), LocalTime.of(0, 0, 0, 0)));
        }
        return parseGMTString(header.getValue());
    }

    public static Optional<LocalDateTime> parseGMTString(String value) {
        DateTimeFormatter formatter = getFormatter();
        try {
            return Optional.of(LocalDateTime.from(formatter.parse(value)));
        } catch (DateTimeParseException ignore) {
        }

        return Optional.empty();
    }


    public static Header toHttpDate(String headerName, LocalDateTime time) {
        return new Header(headerName, toGMTString(time));
    }

    public static String toGMTString(LocalDateTime time) {
        DateTimeFormatter formatter = getFormatter();
        return formatter.format(time);
    }

    private static DateTimeFormatter getFormatter() {
        return DateTimeFormatter.ofPattern(PATTERN_RFC1123).
                withZone(ZoneId.of("UTC")).
                withLocale(Locale.US);
    }

    public static long getHeaderAsDate(Header header) {
        try {
            Optional<LocalDateTime> dateTime = fromHttpDate(header);
            if (dateTime.isPresent()) {
                return dateTime.get().toInstant(ZoneOffset.UTC).toEpochMilli();
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
        if (headers.contains(CACHE_CONTROL)) {
            Optional<CacheControl> cc = headers.getCacheControl();
            if (OptionalUtils.exists(cc, (cc2 -> cc2.isNoCache() || cc2.isNoStore()))) {
               return false; 
            }
        }
        if (headers.contains(PRAGMA)) {
            Optional<String> header = headers.getFirstHeaderValue(PRAGMA);
            if (OptionalUtils.exists(header, s -> s.contains(NO_CACHE_HEADER_VALUE))) {
                return false;
            }
        }
        if (headers.contains(EXPIRES)) {
            Optional<LocalDateTime> expires = headers.getExpires();
            Optional<LocalDateTime> date = headers.getDate();
            if (!expires.isPresent() || !date.isPresent()) {
                return false;
            }
            if (OptionalUtils.exists(expires, e -> e.isBefore(date.get())) || OptionalUtils.exists(expires, e -> e.isEqual(date.get()))) {
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

        return header.getDirectives().stream().
                filter(d -> d instanceof LinkDirective).
                map(d -> (LinkDirective) d).
                collect(Collectors.toList());
    }
}
