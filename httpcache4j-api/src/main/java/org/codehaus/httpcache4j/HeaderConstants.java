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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Locale;
import java.util.Set;

public final class HeaderConstants {
    public static final String LINK_HEADER = "Link";

    private HeaderConstants() {
    }

    public static final String AUTHORIZATION = "Authorization";
    public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
    public static final String AUTHENTICATION_INFO = "Authentication-Info";
    public static final String PROXY_AUTHENTICATION_INFO = "Proxy-Authentication-Info";
    public static final String ACCEPT = "Accept";
    public static final String ACCEPT_LANGUAGE = "Accept-Language";
    public static final String ACCEPT_CHARSET = "Accept-Charset";
    public static final String AGE = "Age";
    public static final String ALLOW = "Allow";
    public static final String CACHE_CONTROL = "Cache-Control";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_LOCATION = "Content-Location";
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String DATE = "Date";
    public static final String ETAG = "ETag";
    public static final String EXPIRES = "Expires";
    public static final String IF_NON_MATCH = "If-None-Match";
    public static final String IF_MATCH = "If-Match";
    public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    public static final String LAST_MODIFIED = "Last-Modified";
    public static final String LOCATION = "Location";
    public static final String PRAGMA = "Pragma";
    public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
    public static final String SET_COOKIE = "Set-Cookie";
    public static final String VARY = "Vary";
    public static final String WARNING = "Warning";
    public static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    public static final String USER_AGENT = "User-Agent";


    /**
     * The Retry-After response-header field can be used with a 503 (Service Unavailable)
     * response to indicate how long the service is expected to be unavailable to the requesting client.
     * This field MAY also be used with any 3xx (Redirection) response to indicate the minimum time the user-agent
     * is asked wait before issuing the redirected request.
     * The value of this field can be either an HTTP-date or an integer number of seconds (in decimal)
     * after the time of the response
     */
    //TODO: Use this!
    public static final String RETRY_AFTER = "Retry-After";

    public static final String X_CACHE = "X-Cache";

    static final Set<String> AUTHENTICATION_HEADERS = ImmutableSet.of(
            WWW_AUTHENTICATE.toLowerCase(Locale.ENGLISH),
            PROXY_AUTHENTICATE.toLowerCase(Locale.ENGLISH)
    );
}
