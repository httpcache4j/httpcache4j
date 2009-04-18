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

public interface HeaderConstants {
    String ACCEPT = "Accept";
    String ACCEPT_LANGUAGE = "Accept-Language";
    String ACCEPT_CHARSET = "Accept-Charset";
    String ALLOW = "Allow";
    String CACHE_CONTROL = "Cache-Control";
    String CONTENT_TYPE = "Content-Type";
    String DATE = "Date";
    String ETAG = "ETag";
    String EXPIRES = "Expires";
    String IF_NON_MATCH = "If-None-Match";
    String IF_MATCH = "If-Match";
    String IF_MODIFIED_SINCE = "If-Modified-Since";
    String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    String LAST_MODIFIED = "Last-Modified";
    String LOCATION = "Location";
    String PRAGMA = "Pragma";
    String VARY = "Vary";
    String WARNING = "Warning";

    /**
     * The Retry-After response-header field can be used with a 503 (Service Unavailable)
     * response to indicate how long the service is expected to be unavailable to the requesting client.
     * This field MAY also be used with any 3xx (Redirection) response to indicate the minimum time the user-agent
     * is asked wait before issuing the redirected request.
     * The value of this field can be either an HTTP-date or an integer number of seconds (in decimal)
     * after the time of the response
     */
    //TODO: Use this!
    String RETRY_AFTER = "Retry-After";
}