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

import com.google.common.collect.Sets;

import java.util.Set;

/** HTTP Status to return after handling a call. */
public final class Status implements Comparable<Status> {

    public static Status CONTINUE = new Status(100, "Continue");
    public static Status SWITCHING_PROTOCOLS = new Status(101, "Switching Protocols");
    public static Status OK = new Status(200, "OK");
    public static Status CREATED = new Status(201, "Created");
    public static Status ACCEPTED = new Status(202, "Accepted");
    public static Status NON_AUTHORITATIVE_INFORMATION = new Status(203, "Non-Authoritative Information");
    public static Status NO_CONTENT = new Status(204, "No Content");
    public static Status RESET_CONTENT = new Status(205, "Reset Content");
    public static Status PARTIAL_CONTENT = new Status(206, "Partial Content");
    public static Status MULTIPLE_CHOICES = new Status(300, "Multiple Choices");
    public static Status MOVED_PERMANENTLY = new Status(301, "Moved Permanently");
    public static Status FOUND = new Status(302, "Found");
    public static Status SEE_OTHER = new Status(303, "See Other");
    public static Status NOT_MODIFIED = new Status(304, "Not Modified");
    public static Status USE_PROXY = new Status(305, "Use Proxy");
    public static Status TEMPORARY_REDIRECT = new Status(307, "Temporary Redirect");
    public static Status BAD_REQUEST = new Status(400, "Bad Request");
    public static Status UNAUTHORIZED = new Status(401, "Unauthorized");
    public static Status PAYMENT_REQUIRED = new Status(402, "Payment Required"); //Reserved for future use!
    public static Status FORBIDDEN = new Status(403, "Forbidden");
    public static Status NOT_FOUND = new Status(404, "Not Found");
    public static Status METHOD_NOT_ALLOWED = new Status(405, "Method Not Allowed");
    public static Status NOT_ACCEPTABLE = new Status(406, "Not Acceptable");
    public static Status PROXY_AUTHENTICATION_REQUIRED = new Status(407, "Proxy Authentication Required");
    public static Status REQUEST_TIMEOUT = new Status(408, "Request Timeout");
    public static Status CONFLICT = new Status(409, "Conflict");
    public static Status GONE = new Status(410, "Gone");
    public static Status LENGTH_REQUIRED = new Status(411, "Length Required");
    public static Status PRECONDITION_FAILED = new Status(412, "Precondition Failed");
    public static Status REQUEST_ENTITY_TOO_LARGE = new Status(413, "Request Entity Too Large");
    public static Status REQUEST_URI_TOO_LONG = new Status(414, "Request-URI Too Long");
    public static Status UNSUPPORTED_MEDIA_TYPE = new Status(415, "Unsupported Media Type");
    public static Status REQUESTED_RANGE_NOT_SATISFIABLE = new Status(416, "Requested Range Not Satisfiable");
    public static Status EXPECTATION_FAILED = new Status(417, "Expectation Failed");
    public static Status INTERNAL_SERVER_ERROR = new Status(500, "Internal Server Error");
    public static Status NOT_IMPLEMENTED = new Status(501, "Not Implemented");
    public static Status BAD_GATEWAY = new Status(502, "Bad Gateway");
    public static Status SERVICE_UNAVAILABLE = new Status(503, "Service Unavailable");
    public static Status GATEWAY_TIMEOUT = new Status(504, "Gateway Timeout");
    public static Status HTTP_VERSION_NOT_SUPPORTED = new Status(505, "HTTP Version Not Supported");

    private static final Set<Status> STATUSES_WITHOUT_BODY = Sets.newHashSet(RESET_CONTENT, NO_CONTENT, NOT_MODIFIED);
    
    private final int code;
    private final String name;

    public Status(int code, String name) {
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
        return code >= 400 && code < 500;
    }

    public boolean isServerError() {
        return code >= 500 && code < 600;
    }

    public boolean isBodyContentAllowed() {
        return !STATUSES_WITHOUT_BODY.contains(this);
    }

    public int compareTo(Status o) {
        if (code > o.code) return 1;
        if (code == o.code) return 0;
        return -1;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Status status = (Status) o;

        if (code != status.code) {
            return false;
        }
        if (name != null ? !name.equals(status.name) : status.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public final int hashCode() {
        int result = code;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
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
        return new Status(statusCode, "Unknown");
    }
}