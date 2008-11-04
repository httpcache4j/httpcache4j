package org.codehaus.httpcache4j;

public interface HeaderConstants {
    String ETAG = "ETag";
    String LAST_MODIFIED = "Last-Modified";
    String IF_NON_MATCH = "If-None-Match";
    String IF_MATCH = "If-Match";
    String IF_MODIFIED_SINCE = "If-Modified-Since";
    String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
    String CONTENT_TYPE = "Content-Type";
    String EXPIRES = "Expires";
    String PRAGMA = "Pragma";
    String CACHE_CONTROL = "Cache-Control";
    String VARY = "Vary";
    String ACCEPT = "Accept";
    String ACCEPT_LANGUAGE = "Accept-Language";
    String ACCEPT_CHARSET = "Accept-Charset";
    String ALLOW = "Allow";

    /**
     * The Retry-After response-header field can be used with a 503 (Service Unavailable)
     * response to indicate how long the service is expected to be unavailable to the requesting client.
     * This field MAY also be used with any 3xx (Redirection) response to indicate the minimum time the user-agent
     * is asked wait before issuing the redirected request.
     * The value of this field can be either an HTTP-date or an integer number of seconds (in decimal)
     * after the time of the response
     */
    String RETRY_AFTER = "Retry-After";
}