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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.codehaus.httpcache4j.mutable.MutableHeaders;
import org.codehaus.httpcache4j.util.CaseInsensitiveKey;

import java.util.*;


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

    private Headers(final HeaderHashMap headers) {
        Preconditions.checkNotNull(headers, "The header map may not be null");
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

    public Headers set(Header header) {
        HeaderHashMap headers = copyMap();
        headers.put(header.getName(), Lists.newArrayList(header.getValue()));
        return new Headers(headers);
    }

    public Headers set(String name, String value) {
        return set(new Header(name, value));
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
        return headers.headerIterator();
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
    
    public Headers set(Iterable<Header> headers) {
        HeaderHashMap map = copyMap();
        Headers copy = new Headers().add(headers);
        Set<String> keys = copy.keySet();
        for (String key : keys) {
            map.put(key, copy.headers.get(key));
        }
        return new Headers(map);
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
            List<Header> headers = new ArrayList<Header>();
            CaseInsensitiveKey name = new CaseInsensitiveKey(key);
            headers.addAll(Lists.transform(get(name), nameToHeader(name)));
            return Collections.unmodifiableList(headers);
        }

        private Function<String, Header> nameToHeader(final CaseInsensitiveKey key) {
            return new Function<String, Header>() {
                    public Header apply(String from) {
                        return new Header(key.getDelegate(), from);
                    }
                };
        }

        public List<String> put(String key, List<String> value) {
            return super.put(new CaseInsensitiveKey(key), value);
        }

        List<String> putImpl(String key, List<Header> value) {
            List<String> stringList = Lists.transform(value, headerToString);
            return put(key, new ArrayList<String>(stringList));
        }

        public List<String> remove(String key) {
            return remove(new CaseInsensitiveKey(key));
        }

        Iterator<Header> headerIterator() {
            List<Header> headers = new ArrayList<Header>();
            for (Map.Entry<CaseInsensitiveKey, List<String>> entry : this.entrySet()) {
                headers.addAll(Lists.transform(entry.getValue(), nameToHeader(entry.getKey())));
            }
            return headers.iterator();
        }
    }
}
