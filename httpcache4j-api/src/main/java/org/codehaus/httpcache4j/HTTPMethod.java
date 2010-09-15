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

import org.apache.commons.lang.Validate;

/**
 * An enum that defines the different HTTP methods.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class HTTPMethod {
    public static final HTTPMethod CONNECT = new HTTPMethod("CONNECT");
    public static final HTTPMethod DELETE = new HTTPMethod("DELETE");
    public static final HTTPMethod GET = new HTTPMethod("GET");
    public static final HTTPMethod HEAD = new HTTPMethod("HEAD");
    public static final HTTPMethod OPTIONS = new HTTPMethod("OPTIONS");
    public static final HTTPMethod PATCH = new HTTPMethod("PATCH");
    public static final HTTPMethod POST = new HTTPMethod("POST");
    public static final HTTPMethod PURGE = new HTTPMethod("PURGE");
    public static final HTTPMethod PUT = new HTTPMethod("PUT");
    public static final HTTPMethod TRACE = new HTTPMethod("TRACE");

    private final String method;

    private HTTPMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    @Deprecated
    public String name() {
      return getMethod();
    }

    @Override
    public String toString() {
        return method;
    }

    public static HTTPMethod valueOf(String method) {
        Validate.notEmpty(method, "Method name may not be null or empty");
        if (CONNECT.getMethod().equalsIgnoreCase(method)) {
            return CONNECT;
        }
        else if (DELETE.getMethod().equalsIgnoreCase(method)) {
            return DELETE;
        }
        else if (GET.getMethod().equalsIgnoreCase(method)) {
            return GET;
        }
        else if (HEAD.getMethod().equalsIgnoreCase(method)) {
            return HEAD;
        }
        else if (OPTIONS.getMethod().equalsIgnoreCase(method)) {
            return OPTIONS;
        }
        else if (PATCH.getMethod().equalsIgnoreCase(method)) {
            return PATCH;
        }
        else if (POST.getMethod().equalsIgnoreCase(method)) {
            return POST;
        }
        else if (PUT.getMethod().equalsIgnoreCase(method)) {
            return PUT;
        }
        else if (PURGE.getMethod().equalsIgnoreCase(method)) {
            return PURGE;
        }
        else if (TRACE.getMethod().equalsIgnoreCase(method)) {
            return TRACE;
        }
        else {
            return new HTTPMethod(method.toUpperCase());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HTTPMethod that = (HTTPMethod) o;

        if (method != null ? !method.equalsIgnoreCase(that.method) : that.method != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return method != null ? method.hashCode() : 0;
    }

    public boolean canHavePayload() {
        return this == POST || this == PUT || this == PATCH;
    }
}
