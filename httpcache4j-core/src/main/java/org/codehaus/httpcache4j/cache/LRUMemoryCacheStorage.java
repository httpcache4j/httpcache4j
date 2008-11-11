package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.util.LRUHashMap;
import org.codehaus.httpcache4j.payload.Payload;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public class LRUMemoryCacheStorage extends MemoryCacheStorage {

    public LRUMemoryCacheStorage() {
        this(1000);
    }

    public LRUMemoryCacheStorage(int capacity) {
        cache = new InvalidatingLRUHashMap(capacity);
    }

    private class InvalidatingLRUHashMap extends LRUHashMap<URI, CacheValue> {
        public InvalidatingLRUHashMap(final int capacity) {
            super(capacity);
        }

        @Override
        public CacheValue remove(final Object key) {
            final CacheValue value = super.remove(key);
            invalidate(value, null);
            return value;
        }
    }
}