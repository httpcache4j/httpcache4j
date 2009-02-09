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

package org.codehaus.httpcache4j.cache;

import org.apache.commons.lang.Validate;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.Header;

import java.io.Serializable;
import java.util.*;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public final class Vary implements Serializable {
    public static final String ALL = "*";
    private static final long serialVersionUID = -5275022740812240365L;

    private final Map<String, String> varyHeaders = new HashMap<String, String>();

    /**
     * Default constructor (no variations)
     */
    public Vary() {        
    }

    /**
     * Constructor
     *
     * @param headers the vary headers as keys from the response, with request headers as values.
     */
    public Vary(final Map<String, String> headers) {
        Validate.notNull(headers);
        varyHeaders.putAll(headers);
    }

    /** @return the header names. */
    public List<String> getVaryHeaderNames() {
        return Collections.unmodifiableList(new ArrayList<String>(varyHeaders.keySet()));
    }

    /**
     * Analyses the headers in the given request to figure out if this {@link Vary variation} matches.
     *
     * @param request the request to analyse
     *
     * @return {@code true} if the request matches the variance. {@code false} if not.
     */
    public boolean matches(final HTTPRequest request) {
        for (Map.Entry<String, String> varyEntry : varyHeaders.entrySet()) {
            List<Header> requestHeaderValue = request.getHeaders().getHeaders(varyEntry.getKey());
            boolean valid = requestHeaderValue == null ? varyEntry.getValue() == null : request.getHeaders().getFirstHeader(varyEntry.getKey()).getValue().equals(varyEntry.getValue());
            if (!valid) {
                return false;
            }

        }
        return true;
    }

    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Vary vary = (Vary) o;

        if (varyHeaders != null ? !varyHeaders.equals(vary.varyHeaders) : vary.varyHeaders != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        return (varyHeaders != null ? varyHeaders.hashCode() : 0);
    }
}