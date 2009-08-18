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

import java.util.List;
import java.util.Locale;

import org.codehaus.httpcache4j.preference.Preference;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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
        return formatter.parseDateTime(header.getValue());
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

    public static boolean hasCacheableHeaders(Headers headers) {
        if (headers.contains(new Header(VARY, "*"))) {
            return false;
        }
        if (headers.hasHeader(CACHE_CONTROL)){
            final Header header = headers.getFirstHeader(CACHE_CONTROL);
            if (header.getValue().contains(NO_STORE_HEADER_VALUE) || header.getValue().contains(NO_CACHE_HEADER_VALUE)) {
                return false;
            }
            return header.getDirectives().containsKey("max-age");
        }
        if (headers.contains(new Header(PRAGMA, NO_CACHE_HEADER_VALUE)) || headers.contains(new Header(PRAGMA, NO_STORE_HEADER_VALUE))) {
            return false;
        }
        return headers.getFirstHeader(ETAG) != null
                || headers.getFirstHeader(EXPIRES) != null
                || headers.getFirstHeader(LAST_MODIFIED) != null;
    }
}