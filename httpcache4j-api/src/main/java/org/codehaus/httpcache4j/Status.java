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

/** Status to return after handling a call. */
public final class Status extends Metadata {

    public static final Status CONTINUE = new Status(100, "Continue");
    public static final Status SWITCHING_PROTOCOLS = new Status(101, "Switching Protocols");
    public static final Status OK = new Status(200, "OK");
    public static final Status CREATED = new Status(201, "Created");
    public static final Status ACCEPTED = new Status(202, "Accepted");
    public static final Status NON_AUTHORITATIVE_INFORMATION = new Status(203, "Non-Authoritative Information");
    public static final Status NO_CONTENT = new Status(204, "No Content");
    public static final Status RESET_CONTENT = new Status(205, "Reset Content");
    public static final Status PARTIAL_CONTENT = new Status(206, "Partial Content");
    public static final Status MULTIPLE_CHOICES = new Status(300, "Multiple Choices");
    public static final Status MOVED_PERMANENTLY = new Status(301, "Moved Permanently");
    public static final Status FOUND = new Status(302, "Found");
    public static final Status SEE_OTHER = new Status(303, "See Other");
    public static final Status NOT_MODIFIED = new Status(304, "Not Modified");
    public static final Status USE_PROXY = new Status(305, "Use Proxy");
    public static final Status TEMPORARY_REDIRECT = new Status(307, "Temporary Redirect");
    public static final Status BAD_REQUEST = new Status(400, "Bad Request");
    public static final Status UNAUTHORIZED = new Status(401, "Unauthorized");
    public static final Status PAYMENT_REQUIRED = new Status(402, "Payment Required"); //Reserved for future use!
    public static final Status FORBIDDEN = new Status(403, "Forbidden");
    public static final Status NOT_FOUND = new Status(404, "Not Found");
    public static final Status METHOD_NOT_ALLOWED = new Status(405, "Method Not Allowed");
    public static final Status NOT_ACCEPTABLE = new Status(406, "Not Acceptable");
    public static final Status PROXY_AUTHENTICATION_REQUIRED = new Status(407, "Proxy Authentication Required");
    public static final Status REQUEST_TIMEOUT = new Status(408, "Request Timeout");
    public static final Status CONFLICT = new Status(409, "Conflict");
    public static final Status GONE = new Status(410, "Gone");
    public static final Status LENGTH_REQUIRED = new Status(411, "Length Required");
    public static final Status PRECONDITION_FAILED = new Status(412, "Precondition Failed");
    public static final Status REQUEST_ENTITY_TOO_LARGE = new Status(413, "Request Entity Too Large");
    public static final Status REQUEST_URI_TOO_LONG = new Status(414, "Request-URI Too Long");
    public static final Status UNSUPPORTED_MEDIA_TYPE = new Status(415, "Unsupported Media Type");
    public static final Status REQUESTED_RANGE_NOT_SATISFIABLE = new Status(416, "Requested Range Not Satisfiable");
    public static final Status EXPECTATION_FAILED = new Status(417, "Expectation Failed");
    public static final Status INTERNAL_SERVER_ERROR = new Status(500, "Internal Server Error");
    public static final Status NOT_IMPLEMENTED = new Status(501, "Not Implemented");
    public static final Status BAD_GATEWAY = new Status(502, "Bad Gateway");
    public static final Status SERVICE_UNAVAILABLE = new Status(503, "Service Unavailable");
    public static final Status GATEWAY_TIMEOUT = new Status(504, "Gateway Timeout");
    public static final Status HTTP_VERSION_NOT_SUPPORTED = new Status(505, "HTTP Version Not Supported");

    private int code;
    private static final long serialVersionUID = 352513594744701224L;

    public Status(int code, String name) {
        super(name);
        this.code = code;
    }

    public Status(int code, String name, String description) {
        super(name, description);

        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public boolean isClientError() {
        return code >= 400 && code <= 500;
    }

    public boolean isServerError() {
        return code >= 500 && code <= 600;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        Status status = (Status) o;

        return code == status.code;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + code;
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