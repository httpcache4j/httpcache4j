package org.codehaus.httpcache4j;

import java.util.List;

/**
 * Headers to be used by the HTTPResponse. These are not modifiable.
 */
final class UnmodifiableHeaders extends Headers {
    
    public UnmodifiableHeaders(final Headers headers) {
        super(headers.getHeadersAsMap());
    }

    @Override
    public void add(String key, String value) {
        throw new UnsupportedOperationException("These headers cannot be modified");
    }

    @Override
    public void add(Header header) {
        throw new UnsupportedOperationException("These headers cannot be modified");
    }

    @Override
    public void put(String name, List<Header> headers) {
        throw new UnsupportedOperationException("These headers cannot be modified");
    }

    @Override
    public void remove(String name) {
        throw new UnsupportedOperationException("These headers cannot be modified");
    }
}
