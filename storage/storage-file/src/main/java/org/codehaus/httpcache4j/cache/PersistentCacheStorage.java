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

import java.io.*;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.io.Closeables;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.util.InvalidateOnRemoveLRUHashMap;
import org.codehaus.httpcache4j.util.SerializationUtils;

/**
 * Persistent version of the in memory cache. This stores a serialized version of the
 * hashmap on every save. The cache is then restored on startup.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class PersistentCacheStorage extends MemoryCacheStorage implements Serializable, InvalidateOnRemoveLRUHashMap.RemoveListener {


    private static final long serialVersionUID = 2551525125071085301L;

    private final File serializationFile;
    private final FileManager fileManager;

    private transient int modCount;
    private long lastSerialization = 0L;
    private SerializationPolicy serializationPolicy = new DefaultSerializationPolicy();

    public PersistentCacheStorage(File storageDirectory) {
        this(1000, storageDirectory, "persistent.ser");
    }

    public PersistentCacheStorage(final int capacity, final File storageDirectory, final String name) {
        super(capacity);
        Preconditions.checkArgument(capacity > 0, "You may not have a empty persistent cache");
        Preconditions.checkNotNull(storageDirectory, "You may not have a null storageDirectory");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "You may not have a empty file name");
        fileManager = new FileManager(storageDirectory);

        serializationFile = new File(storageDirectory, name);
        getCacheFromDisk();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                saveCacheToDisk();
            }
        }));
    }

    public void onRemoveFromMap(Key key) {
        fileManager.remove(key);
    }

    FileManager getFileManager() {
        return fileManager;
    }

    public void setSerializationPolicy(SerializationPolicy serializationPolicy) {
        this.serializationPolicy = serializationPolicy == null ? new DefaultSerializationPolicy() : serializationPolicy;
    }

    @Override
    protected void afterClear() {
        serializationFile.delete();
        fileManager.clear();
    }

    @Override
    public HTTPResponse putImpl(Key key, HTTPResponse response) {
        HTTPResponse res = super.putImpl(key, response);
        if (serializationPolicy.shouldWePersist(modCount++, lastSerialization)) {
            lastSerialization = System.currentTimeMillis();
            saveCacheToDisk();
        }
        return res;
    }

    @Override
    protected CacheItem createCacheItem(HTTPResponse pCacheableResponse) {
        return new SerializableCacheItem(new DefaultCacheItem(pCacheableResponse));
    }

    @Override
    protected Payload createPayload(Key key, Payload payload, InputStream stream) throws IOException {
        File file = fileManager.createFile(key, stream);
        if (file != null && file.exists()) {
            return new FilePayload(file, payload.getMimeType());
        }
        return null;
    }

    private void getCacheFromDisk() {
        write.lock();
        try {
            if (serializationFile.exists()) {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(serializationFile);
                    cache = (InvalidateOnRemoveLRUHashMap) SerializationUtils.deserialize(inputStream);
                }
                catch (Exception e) {
                    serializationFile.delete();
                    //Ignored, we create a new one.
                    cache = new InvalidateOnRemoveLRUHashMap(capacity);
                }
                finally {
                    Closeables.closeQuietly(inputStream);
                }
            }
            else {
                cache = new InvalidateOnRemoveLRUHashMap(capacity);
            }
        } finally {
            cache.setListener(this);
            write.unlock();
        }
    }

    private void saveCacheToDisk() {
        write.lock();
        InvalidateOnRemoveLRUHashMap snapshot = this.cache.copy();

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(serializationFile);
            SerializationUtils.serialize(snapshot, outputStream);
        }
        catch (Exception e) {
            //Ignored, we create a new one.
        }
        finally {
            Closeables.closeQuietly(outputStream);
            write.unlock();
        }
    }
}
