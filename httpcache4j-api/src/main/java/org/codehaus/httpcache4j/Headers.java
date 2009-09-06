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

import com.google.common.collect.Lists;
import com.google.common.base.Function;


/**
 * A collection of headers.
 * All methods that modify the headers return a new Headers object. 
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class Headers implements Serializable, Iterable<Header> {
    private static final long serialVersionUID = -7175564984758939316L;
    private final HeaderHashMap headers = new HeaderHashMap();

    public Headers() {
    }

    public Headers(final Headers headers) {
        Validate.notNull(headers, "The headers may not be null");
        this.headers.putAll(headers.copyMap());
    }

    private Headers(final HeaderHashMap headers) {
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
        HeaderHashMap headers = copyMap();
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

    private HeaderHashMap copyMap() {
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

    public Headers add(String name, List<Header> headers) {
        HeaderHashMap heads = copyMap();
        heads.putImpl(name, headers);
        return new Headers(heads);
    }

    public Headers remove(String name) {
        HeaderHashMap heads = copyMap();
        heads.remove(name);
        return new Headers(heads);
    }

    public Headers add(Iterable<Header> headers) {
        HeaderHashMap map = copyMap();
        for (Header header : headers) {
            List<String> list = new ArrayList<String>(map.get(header.getName()));
            if (!list.contains(header.getValue())) {
                list.add(header.getValue());
            }
            map.put(header.getName(), list);
        }
        return new Headers(map);
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

    public static class HeaderHashMap extends LinkedHashMap<String, List<String>> {
        private static final long serialVersionUID = 2714358409043444835L;

        private static final Function<Header,String> headerToString = new Function<Header, String>() {
            public String apply(Header from) {
                return from.getValue();
            }
        };

        public HeaderHashMap() {
        }

        public HeaderHashMap(HeaderHashMap headerHashMap) {
            super(headerHashMap);
        }

        @Override
        public List<String> get(Object key) {
            List<String> value = super.get(((String) key).toLowerCase());
            return value != null ? value : Collections.<String>emptyList();
        }

        List<Header> getAsHeaders(final String key) {
            List<Header> headers = new ArrayList<Header>();
            headers.addAll(Lists.transform(get(key), stringToHeader(key)));
            return Collections.unmodifiableList(headers);
        }

        private Function<String, Header> stringToHeader(final String key) {
            return new Function<String, Header>() {
                    public Header apply(String from) {
                        return new Header(key, from);
                    }
                };
        }

        @Override
        public List<String> put(String key, List<String> value) {
            return super.put(key.toLowerCase(), value);
        }

        List<String> putImpl(String key, List<Header> value) {
            List<String> stringList = Lists.transform(value, headerToString);
            return put(key, new ArrayList<String>(stringList));
        }

        Iterator<Header> getAsHeaders() {
            List<Header> headers = new ArrayList<Header>();
            for (Map.Entry<String, List<String>> entry : this.entrySet()) {
                headers.addAll(Lists.transform(entry.getValue(), stringToHeader(entry.getKey())));
            }
            return headers.iterator();
        }
    }
}