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

import com.google.common.collect.Sets;
import com.google.common.io.Closeables;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.payload.ByteArrayPayload;
import org.codehaus.httpcache4j.util.InvalidateOnRemoveLRUHashMap;

import java.net.URI;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.io.InputStream;
import java.io.IOException;

/**
 * In Memory implementation of a cache storage.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class MemoryCacheStorage implements CacheStorage {

    protected final int capacity;
    protected InvalidateOnRemoveLRUHashMap cache;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    protected final Lock read = lock.readLock();
    protected final Lock write = lock.writeLock();

    public MemoryCacheStorage() {
        this(1000);
    }

    protected MemoryCacheStorage(int capacity) {
        this.capacity = capacity;
        cache = new InvalidateOnRemoveLRUHashMap(this.capacity);
    }

    private HTTPResponse rewriteResponse(Key key, HTTPResponse response) {
        if (response.hasPayload()) {
            Payload payload = response.getPayload();
            InputStream stream = null;
            try {
                stream = payload.getInputStream();
                return response.withPayload(createPayload(key, payload, stream));
            } catch (IOException ignore) {
            }
            finally {
                Closeables.closeQuietly(stream);
            }
        }
        else {
            return response;
        }
        throw new IllegalArgumentException("Unable to cache response");
    }


    public final HTTPResponse insert(final HTTPRequest request, final HTTPResponse response) {
        write.lock();
        Key key = Key.create(request, response);
        try {
            invalidate(key);
            HTTPResponse cacheableResponse = rewriteResponse(key, response);
            return putImpl(key, cacheableResponse);
        } finally {
            write.unlock();
        }
    }

    protected HTTPResponse putImpl(final Key pKey, final HTTPResponse pCacheableResponse) {
        CacheItem item = createCacheItem(pCacheableResponse);
        Map<Vary, CacheItem> varyCacheItemMap = cache.get(pKey.getURI());
        if (varyCacheItemMap == null) {
            varyCacheItemMap = new HashMap<Vary, CacheItem>();
            cache.put(pKey.getURI(), varyCacheItemMap);
        }
        varyCacheItemMap.put(pKey.getVary(), item);
        return pCacheableResponse;
    }

    protected CacheItem createCacheItem(HTTPResponse pCacheableResponse) {
        return new DefaultCacheItem(pCacheableResponse);
    }

    public final HTTPResponse update(final HTTPRequest request, final HTTPResponse response) {
        write.lock();
        Key key = Key.create(request, response);
        try {
            return putImpl(key, response);
        } finally {
            write.unlock();
        }
    }

    protected Payload createPayload(Key key, Payload payload, InputStream stream) throws IOException {
        ByteArrayPayload p = new ByteArrayPayload(stream, payload.getMimeType());
        if (p.isAvailable()) {
            return p;
        }
        return null;
    }

    public final CacheItem get(HTTPRequest request) {
        read.lock();

        try {
            Map<Vary, CacheItem> varyCacheItemMap = cache.get(request.getNormalizedURI());
            if (varyCacheItemMap == null) {
                return null;
            }
            else {
                for (Map.Entry<Vary, CacheItem> entry : varyCacheItemMap.entrySet()) {
                    if (entry.getKey().matches(request)) {
                        return entry.getValue();
                    }
                }
            }
            return null;
        } finally {
            read.unlock();
        }
    }

    public final void invalidate(URI uri) {
        write.lock();

        try {
            Map<Vary, CacheItem> varyCacheItemMap = cache.get(uri);
            if (varyCacheItemMap != null) {
                Set<Vary> vary = new HashSet<Vary>(varyCacheItemMap.keySet());
                for (Vary v : vary) {
                    Key key = new Key(uri, v);
                    cache.remove(key);
                }
            }
        } finally {
            write.unlock();
        }
    }

    public final CacheItem get(Key key) {
        read.lock();

        try {
            Map<Vary, CacheItem> varyCacheItemMap = cache.get(key.getURI());
            if (varyCacheItemMap != null) {
                return varyCacheItemMap.get(key.getVary());
            }
            return null;
        } finally {
            read.unlock();
        }
    }

    private void invalidate(Key key) {
        cache.remove(key);
    }

    public final void clear() {
        write.lock();

        try {
            Set<URI> uris = new HashSet<URI>(cache.keySet());
            for (URI uri : uris) {
                cache.remove(uri);
            }
            afterClear();
        } finally {
            write.unlock();
        }
    }


    protected void afterClear() {
    }

    public final int size() {
        read.lock();
        try {
            int size = 0;
            for (Map<Vary, CacheItem> map : cache.values()) {
                size += map.size();
            }
            return size;
        } finally {
            read.unlock();
        }
    }

    public final Iterator<Key> iterator() {
        read.lock();
        try {
            HashSet<Key> keys = Sets.newHashSet();
            for (Map.Entry<URI, Map<Vary, CacheItem>> entry : cache.entrySet()) {
                for (Vary vary : entry.getValue().keySet()) {
                    keys.add(new Key(entry.getKey(), vary));
                }
            }
            return Collections.unmodifiableSet(keys).iterator();
        } finally {
            read.unlock();
        }
    }

    @Override
    public void shutdown() {
    }
}
