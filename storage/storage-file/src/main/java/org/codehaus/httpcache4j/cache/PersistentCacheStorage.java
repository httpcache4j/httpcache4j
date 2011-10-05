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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.payload.Payload;

/**
 * Persistent version of the in memory cache. This stores a serialized version of the
 * hashmap on every save. The cache is then restored on startup.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class PersistentCacheStorage extends MemoryCacheStorage implements Serializable {

    private static final long PERSISTENT_TIMEOUT = 60000L;
    private static final int PERSISTENT_TRESHOLD = 100;

    private static final long serialVersionUID = 2551525125071085301L;

    private final File serializationFile;
    private final FileManager fileManager;

    private transient int modCount;
    private long lastSerialization = 0L;

    public PersistentCacheStorage(File storageDirectory) {
        this(1000, storageDirectory, "persistent.ser");
    }

    public PersistentCacheStorage(final int capacity, final File storageDirectory, final String name) {
        super(capacity);
        Validate.isTrue(capacity > 0, "You may not have a empty persistent cache");
        Validate.notNull(storageDirectory, "You may not have a null storageDirectory");
        Validate.notEmpty(name, "You may not have a empty file name");
        fileManager = new FileManager(storageDirectory);

        serializationFile = new File(storageDirectory, name);
        getCacheFromDisk();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                saveCacheToDisk();
            }
        }));
    }

    @Override
    protected void afterClear() {
        serializationFile.delete();
        fileManager.clear();
    }

    @Override
    public HTTPResponse putImpl(Key key, HTTPResponse response) {
      HTTPResponse res = super.putImpl(key, response);
      if (modCount++ % PERSISTENT_TRESHOLD == 0) {
        if (System.currentTimeMillis() > lastSerialization + PERSISTENT_TIMEOUT) {
          lastSerialization = System.currentTimeMillis();
          saveCacheToDisk();
        }
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
            return new CleanableFilePayload(file, payload.getMimeType());
        }
        return null;
    }

    private synchronized void getCacheFromDisk() {
        if (cache == null) {
            cache = new InvalidateOnRemoveLRUHashMap(capacity);
        }
        if (serializationFile.exists()) {
            FileInputStream inputStream = null;
            try {
                inputStream = FileUtils.openInputStream(serializationFile);
                cache = (InvalidateOnRemoveLRUHashMap) SerializationUtils.deserialize(inputStream);
            }
            catch (Exception e) {
                serializationFile.delete();
                //Ignored, we create a new one.
                cache = new InvalidateOnRemoveLRUHashMap(capacity);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    private synchronized void saveCacheToDisk() {
        InvalidateOnRemoveLRUHashMap snapshot = this.cache.copy();

        FileOutputStream outputStream = null;
        try {
            outputStream = FileUtils.openOutputStream(serializationFile);
            SerializationUtils.serialize(snapshot, outputStream);
        }
        catch (Exception e) {
            //Ignored, we create a new one.
        }
        finally {
            IOUtils.closeQuietly(outputStream);
        }
    }
}
