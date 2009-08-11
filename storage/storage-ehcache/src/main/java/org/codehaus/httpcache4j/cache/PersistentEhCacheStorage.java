/*
 * Copyright (c) 2009. The Codehaus. All Rights Reserved.
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
 */

package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.util.DeletingFileFilter;
import org.codehaus.httpcache4j.util.StorageUtil;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Cache;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * @author <a href="mailto:erlend@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class PersistentEhCacheStorage extends AbstractEhCacheStorage {
    private static final long PERSISTENT_TIMEOUT = 60000L;
    private static final int PERSISTENT_TRESHOLD = 100;
    
    private final File diskStoragePath;
    private transient int modCount;
    private long lastSerialization = 0L;

    public PersistentEhCacheStorage(File diskStoragePath) {
        super(new Cache("http", 1000, MemoryStoreEvictionPolicy.LRU, true, diskStoragePath.getAbsolutePath(), false, 3600L, 100L, true, 5L, null));
        StorageUtil.ensureDirectoryExists(diskStoragePath);
        this.diskStoragePath = diskStoragePath;
    }

    protected Payload createPayload(Key key, Payload payload, InputStream stream) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected HTTPResponse putImpl(Key key, HTTPResponse response) {
        HTTPResponse puttedResponse = super.putImpl(key, response);
        if (modCount++ % PERSISTENT_TRESHOLD == 0) {
            if (System.currentTimeMillis() > lastSerialization + PERSISTENT_TIMEOUT) {
                lastSerialization = System.currentTimeMillis();
                cache.flush();
            }
        }
        
        return puttedResponse;        
    }

    @Override
    protected void invalidate(final Key key) {
        HTTPResponse response = get(key);
        if (response.hasPayload()) {
            CleanableFilePayload payload = (CleanableFilePayload) response.getPayload();
            payload.clean();
        }
        super.invalidate(key);
    }

    @Override
    public void clear() {
        super.clear();
        diskStoragePath.listFiles(new DeletingFileFilter());
    }
}
