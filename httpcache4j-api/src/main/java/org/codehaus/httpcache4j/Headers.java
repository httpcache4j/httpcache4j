package org.codehaus.httpcache4j;

import org.apache.commons.lang.Validate;

import java.io.Serializable;
import java.util.*;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public final class Headers implements Serializable, Iterable<Map.Entry<String, List<Header>>> {
    public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
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