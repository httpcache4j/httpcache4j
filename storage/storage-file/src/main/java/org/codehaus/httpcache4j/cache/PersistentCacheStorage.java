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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Random;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.uri.URIBuilder;
import org.codehaus.httpcache4j.util.IOUtils;
import org.codehaus.httpcache4j.util.MemoryCache;
import org.codehaus.httpcache4j.util.SerializationUtils;

/**
 * Persistent version of the in memory cache. This stores a serialized version of the
 * hashmap on every save. The cache is then restored on startup.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class PersistentCacheStorage extends MemoryCacheStorage implements Serializable, MemoryCache.KeyListener {


    private static final long serialVersionUID = 2551525125071085301L;

    private final File serializationFile;
    private final FileManager fileManager;

    private transient int modCount;
    private long lastSerialization = 0L;
    private SerializationPolicy serializationPolicy = new DefaultSerializationPolicy();
    private Random rand = new Random();

    public PersistentCacheStorage(File storageDirectory) {
        this(1000, storageDirectory, "persistent.ser");
    }

    public PersistentCacheStorage(final int capacity, final File storageDirectory, final String name) {
        super(capacity, 10);
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

    public void onRemove(Key key) {
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
    protected HTTPResponse putImpl(Key key, HTTPResponse response) {
        if (response.hasPayload() && response.getPayload() instanceof FilePayload) {
            final FilePayload payload = (FilePayload)response.getPayload();
            try {
                response = response.withPayload(createRealPayload(key, payload));
            } catch (IOException ignore) {
            }
        }
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
        File file = fileManager.createFile(tmpKey(key), stream);
        if (file != null && file.exists()) {
            return new FilePayload(file, payload.getMimeType());
        }
        return null;
    }

    private Key tmpKey(Key key) {
        return new Key(URIBuilder.fromURI(key.getURI()).addPath(rand.nextInt()+"_httpCache4jTmp").toURI(), key.getVary());
    }

    private Payload createRealPayload(Key key, FilePayload payload) throws IOException {
        File file = fileManager.moveFile(payload.getFile(), key);
        if (file != null && file.exists()) {
            return new FilePayload(file, payload.getMimeType());
        }
        return null;
    }

    private void getCacheFromDisk() {
        write.lock();
        cache.setKeyListener(null);
        try {
            if (serializationFile.exists()) {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(serializationFile);
                    cache = (MemoryCache) SerializationUtils.deserialize(inputStream);
                }
                catch (Exception e) {
                    serializationFile.delete();
                    //Ignored, we create a new one.
                    cache = new MemoryCache(capacity);
                }
                finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }
            else {
                cache = new MemoryCache(capacity);
            }
        } finally {
            cache.setKeyListener(this);
            write.unlock();
        }
    }

    private void saveCacheToDisk() {
        read.lock();

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(serializationFile);
            SerializationUtils.serialize(cache, outputStream);
        }
        catch (Exception e) {
            //Ignored, we create a new one.
        }
        finally {
            IOUtils.closeQuietly(outputStream);
            read.unlock();
        }
    }
}
