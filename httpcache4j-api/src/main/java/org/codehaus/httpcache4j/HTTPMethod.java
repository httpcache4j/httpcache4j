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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;

import java.util.Locale;
import java.util.Map;

/**
 * An enum that defines the different HTTP methods.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class HTTPMethod {
    public static final HTTPMethod CONNECT = new HTTPMethod("CONNECT");
    public static final HTTPMethod DELETE = new HTTPMethod("DELETE", false, Idempotency.IDEMPOTENT);
    public static final HTTPMethod GET = new HTTPMethod("GET", true, Idempotency.IDEMPOTENT);
    public static final HTTPMethod HEAD = new HTTPMethod("HEAD", true, Idempotency.IDEMPOTENT);
    public static final HTTPMethod OPTIONS = new HTTPMethod("OPTIONS", false, Idempotency.IDEMPOTENT);
    public static final HTTPMethod PATCH = new HTTPMethod("PATCH");
    public static final HTTPMethod POST = new HTTPMethod("POST");
    public static final HTTPMethod PURGE = new HTTPMethod("PURGE");
    public static final HTTPMethod PUT = new HTTPMethod("PUT", false, Idempotency.IDEMPOTENT);
    public static final HTTPMethod TRACE = new HTTPMethod("TRACE", false, Idempotency.IDEMPOTENT);

    private static Map<String, HTTPMethod> defaultMethods = ImmutableMap.<String, HTTPMethod>builder()
            .put(CONNECT.getMethod().toUpperCase(Locale.ENGLISH), CONNECT)
            .put(DELETE.getMethod().toUpperCase(Locale.ENGLISH), DELETE)
            .put(GET.getMethod().toUpperCase(Locale.ENGLISH), GET)
            .put(HEAD.getMethod().toUpperCase(Locale.ENGLISH), HEAD)
            .put(OPTIONS.getMethod().toUpperCase(Locale.ENGLISH), OPTIONS)
            .put(PATCH.getMethod().toUpperCase(Locale.ENGLISH), PATCH)
            .put(POST.getMethod().toUpperCase(Locale.ENGLISH), POST)
            .put(PURGE.getMethod().toUpperCase(Locale.ENGLISH), PURGE)
            .put(PUT.getMethod().toUpperCase(Locale.ENGLISH), PUT)
            .put(TRACE.getMethod().toUpperCase(Locale.ENGLISH), TRACE)
            .build();

    private final String method;
    private final boolean cacheable;
    private final Idempotency idempotency;

    public static enum Idempotency {
        IDEMPOTENT,
        NON_IDEMPOTENT
    }

    private HTTPMethod(String method) {
        this(method, false, Idempotency.NON_IDEMPOTENT);
    }

    private HTTPMethod(String method, boolean cacheable, Idempotency idempotency) {
        this.method = method;
        this.cacheable = cacheable;
        this.idempotency = idempotency;
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

    public static HTTPMethod[] values() {
        return defaultMethods.values().toArray(new HTTPMethod[defaultMethods.size()]);
    }

    public static HTTPMethod valueOf(String method) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(method), "Method name may not be null or empty");
        String uppercaseMethod = method.toUpperCase(Locale.ENGLISH);
        if (defaultMethods.containsKey(uppercaseMethod)) {
            return defaultMethods.get(uppercaseMethod);
        }
        return new HTTPMethod(uppercaseMethod);
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

    public boolean isSafe() {
        return this == GET || this == HEAD;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public boolean isIdempotent() {
        return idempotency == Idempotency.IDEMPOTENT;
    }
}
