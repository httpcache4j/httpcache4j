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

import java.io.Serializable;
import java.text.Collator;
import java.util.*;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.Header;
import org.codehaus.httpcache4j.HeaderConstants;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.mutable.MutableHeaders;
import org.codehaus.httpcache4j.preference.Preference;

/**
 * Represents a HTTP Variation.
 * We need to store a different version of the response if the request varies on
 * E.G Accept headers.
 * Implementors of storage engines needs to have knowledge of this class.
 * See {@link Key} for how it's used.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class Vary {
    public static final Vary ALL = new Vary(Collections.singletonMap("ALL", "true"));
    private final Map<String, String> varyHeaders;

    /**
     * Default constructor (no variations)
     */
    public Vary() {
        this(Collections.<String, String>emptyMap());
    }

    /**
     * Constructor
     *
     * @param headers the vary headers as keys from the response, with request headers as values.
     */
    public Vary(final Map<String, String> headers) {
        Objects.requireNonNull(headers, "Headers may not be null");
        Map<String, String> h = new TreeMap<String, String>(new VaryComparator());
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String value = normalizeValue(entry.getKey(), entry.getValue());
            h.put(entry.getKey(), value);
        }
        varyHeaders = Collections.unmodifiableMap(h);
    }

    public Vary(Headers headers) {
        Map<String, String> h = new TreeMap<String, String>(new VaryComparator());
        for (Header header : headers) {
            String value = normalizeValue(header.getName(), header.getValue());
            h.put(header.getName(), value);
        }
        varyHeaders = Collections.unmodifiableMap(h);
    }

    private String normalizeValue(String name, String value) {
        if (name.toLowerCase().startsWith("accept")) {
            List<Preference> parse = Preference.parse(new Header(name, value));
            value = Preference.toHeader(name, parse).getValue();
        }
        return value;
    }

    public int size() {
        return varyHeaders.size();
    }

    public boolean isEmpty() {
        return varyHeaders.isEmpty();
    }

    /**
     * Analyses the headers in the given request to figure out if this {@link Vary variation} matches.
     *
     * @param request the request to analyse
     * @return {@code true} if the request matches the variance. {@code false} if not.
     */
    //todo: cleanup this
    public boolean matches(final HTTPRequest request) {
        if (varyHeaders.containsKey("ALL")) return false;
        Headers headers = request.getAllHeaders();

        for (Map.Entry<String, String> varyEntry : varyHeaders.entrySet()) {
            if (request.getChallenge().isPresent() && varyEntry.getKey().equals(HeaderConstants.AUTHORIZATION)) {
                if (!request.getChallenge().get().getIdentifier().equals(varyEntry.getValue())) {
                    return false;
                }
            }
            else {
                List<Header> requestHeaderValue = headers.getHeaders(varyEntry.getKey());
                boolean valid = requestHeaderValue.isEmpty() ? varyEntry.getValue() == null : headers.getFirstHeader(varyEntry.getKey()).get().getValue().equals(varyEntry.getValue());
                if (!valid) {
                    return false;
                }
            }
        }
        List<Preference> preferences = new ArrayList<>();
        preferences.addAll(headers.getAccept());
        preferences.addAll(headers.getAcceptCharset());
        preferences.addAll(headers.getAcceptLanguage());

        return !(varyHeaders.isEmpty() && !preferences.isEmpty());
    }

    @Override
    public String toString() {
        MutableHeaders headers = new MutableHeaders();
        for (Map.Entry<String, String> entry : varyHeaders.entrySet()) {
            headers.add(entry.getKey(), entry.getValue());
        }
        return headers.toHeaders().toString();
    }

    public Map<String, String> getVaryHeaders() {
        return varyHeaders;
    }

    public static Vary parse(String value) {
        Headers headers = Headers.parse(value);
        return new Vary(headers);
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
        return varyHeaders.hashCode();
    }

    public static class VaryComparator implements Comparator<String>, Serializable {
        private static final long serialVersionUID = 7826440288680033131L;
        private transient Collator collator = getCollator();

        private Collator getCollator() {
            if (collator == null) {
                collator = Collator.getInstance(Locale.UK);
            }
            return collator;
        }

        public int compare(String one, String two) {
            return getCollator().compare(one, two);
        }
    }
}
