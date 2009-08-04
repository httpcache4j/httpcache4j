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

import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.Set;

import fj.F;


/**
 * A collection of headers.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class Headers implements Serializable, Iterable<Header> {
    private static final long serialVersionUID = -7175564984758939316L;
    private final HeaderHashMap headers = new HeaderHashMap();

    public Headers() {
    }

    public Headers(final Headers headers) {
        Validate.notNull(headers, "The headers may not be null");
        this.headers.putAll(headers.getHeadersAsMap());
    }

    public Headers(final Map<String, List<String>> headers) {
        Validate.notNull(headers, "The header map may not be null");
        this.headers.putAll(headers);
    }

    public List<Header> getHeaders(String headerKey) {
        return headers.getAsHeaders(headerKey);
    }

    public Header getFirstHeader(String headerKey) {
        List<Header> headerList = getHeaders(headerKey);
        if (!headerList.isEmpty()) {
            return headerList.get(0);
        }
        return null;
    }

    public String getFirstHeaderValue(String headerKey) {
        Header header = getFirstHeader(headerKey);
        if (header != null) {
            return header.getValue();
        }
        return null;
    }

    public Headers add(Header header) {
        Map<String, List<String>> headers = copyMap();
        List<String> list = new ArrayList<String>(headers.get(header.getName()));
        if (!list.contains(header.getValue())) {
            list.add(header.getValue());
        }
        headers.put(header.getName(), list);
        return new Headers(headers);
    }

    public Headers add(String key, String value) {
        return add(new Header(key, value));
    }

    public boolean contains(Header header) {
        List<Header> values = getHeaders(header.getName());
        return values.contains(header);
    }

    public Map<String, List<String>> getHeadersAsMap() {
        return Collections.unmodifiableMap(headers);
    }

    public HeaderHashMap copyMap() {
        return new HeaderHashMap(headers);
    }

    public Iterator<Header> iterator() {
        return headers.getAsHeaders();
    }

    public Set<String> keySet() {
        return headers.keySet();
    }

    public boolean hasHeader(String headerName) {
        return !headers.get(headerName).isEmpty();
    }

    public Headers put(String name, List<Header> headers) {
        HeaderHashMap heads = copyMap();
        heads.put(name, headers);
        return new Headers(heads);
    }

    public Headers remove(String name) {
        Map<String, List<String>> heads = copyMap();
        heads.remove(name);
        return new Headers(heads);
    }

    public int size() {
        return headers.size();
    }

    public boolean isEmpty() {
        return headers.isEmpty();
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
        return headers != null ? headers.hashCode() : 0;
    }

    public Headers add(Iterable<Header> headers) {
        HeaderHashMap map = new HeaderHashMap();
        for (Header header : headers) {
            List<String> list = new ArrayList<String>(map.get(header.getName()));
            if (!list.contains(header.getValue())) {
                list.add(header.getValue());
            }
            map.put(header.getName(), list);
        }
        return new Headers(map);
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

    public static class HeaderHashMap extends LinkedHashMap<String, List<String>> {
        private static final long serialVersionUID = 2714358409043444835L;

        public HeaderHashMap() {
        }

        public HeaderHashMap(Map<? extends String, ? extends List<String>> m) {
            super(m);
        }

        private static F<Header,String> headerToString = new F<Header, String>() {
            public String f(Header value) {
                return value.getValue();
            }
        };

        @Override
        public List<String> get(Object key) {
            List<String> value = super.get(key);
            return value != null ? value : Collections.<String>emptyList();
        }

        public List<Header> getAsHeaders(final String key) {
            fj.data.List<String> stringList = fj.data.List.iterableList(get(key));
            fj.data.List<Header> headerList = stringList.map(stringToHeader(key));
            return new ArrayList<Header>(headerList.toCollection());
        }

        private F<String, Header> stringToHeader(final String key) {
            return new F<String, Header>() {
                public Header f(String value) {
                    return new Header(key, value);
                }
            };
        }

        public List<String> put(String key, List<Header> value) {
            fj.data.List<Header> headers = fj.data.List.iterableList(value);
            fj.data.List<String> strings = headers.map(headerToString);
            return super.put(key, new ArrayList<String>(strings.toCollection()));
        }

        public Iterator<Header> getAsHeaders() {
            fj.data.List<Header> headers = fj.data.List.nil();
            for (Map.Entry<String, List<String>> entry : this.entrySet()) {
                fj.data.List<String> stringList = fj.data.List.iterableList(entry.getValue());
                fj.data.List<Header> headerList = stringList.map(stringToHeader(entry.getKey()));
                headers = headers.append(headerList);
            }
            return headers.iterator();
        }
    }
}