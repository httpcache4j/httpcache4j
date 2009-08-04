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


import java.net.URI;
import java.util.*;

/**
 * In Memory implementation of a cache storage.
 *
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class MemoryCacheStorage implements CacheStorage {

    protected InvalidateOnRemoveLRUHashMap cache;

    public MemoryCacheStorage() {
        this(1000);
    }

    public MemoryCacheStorage(int capacity) {
        cache = new InvalidateOnRemoveLRUHashMap(capacity);
    }

    public synchronized HTTPResponse put(Key key, HTTPResponse response) {
        HTTPResponse fixedResponse = createPayload(response);
        cache.put(key, new CacheItem(fixedResponse));
        return fixedResponse;
    }

    protected HTTPResponse createPayload(HTTPResponse response) {
        return response;
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

    public void invalidate(Key key) {
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

    public Iterator<Key> iterator() {
        return Collections.unmodifiableSet(cache.keySet()).iterator();
    }

    protected class InvalidateOnRemoveLRUHashMap extends LinkedHashMap<Key, CacheItem> {
        private static final long serialVersionUID = -8600084275381371031L;
        private final int capacity;

        public InvalidateOnRemoveLRUHashMap(final int capacity) {
            super(capacity);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<Key, CacheItem> eldest) {
            return size() >= capacity;
        }

        @Override
        public CacheItem remove(final Object key) {
            final CacheItem value = super.remove(key);
            Payload payload = value.getResponse().getPayload();
            if (payload instanceof CleanablePayload) {
                ((CleanablePayload) payload).clean();
            }
            return value;
        }
    }
}
