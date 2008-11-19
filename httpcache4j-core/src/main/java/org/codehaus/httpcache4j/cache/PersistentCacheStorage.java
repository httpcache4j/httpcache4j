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
import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.payload.Payload;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public class PersistentCacheStorage implements CacheStorage, Serializable {

    protected Map<URI, CacheValue> cache;
    private final File serializationFile;
    private final int capacity;
    private static final long serialVersionUID = 9034997800345196393L;

    public PersistentCacheStorage(File serializationFileDirectory) {
        this(1000, serializationFileDirectory, "persistent.ser");
    }

    public PersistentCacheStorage(int capacity, File serializationFileDirectory, String name) {
        Validate.isTrue(capacity > 0, "You may not have a empty persistent cache");
        Validate.notNull(serializationFileDirectory, "You may not have a null serializationDirectory");
        Validate.notEmpty(name, "You may not have a empty name");
        this.capacity = capacity;
        serializationFile = new File(serializationFileDirectory, name);
        getCacheFromDisk();
        if (!serializationFileDirectory.exists()) {
            serializationFileDirectory.mkdirs();
        }
    }

    public synchronized void put(URI requestURI, Vary vary, CacheItem cacheItem) {
        getCacheFromDisk();
        if (cache.containsKey(requestURI)) {
            CacheValue value = cache.get(requestURI);
            Map<Vary, CacheItem> variations = new HashMap<Vary, CacheItem>(value.getVariations());
            variations.put(vary, cacheItem);
            value = new CacheValue(variations);
            value.getVariations().put(vary, cacheItem);
            cache.put(requestURI, value);
        }
        else {
            cache.put(requestURI, new CacheValue(Collections.singletonMap(vary, cacheItem)));
        }
        saveCacheToDisk();
    }

    public synchronized CacheItem get(HTTPRequest request) {
        CacheValue cacheValue = cache.get(request.getRequestURI());
        if (cacheValue != null) {
            for (Map.Entry<Vary, CacheItem> entry : cacheValue) {
                if (entry.getKey().matches(request)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public synchronized void invalidate(URI uri) {
        getCacheFromDisk();
        if (cache.containsKey(uri)) {
            cache.remove(uri);
        }
        saveCacheToDisk();
    }

    public void invalidate(URI requestURI, CacheItem item) {
        getCacheFromDisk();
        if (cache.containsKey(requestURI) && item != null) {
            CacheValue value = cache.get(requestURI);
            invalidate(value, item);
            if (value.getVariations().isEmpty()) {
                cache.remove(requestURI);
            }
        }
        saveCacheToDisk();
    }

    protected void invalidate(CacheValue value, CacheItem item) {
        if (item == null) {
            for (Map.Entry<Vary, CacheItem> entry : value) {
                Payload payload = entry.getValue().getResponse().getPayload();
                if (payload instanceof CleanablePayload) {
                    ((CleanablePayload) payload).clean();
                }
            }
        }
        else {
            Vary found = null;
            for (Map.Entry<Vary, CacheItem> entry : value) {
                if (entry.getValue().equals(item)) {
                    found = entry.getKey();
                }
            }

            if (found != null) {
                value.getVariations().remove(found);
                Payload payload = item.getResponse().getPayload();
                if (payload instanceof CleanablePayload) {
                    ((CleanablePayload) payload).clean();
                }
            }
        }
    }

    public synchronized void clear() {
        Set<URI> uris = new HashSet<URI>(cache.keySet());
        for (URI uri : uris) {
            cache.remove(uri);
        }
    }

    public synchronized int size() {
        return cache.size();
    }

    @SuppressWarnings({"unchecked"})
    private void getCacheFromDisk() {
        FileInputStream inputStream = null;
        try {
            inputStream = FileUtils.openInputStream(serializationFile);
            cache = (Map<URI, CacheValue>) SerializationUtils.deserialize(inputStream);
        }
        catch (IOException e) {
            //Ignored, we create a new one.
        }
        finally {
            IOUtils.closeQuietly(inputStream);
        }
        cache = new InvalidateOnRemoveHashMap(capacity);
    }

    @SuppressWarnings({"unchecked"})
    private void saveCacheToDisk() {
        FileOutputStream outputStream = null;
        try {
            outputStream = FileUtils.openOutputStream(serializationFile);
            SerializationUtils.serialize((Serializable) cache, outputStream);
        }
        catch (IOException e) {
            //Ignored, we create a new one.
        }
        finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    private class InvalidateOnRemoveHashMap extends HashMap<URI, CacheValue> {
        public InvalidateOnRemoveHashMap(final int capacity) {
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