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


import org.codehaus.httpcache4j.mutable.MutableHeaders;
import org.codehaus.httpcache4j.preference.Preference;
import org.codehaus.httpcache4j.util.CaseInsensitiveKey;
import org.joda.time.DateTime;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * A collection of headers.
 * All methods that modify the headers return a new Headers object. 
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class Headers implements Iterable<Header> {
    private final HeaderHashMap headers = new HeaderHashMap();

    public Headers() {
    }

    public Headers(final Headers headers) {
        this(headers.copyMap());
    }

    public Headers(final Map<String, List<String>> headers) {
        this(new HeaderHashMap(headers));
    }

    private Headers(final HeaderHashMap headers) {
        this.headers.putAll(Objects.requireNonNull(headers, "The header map may not be null"));
    }

    public List<Header> getHeaders(String name) {
        return headers.getAsHeaders(name);
    }

    public List<Directives> getDirectives(String name) {
        return getHeaders(name).stream().map(Header::getDirectives).collect(Collectors.toList());
    }

    //TODO: Eliminate null
    public Header getFirstHeader(String headerKey) {
        List<Header> headerList = getHeaders(headerKey);
        if (!headerList.isEmpty()) {
            return headerList.get(0);
        }
        return null;
    }

    //TODO: Eliminate null
    public String getFirstHeaderValue(String headerKey) {
        Header header = getFirstHeader(headerKey);
        if (header != null) {
            return header.getValue();
        }
        return null;
    }

    //TODO: Eliminate null
    public Directives getFirstHeaderValueAsDirectives(String headerKey) {
        Header header = getFirstHeader(headerKey);
        if (header != null) {
            return header.getDirectives();
        }
        return null;
    }

    public Headers add(Header header) {
       return add(Collections.singletonList(header));
    }

    public Headers add(String key, String value) {
        return add(new Header(key, value));
    }

    public Headers add(Iterable<Header> headers) {
        HeaderHashMap map = copyMap();
        for (Header header : headers) {
            List<String> list = new ArrayList<>(map.get(header.getName()));
            String value = normalizeValue(header.getName(), header.getValue());
            if (!list.contains(value)) {
                list.add(value);
            }
            map.put(header.getName(), list);
        }
        return new Headers(map);
    }

    public Headers add(String name, Iterable<String> values) {
        List<Header> list = new ArrayList<>();
        for (String value : values) {
            list.add(new Header(name, value));
        }
        return add(list);
    }

    public Headers set(Header header) {
        HeaderHashMap headers = copyMap();
        String normalized = normalizeValue(header.getName(), header.getValue());
        headers.put(header.getName(), new ArrayList<>(Arrays.asList(normalized)));
        return new Headers(headers);
    }

    public Headers set(String name, String value) {
        return set(new Header(name, value));
    }

    public Headers set(Iterable<Header> headers) {
        HeaderHashMap map = copyMap();
        Headers copy = new Headers().add(headers);
        Set<String> keys = copy.keySet();
        for (String key : keys) {
            map.put(key, copy.headers.get(key));
        }
        return new Headers(map);
    }

    public boolean contains(Header header) {
        List<Header> values = getHeaders(header.getName());
        return values.contains(header);
    }

    public boolean contains(String headerName) {
        return !headers.get(headerName).isEmpty();
    }

    /**
     * @deprecated use {@link #contains(String)} instead
     */
    @Deprecated
    public boolean hasHeader(String headerName) {
        return !headers.get(headerName).isEmpty();
    }

    public Headers remove(String name) {
        HeaderHashMap heads = copyMap();
        heads.remove(name);
        return new Headers(heads);
    }

    public Iterator<Header> iterator() {
        return headers.headerIterator();
    }

    public Set<String> keySet() {
        return headers.keys();
    }

    public int size() {
        return headers.size();
    }

    public boolean isEmpty() {
        return headers.isEmpty();
    }

    public Headers asCacheable() {
        return HeaderUtils.cleanForCaching(this);
    }

    public boolean isCachable() {
        return HeaderUtils.hasCacheableHeaders(this);
    }

    public List<Preference> getAcceptLanguage() {
        return Preference.parse(getFirstHeader(HeaderConstants.ACCEPT_LANGUAGE));
    }

    public Headers withAcceptLanguage(List<Preference> acceptLanguage) {
        return set(Preference.toHeader(HeaderConstants.ACCEPT_LANGUAGE, acceptLanguage));
    }

    public List<Preference> getAcceptCharset() {
        return Preference.parse(getFirstHeader(HeaderConstants.ACCEPT_CHARSET));
    }

    public Headers withAcceptCharset(List<Preference> charsets) {
        return set(Preference.toHeader(HeaderConstants.ACCEPT_CHARSET, charsets));
    }

    public List<Preference> getAccept() {
        return Preference.parse(getFirstHeader(HeaderConstants.ACCEPT));
    }

    public Headers withAccept(List<Preference> charsets) {
        return set(Preference.toHeader(HeaderConstants.ACCEPT, charsets));
    }

    public Headers addAccept(Preference... accept) {
        List<Preference> preferences = Arrays.asList(accept);
        return add(Preference.toHeader(HeaderConstants.ACCEPT, preferences));
    }

    public Headers addAccept(MIMEType... accept) {
        List<Preference> preferences = Arrays.asList(accept).stream().map(MIMEType::toString).map(Preference::new).collect(Collectors.toList());
        return add(Preference.toHeader(HeaderConstants.ACCEPT, preferences));
    }

    public Headers addAcceptLanguage(Locale... accept) {
        Function<Locale, String> f = a -> a.getLanguage() + "-" + a.getCountry().toLowerCase(Locale.ENGLISH);
        List<Preference> preferences = Arrays.asList(accept).stream().map(f).map(Preference::new).collect(Collectors.toList());
        return add(Preference.toHeader(HeaderConstants.ACCEPT_LANGUAGE, preferences));
    }

    public Headers addAcceptLanguage(Preference... accept) {
        List<Preference> preferences = Arrays.asList(accept);
        return add(Preference.toHeader(HeaderConstants.ACCEPT_LANGUAGE, preferences));
    }

    public Headers addAcceptCharset(Preference... accept) {
        List<Preference> preferences = Arrays.asList(accept);
        return add(Preference.toHeader(HeaderConstants.ACCEPT_LANGUAGE, preferences));
    }

    public Set<HTTPMethod> getAllow() {
        Header header = getFirstHeader(HeaderConstants.ALLOW);
        if (header != null) {
            return header.getDirectives().stream().map(d -> HTTPMethod.valueOf(d.getName().toUpperCase(Locale.ENGLISH))).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    public Headers withAllow(Set<HTTPMethod> allow) {
        if (!allow.isEmpty()) {
            String value = allow.stream().map(HTTPMethod::getMethod).collect(Collectors.joining(","));
            return set(HeaderConstants.ALLOW, value);
        }
        return remove(HeaderConstants.ALLOW);
    }

    public CacheControl getCacheControl() {
        return new CacheControl(getFirstHeader(HeaderConstants.CACHE_CONTROL));
    }

    public Headers withCacheControl(CacheControl cc) {
        return set(cc.toHeader());
    }

    public DateTime getDate() {
        return HeaderUtils.fromHttpDate(getFirstHeader(HeaderConstants.DATE));
    }

    public Headers withDate(DateTime dt) {
        return set(HeaderUtils.toHttpDate(HeaderConstants.DATE, dt));
    }

    public MIMEType getContentType() {
        String ct = getFirstHeaderValue(HeaderConstants.CONTENT_TYPE);
        return ct == null ? null : MIMEType.valueOf(ct);
    }

    public Headers withContentType(MIMEType ct) {
        return set(HeaderConstants.CONTENT_TYPE, ct.toString());
    }

    public DateTime getExpires() {
        return HeaderUtils.fromHttpDate(getFirstHeader(HeaderConstants.EXPIRES));
    }

    public Headers withExpires(DateTime expires) {
        return set(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, expires));
    }

    public DateTime getLastModified() {
        return HeaderUtils.fromHttpDate(getFirstHeader(HeaderConstants.LAST_MODIFIED));
    }

    public Headers withLastModified(DateTime lm) {
        return set(HeaderUtils.toHttpDate(HeaderConstants.LAST_MODIFIED, lm));
    }

    public Conditionals getConditionals() {
        return Conditionals.valueOf(this);
    }

    public Headers withConditionals(Conditionals conditionals) {
        return add(conditionals.toHeaders());
    }


    //TODO: Eliminate null
    public Tag getETag() {
        Header tag = getFirstHeader(HeaderConstants.ETAG);
        if (tag != null) {
            return Tag.parse(tag.getValue());
        }
        return null;
    }

    public Headers withETag(Tag tag) {
        return set(HeaderConstants.ETAG, tag.format());
    }

    //TODO: Eliminate null
    public URI getLocation() {
        String location = getFirstHeaderValue(HeaderConstants.LOCATION);
        if (location != null) {
            return URI.create(location);
        }
        return null;
    }

    public Headers withLocation(URI href) {
        return set(HeaderConstants.LOCATION, href.toString());
    }

    //TODO: Eliminate null
    public URI getContentLocation() {
        String location = getFirstHeaderValue(HeaderConstants.CONTENT_LOCATION);
        if (location != null) {
            return URI.create(location);
        }
        return null;
    }

    public Headers withContentLocation(URI href) {
        return set(HeaderConstants.CONTENT_LOCATION, href.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Headers headers1 = (Headers) o;

        return headers.equals(headers1.headers);
    }

    @Override
    public int hashCode() {
        return headers.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Header header : this) {
            if (builder.length() > 0) {
                builder.append("\r\n");
            }
            builder.append(header);
        }
        return builder.toString();
    }

    private HeaderHashMap copyMap() {
        return new HeaderHashMap(headers);
    }

    private String normalizeValue(String name, String value) {
        if (name.toLowerCase().startsWith("accept")) {
            List<Preference> parse = Preference.parse(new Header(name, value));
            value = Preference.toHeader(name, parse).getValue();
        }
        return value;
    }

    public static Headers parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new Headers();
        }
        MutableHeaders headers = new MutableHeaders();
        String[] fields = input.split("\r\n");
        for (String field : fields) {
            headers.add(Header.valueOf(field.trim()));
        }
        return headers.toHeaders();
    }


    private static class HeaderHashMap extends LinkedHashMap<CaseInsensitiveKey, List<String>> {
        private static final long serialVersionUID = 2714358409043444835L;

        public HeaderHashMap() {
        }

        public HeaderHashMap(HeaderHashMap headerHashMap) {
            super(headerHashMap);
        }
        public HeaderHashMap(Map<String, List<String>> headerHashMap) {
            super(headerHashMap.entrySet().
                            stream().
                            map(k -> new SimpleImmutableEntry<>(new CaseInsensitiveKey(k.getKey()), k.getValue()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
        }

        public List<String> get(String key) {
            return get(new CaseInsensitiveKey(key));
        }

        public Set<String> keys() {
            Set<String> strings = new HashSet<String>();
            for (CaseInsensitiveKey name : super.keySet()) {
                strings.add(name.getDelegate());
            }
            return strings;
        }

        @Override
        public List<String> get(Object key) {
            List<String> value = super.get(key);
            return value != null ? value : Collections.<String>emptyList();
        }

        List<Header> getAsHeaders(final String key) {
            CaseInsensitiveKey name = new CaseInsensitiveKey(key);
            List<Header> headers = get(name).stream().map(v -> new Header(name.getDelegate(), v)).collect(Collectors.toList());
            return Collections.unmodifiableList(headers);
        }

        public List<String> put(String key, List<String> value) {
            return super.put(new CaseInsensitiveKey(key), value);
        }

        public List<String> remove(String key) {
            return remove(new CaseInsensitiveKey(key));
        }

        Iterator<Header> headerIterator() {
            List<Header> headers = entrySet().stream().flatMap(e -> e.getValue().stream().map(v -> new Header(e.getKey().getDelegate(), v)))
                    .collect(Collectors.toList());
            return headers.iterator();
        }
    }
}
