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

import java.util.EnumSet;

/** HTTP Status to return after handling a call. */
public enum Status {

    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
    NO_CONTENT(204, "No Content"),
    RESET_CONTENT(205, "Reset Content"),
    PARTIAL_CONTENT(206, "Partial Content"),
    MULTIPLE_CHOICES(300, "Multiple Choices"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    SEE_OTHER(303, "See Other"),
    NOT_MODIFIED(304, "Not Modified"),
    USE_PROXY(305, "Use Proxy"),
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PAYMENT_REQUIRED(402, "Payment Required"), //Reserved for future use!
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    CONFLICT(409, "Conflict"),
    GONE(410, "Gone"),
    LENGTH_REQUIRED(411, "Length Required"),
    PRECONDITION_FAILED(412, "Precondition Failed"),
    REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
    EXPECTATION_FAILED(417, "Expectation Failed"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported");

    private final int code;
    private final String name;

    private Status(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isClientError() {
        return code >= 400 && code <= 500;
    }

    public boolean isServerError() {
        return code >= 500 && code <= 600;
    }

    public boolean isBodyContentAllowed() {
        return !EnumSet.of(RESET_CONTENT, NO_CONTENT, NOT_MODIFIED).contains(this);
    }

    @Override
    public String toString() {
        return getCode() + " " + getName();
    }

    public static Status valueOf(int statusCode) {
        if (CONTINUE.getCode() == statusCode) {
            return CONTINUE;
        }
        else if (SWITCHING_PROTOCOLS.getCode() == statusCode) {
            return SWITCHING_PROTOCOLS;
        }
        else if (OK.getCode() == statusCode) {
            return OK;
        }
        else if (CREATED.getCode() == statusCode) {
            return CREATED;
        }
        else if (ACCEPTED.getCode() == statusCode) {
            return ACCEPTED;
        }
        else if (NON_AUTHORITATIVE_INFORMATION.getCode() == statusCode) {
            return NON_AUTHORITATIVE_INFORMATION;
        }
        else if (NO_CONTENT.getCode() == statusCode) {
            return NO_CONTENT;
        }
        else if (RESET_CONTENT.getCode() == statusCode) {
            return RESET_CONTENT;
        }
        else if (PARTIAL_CONTENT.getCode() == statusCode) {
            return PARTIAL_CONTENT;
        }
        else if (MULTIPLE_CHOICES.getCode() == statusCode) {
            return MULTIPLE_CHOICES;
        }
        else if (MOVED_PERMANENTLY.getCode() == statusCode) {
            return MOVED_PERMANENTLY;
        }
        else if (FOUND.getCode() == statusCode) {
            return FOUND;
        }
        else if (SEE_OTHER.getCode() == statusCode) {
            return SEE_OTHER;
        }
        else if (NOT_MODIFIED.getCode() == statusCode) {
            return NOT_MODIFIED;
        }
        else if (USE_PROXY.getCode() == statusCode) {
            return USE_PROXY;
        }
        else if (TEMPORARY_REDIRECT.getCode() == statusCode) {
            return TEMPORARY_REDIRECT;
        }
        else if (BAD_REQUEST.getCode() == statusCode) {
            return BAD_REQUEST;
        }
        else if (UNAUTHORIZED.getCode() == statusCode) {
            return UNAUTHORIZED;
        }
        else if (PAYMENT_REQUIRED.getCode() == statusCode) {
            return PAYMENT_REQUIRED;
        }
        else if (FORBIDDEN.getCode() == statusCode) {
            return FORBIDDEN;
        }
        else if (NOT_FOUND.getCode() == statusCode) {
            return NOT_FOUND;
        }
        else if (METHOD_NOT_ALLOWED.getCode() == statusCode) {
            return METHOD_NOT_ALLOWED;
        }
        else if (NOT_ACCEPTABLE.getCode() == statusCode) {
            return NOT_ACCEPTABLE;
        }
        else if (PROXY_AUTHENTICATION_REQUIRED.getCode() == statusCode) {
            return PROXY_AUTHENTICATION_REQUIRED;
        }
        else if (REQUEST_TIMEOUT.getCode() == statusCode) {
            return REQUEST_TIMEOUT;
        }
        else if (CONFLICT.getCode() == statusCode) {
            return CONFLICT;
        }
        else if (GONE.getCode() == statusCode) {
            return GONE;
        }
        else if (LENGTH_REQUIRED.getCode() == statusCode) {
            return LENGTH_REQUIRED;
        }
        else if (PRECONDITION_FAILED.getCode() == statusCode) {
            return PRECONDITION_FAILED;
        }
        else if (REQUEST_ENTITY_TOO_LARGE.getCode() == statusCode) {
            return REQUEST_ENTITY_TOO_LARGE;
        }
        else if (REQUEST_URI_TOO_LONG.getCode() == statusCode) {
            return REQUEST_URI_TOO_LONG;
        }
        else if (UNSUPPORTED_MEDIA_TYPE.getCode() == statusCode) {
            return UNSUPPORTED_MEDIA_TYPE;
        }
        else if (REQUESTED_RANGE_NOT_SATISFIABLE.getCode() == statusCode) {
            return REQUESTED_RANGE_NOT_SATISFIABLE;
        }
        else if (EXPECTATION_FAILED.getCode() == statusCode) {
            return EXPECTATION_FAILED;
        }
        else if (INTERNAL_SERVER_ERROR.getCode() == statusCode) {
            return INTERNAL_SERVER_ERROR;
        }
        else if (NOT_IMPLEMENTED.getCode() == statusCode) {
            return NOT_IMPLEMENTED;
        }
        else if (BAD_GATEWAY.getCode() == statusCode) {
            return BAD_GATEWAY;
        }
        else if (SERVICE_UNAVAILABLE.getCode() == statusCode) {
            return SERVICE_UNAVAILABLE;
        }
        else if (GATEWAY_TIMEOUT.getCode() == statusCode) {
            return GATEWAY_TIMEOUT;
        }
        else if (HTTP_VERSION_NOT_SUPPORTED.getCode() == statusCode) {
            return HTTP_VERSION_NOT_SUPPORTED;
        }
        throw new IllegalArgumentException("Unknown status");
    }
}