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

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.payload.ByteArrayPayload;

import java.net.URI;
import java.util.*;
import java.io.InputStream;
import java.io.IOException;

/**
 * In Memory implementation of a cache storage.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class MemoryCacheStorage extends AbstractMapBasedCacheStorage  {

    protected final int capacity;
    protected InvalidateOnRemoveLRUHashMap cache;

    public MemoryCacheStorage() {
        this(1000);
    }

    public MemoryCacheStorage(int capacity) {
        this(capacity, ByteArrayPayload.class);
    }

    protected MemoryCacheStorage(int capacity, Class<? extends Payload> payloadType) {
        super(payloadType);
        this.capacity = capacity;
        cache = new InvalidateOnRemoveLRUHashMap(this.capacity);
    }

    protected synchronized HTTPResponse putImpl(Key key, HTTPResponse cachedResponse) {
        cache.put(key, new CacheItem(cachedResponse));
        return cachedResponse;
    }

    protected Payload createPayload(Key key, Payload payload, InputStream stream) throws IOException {
        return new ByteArrayPayload(stream, payload.getMimeType());
    }

    public synchronized CacheItem get(HTTPRequest request) {
        for (Map.Entry<Key, CacheItem> entry : cache.entrySet()) {
            Key key = entry.getKey();
            if (request.getRequestURI().equals(key.getURI()) && key.getVary().matches(request)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public synchronized void invalidate(URI uri) {
        Set<Key> keys = new HashSet<Key>();
        for (Key key : cache.keySet()) {
            if (key.getURI().equals(uri)) {
                keys.add(key);
            }
        }
        for (Key key : keys) {
            cache.remove(key);
        }
    }

    protected synchronized void invalidate(Key key) {
        cache.remove(key);
    }

    public synchronized void clear() {
        Set<Key> uris = new HashSet<Key>(cache.keySet());
        for (Key uri : uris) {
            cache.remove(uri);
        }
    }

    public synchronized int size() {
        return cache.size();
    }

    public synchronized Iterator<Key> iterator() {
        return Collections.unmodifiableSet(cache.keySet()).iterator();
    }

    protected class InvalidateOnRemoveLRUHashMap extends LinkedHashMap<Key, CacheItem> {
        private static final long serialVersionUID = -8600084275381371031L;
        private final int capacity;

        public InvalidateOnRemoveLRUHashMap(final int capacity) {
            super(capacity);
            this.capacity = capacity;
        }

        private InvalidateOnRemoveLRUHashMap(InvalidateOnRemoveLRUHashMap map) {
            super(map);
            this.capacity = map.capacity;
        }

        public InvalidateOnRemoveLRUHashMap copy() {
            return new InvalidateOnRemoveLRUHashMap(this);
        }


        @Override
        protected boolean removeEldestEntry(Map.Entry<Key, CacheItem> eldest) {
            return size() > capacity;
        }


        @Override
        public CacheItem remove(final Object key) {
            final CacheItem value = super.remove(key);
            if (value != null) {
                Payload payload = value.getResponse().getPayload();
                if (payload instanceof CleanablePayload) {
                    ((CleanablePayload) payload).clean();
                }
            }
            return value;
        }
    }
}
