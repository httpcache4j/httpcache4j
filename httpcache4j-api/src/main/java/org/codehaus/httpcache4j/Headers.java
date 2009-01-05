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

/**
 * A collection of headers.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public final class Headers implements Serializable, Iterable<Map.Entry<String, List<Header>>> {
    private static final long serialVersionUID = -7175564984758939316L;
    private Map<String, List<Header>> headers = new HashMap<String, List<Header>>();

    public Headers() {
    }

    public Headers(Map<String, List<Header>> headers) {
        Validate.notEmpty(headers);
        this.headers.putAll(headers);
    }

    public List<Header> getHeaders(String headerKey) {
        return headers.get(headerKey);
    }

    public Header getFirstHeader(String headerKey) {
        List<Header> headerList = headers.get(headerKey);
        if (headerList != null) {
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

    public void add(Header header) {
        List<Header> list = headers.get(header.getName());
        if (list == null) {
            list = new ArrayList<Header>();
            headers.put(header.getName(), list);
        }
        if (!list.contains(header)) {
            list.add(header);
        }
    }

    public void add(String key, String value) {
        add(new Header(key, value));
    }

    public boolean contains(Header header) {
        List<Header> values = headers.get(header.getName());
        return values != null && values.contains(header);
    }

    public Map<String, List<Header>> getHeadersAsMap() {
        return Collections.unmodifiableMap(headers);
    }

    public Iterator<Map.Entry<String, List<Header>>> iterator() {
        return getHeadersAsMap().entrySet().iterator();
    }

    public Set<String> keySet() {
        return headers.keySet();
    }

    public boolean hasHeader(String headerName) {
        return headers.get(headerName) != null;
    }

    public void put(String name, List<Header> headers) {
        this.headers.put(name, headers);
    }

    public void remove(String name) {
        headers.remove(name);
    }

    public int size() {
        return headers.size();
    }

    public boolean isEmpty() {
        return headers.isEmpty();
    }
}