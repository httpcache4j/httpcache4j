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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * HTTP Status to return after handling a call.
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
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

    public static enum Category {
        INFORMATIONAL(100, 199),
        SUCCESS(200, 299),
        REDIRECTION(300, 399),
        CLIENT_ERROR(400, 499),
        SERVER_ERROR(500, 599);

        private final Range<Integer> range;

        private Category(int min, int max) {
            range = Ranges.closed(min, max);
        }

        public boolean contains(Status status) {
            return range.contains(status.getCode());
        }

        public static Category valueOf(Status status) {
            for (Category category : values()) {
                if (category.contains(status)) {
                    return category;
                }
            }
            throw new IllegalArgumentException("Unknown category");
        }
    }

    private static final Set<Status> STATUSES_WITHOUT_BODY = ImmutableSet.of(RESET_CONTENT, NO_CONTENT, NOT_MODIFIED);
    private static final Map<Integer, Status> STATUSES;

    static {
        ImmutableMap.Builder<Integer, Status> builder = ImmutableMap.builder();
        builder.put(CONTINUE.getCode(), CONTINUE);
        builder.put(SWITCHING_PROTOCOLS.getCode(), SWITCHING_PROTOCOLS);
        builder.put(OK.getCode(), OK);
        builder.put(CREATED.getCode(), CREATED);
        builder.put(ACCEPTED.getCode(), ACCEPTED);
        builder.put(NON_AUTHORITATIVE_INFORMATION.getCode(), NON_AUTHORITATIVE_INFORMATION);
        builder.put(NO_CONTENT.getCode(), NO_CONTENT);
        builder.put(RESET_CONTENT.getCode(), RESET_CONTENT);
        builder.put(PARTIAL_CONTENT.getCode(), PARTIAL_CONTENT);
        builder.put(MULTIPLE_CHOICES.getCode(), MULTIPLE_CHOICES);
        builder.put(MOVED_PERMANENTLY.getCode(), MOVED_PERMANENTLY);
        builder.put(FOUND.getCode(), FOUND);
        builder.put(SEE_OTHER.getCode(), SEE_OTHER);
        builder.put(NOT_MODIFIED.getCode(), NOT_MODIFIED);
        builder.put(USE_PROXY.getCode(), USE_PROXY);
        builder.put(TEMPORARY_REDIRECT.getCode(), TEMPORARY_REDIRECT);
        builder.put(BAD_REQUEST.getCode(), BAD_REQUEST);
        builder.put(UNAUTHORIZED.getCode(), UNAUTHORIZED);
        builder.put(PAYMENT_REQUIRED.getCode(), PAYMENT_REQUIRED);
        builder.put(FORBIDDEN.getCode(), FORBIDDEN);
        builder.put(NOT_FOUND.getCode(), NOT_FOUND);
        builder.put(METHOD_NOT_ALLOWED.getCode(), METHOD_NOT_ALLOWED);
        builder.put(NOT_ACCEPTABLE.getCode(), NOT_ACCEPTABLE);
        builder.put(PROXY_AUTHENTICATION_REQUIRED.getCode(), PROXY_AUTHENTICATION_REQUIRED);
        builder.put(REQUEST_TIMEOUT.getCode(), REQUEST_TIMEOUT);
        builder.put(CONFLICT.getCode(), CONFLICT);
        builder.put(GONE.getCode(), GONE);
        builder.put(LENGTH_REQUIRED.getCode(), LENGTH_REQUIRED);
        builder.put(PRECONDITION_FAILED.getCode(), PRECONDITION_FAILED);
        builder.put(REQUEST_ENTITY_TOO_LARGE.getCode(), REQUEST_ENTITY_TOO_LARGE);
        builder.put(REQUEST_URI_TOO_LONG.getCode(), REQUEST_URI_TOO_LONG);
        builder.put(UNSUPPORTED_MEDIA_TYPE.getCode(), UNSUPPORTED_MEDIA_TYPE);
        builder.put(REQUESTED_RANGE_NOT_SATISFIABLE.getCode(), REQUESTED_RANGE_NOT_SATISFIABLE);
        builder.put(EXPECTATION_FAILED.getCode(), EXPECTATION_FAILED);
        builder.put(INTERNAL_SERVER_ERROR.getCode(), INTERNAL_SERVER_ERROR);
        builder.put(NOT_IMPLEMENTED.getCode(), NOT_IMPLEMENTED);
        builder.put(BAD_GATEWAY.getCode(), BAD_GATEWAY);
        builder.put(SERVICE_UNAVAILABLE.getCode(), SERVICE_UNAVAILABLE);
        builder.put(GATEWAY_TIMEOUT.getCode(), GATEWAY_TIMEOUT);
        builder.put(HTTP_VERSION_NOT_SUPPORTED.getCode(), HTTP_VERSION_NOT_SUPPORTED);
        STATUSES = builder.build();
    }

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

    public Category getCategory() {
        return Category.valueOf(this);
    }

    public boolean isClientError() {
        return Category.CLIENT_ERROR.contains(this);
    }

    public boolean isServerError() {
        return Category.SERVER_ERROR.contains(this);
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
        Status status = STATUSES.get(statusCode);
        if (status != null) {
            return status;
        }
        return new Status(statusCode, "Unknown");
    }

    public static Status[] values() {
        Collection<Status> values = STATUSES.values();
        return values.toArray(new Status[values.size()]);
    }
}
