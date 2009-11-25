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
        this(headers.copyMap());
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
        return headers.keys();
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

    private static class HeaderHashMap extends LinkedHashMap<Name, List<String>> {
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

        public List<String> get(String key) {
            return get(new Name(key));
        }

        public Set<String> keys() {
            Set<String> strings = new HashSet<String>();
            for (Name name : super.keySet()) {
                strings.add(name.getName());
            }
            return strings;
        }

        @Override
        public List<String> get(Object key) {
            List<String> value = super.get(key);
            return value != null ? value : Collections.<String>emptyList();
        }

        List<Header> getAsHeaders(final String key) {
            List<Header> headers = new ArrayList<Header>();
            Name name = new Name(key);
            headers.addAll(Lists.transform(get(name), nameToHeader(name)));
            return Collections.unmodifiableList(headers);
        }

        private Function<String, Header> nameToHeader(final Name key) {
            return new Function<String, Header>() {
                    public Header apply(String from) {
                        return new Header(key.getName(), from);
                    }
                };
        }

        public List<String> put(String key, List<String> value) {
            return super.put(new Name(key), value);
        }

        List<String> putImpl(String key, List<Header> value) {
            List<String> stringList = Lists.transform(value, headerToString);
            return put(key, new ArrayList<String>(stringList));
        }

        public List<String> remove(String key) {
            return remove(new Name(key));
        }

        Iterator<Header> getAsHeaders() {
            List<Header> headers = new ArrayList<Header>();
            for (Map.Entry<Name, List<String>> entry : this.entrySet()) {
                headers.addAll(Lists.transform(entry.getValue(), nameToHeader(entry.getKey())));
            }
            return headers.iterator();
        }
    }

    private static class Name implements Serializable {
        private static final long serialVersionUID = 429640405363982150L;
        private final String name;

        public Name(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Name name1 = (Name) o;

            return !(name != null ? !name.equalsIgnoreCase(name1.name) : name1.name != null);
        }

        @Override
        public int hashCode() {
            return name != null ? name.toLowerCase(Locale.ENGLISH).hashCode() : 0;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}