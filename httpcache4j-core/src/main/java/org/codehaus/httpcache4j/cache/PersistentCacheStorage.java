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
import java.io.Serializable;
import java.net.URI;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.Validate;

/**
 * Persistent version of the in memory cache. This stores a serialized version of the
 * hashmap on every save. The cache is then restored on startup.
 *  
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class PersistentCacheStorage extends MemoryCacheStorage implements Serializable {

    protected Map<URI, CacheValue> cache;
    private final File serializationFile;
    private final int capacity;
    private static final long serialVersionUID = -5754292582576416056L;

    public PersistentCacheStorage(File serializationFileDirectory) {
        this(1000, serializationFileDirectory, "persistent.ser");
    }

    public PersistentCacheStorage(int capacity, File serializationFileDirectory, String name) {
        Validate.isTrue(capacity > 0, "You may not have a empty persistent cache");
        Validate.notNull(serializationFileDirectory, "You may not have a null serializationDirectory");
        Validate.notEmpty(name, "You may not have a empty file name");
        this.capacity = capacity;
        serializationFile = new File(serializationFileDirectory, name);
        getCacheFromDisk();
        if (!serializationFileDirectory.exists()) {
            serializationFileDirectory.mkdirs();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                saveCacheToDisk();
            }
        }));
    }

    @Override
	public synchronized void put(URI requestURI, Vary vary, CacheItem cacheItem) {
        super.put(requestURI, vary, cacheItem);
        saveCacheToDisk();
    }

    @SuppressWarnings({"unchecked"})
    private void getCacheFromDisk() {
        if (cache == null) {
            cache = new InvalidateOnRemoveHashMap(capacity);
        }
        if (serializationFile.exists()) {
            FileInputStream inputStream = null;
            try {
                inputStream = FileUtils.openInputStream(serializationFile);
                cache = (Map<URI, CacheValue>) SerializationUtils.deserialize(inputStream);
            }
            catch (Exception e) {
                serializationFile.delete();
                e.printStackTrace();
                if (e instanceof RuntimeException) {
                    throw (RuntimeException)e;
                }
                //Ignored, we create a new one.
                cache = new InvalidateOnRemoveHashMap(capacity);
            }
            finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }
    
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
}