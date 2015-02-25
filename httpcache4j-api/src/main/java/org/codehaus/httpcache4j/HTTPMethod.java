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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An enum that defines the different HTTP methods.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class HTTPMethod implements Comparable<HTTPMethod> {
    public static final HTTPMethod CONNECT = new HTTPMethod("CONNECT");
    public static final HTTPMethod DELETE = new HTTPMethod("DELETE", Idempotency.IDEMPOTENT, Safety.UNSAFE);
    public static final HTTPMethod GET = new HTTPMethod("GET", Idempotency.IDEMPOTENT, Safety.SAFE);
    public static final HTTPMethod HEAD = new HTTPMethod("HEAD", Idempotency.IDEMPOTENT, Safety.SAFE);
    public static final HTTPMethod OPTIONS = new HTTPMethod("OPTIONS", Idempotency.IDEMPOTENT, Safety.SAFE);
    public static final HTTPMethod PATCH = new HTTPMethod("PATCH");
    public static final HTTPMethod POST = new HTTPMethod("POST");
    public static final HTTPMethod PURGE = new HTTPMethod("PURGE");
    public static final HTTPMethod PUT = new HTTPMethod("PUT", Idempotency.IDEMPOTENT, Safety.UNSAFE);
    public static final HTTPMethod TRACE = new HTTPMethod("TRACE", Idempotency.IDEMPOTENT, Safety.SAFE);

    public static final Map<String, HTTPMethod> defaultMethods;
    static {
        List<HTTPMethod> defaultMethodList = Arrays.asList(
                CONNECT,
                DELETE,
                GET,
                HEAD,
                OPTIONS,
                PATCH,
                POST,
                PURGE,
                PUT,
                TRACE
        );
        defaultMethods = Collections.unmodifiableMap(defaultMethodList.stream().collect(Collectors.toMap(
                m -> m.getMethod().toUpperCase(Locale.ENGLISH), Function.<HTTPMethod>identity()
        )));
    }

    private final String method;
    private final Idempotency idempotency;
    private final Safety safety;

    public static enum Idempotency {
        IDEMPOTENT,
        NON_IDEMPOTENT
    }

    public static enum Safety {
        SAFE,
        UNSAFE
    }

    private HTTPMethod(String method) {
        this(method, Idempotency.NON_IDEMPOTENT, Safety.UNSAFE);
    }

    private HTTPMethod(String method, Idempotency idempotency, Safety safety) {
        this.method = method;
        this.idempotency = idempotency;
        this.safety = safety;
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
        if (method == null || method.isEmpty()) {
            throw new IllegalArgumentException("method may not be null or empty");
        }
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
    public int compareTo(HTTPMethod other) {
        return getMethod().compareToIgnoreCase(other.getMethod());
    }

    @Override
    public int hashCode() {
        return method != null ? method.hashCode() : 0;
    }

    public boolean canHavePayload() {
        return this == POST || this == PUT || this == PATCH;
    }

    public boolean isSafe() {
        return this.safety == Safety.SAFE;
    }

    public boolean isCacheable() {
        return this == GET || this == HEAD;
    }

    public boolean isIdempotent() {
        return idempotency == Idempotency.IDEMPOTENT;
    }
}
