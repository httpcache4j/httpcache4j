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
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;
import com.google.common.io.Closeables;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.util.Pair;
import org.codehaus.httpcache4j.util.StorageUtil;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Completely file-persistent storage, also for metadata.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class PersistentCacheStorage2 implements CacheStorage {
    private final FileManager fileManager;

    public PersistentCacheStorage2(final File storageDirectory) {
        Validate.notNull(storageDirectory, "You may not have a null storageDirectory");
        fileManager = new FileManager(storageDirectory);
    }

    FileManager getFileManager() {
        return fileManager;
    }

    private SerializableCacheItem createCacheItem(HTTPResponse response) {
        return new SerializableCacheItem(new DefaultCacheItem(response));
    }

    private HTTPResponse createCacheableResponse(Key key, HTTPResponse response) throws IOException {
        Payload payload = null;
        if (response.hasPayload()) {
            File file = fileManager.createFile(key, response.getPayload().getInputStream());
            if (file != null && file.exists()) {
                payload = new FilePayload(file, response.getPayload().getMimeType());
            }
        }
        return new HTTPResponse(payload, response.getStatusLine(), response.getHeaders());
    }

    @Override
    public synchronized HTTPResponse insert(HTTPRequest request, HTTPResponse response) {
        Key key = Key.create(request, response);
        try {
            HTTPResponse storedResponse = createCacheableResponse(key, response);
            SerializableCacheItem item = createCacheItem(storedResponse);
            writeItem(key, item);
            return item.getResponse();
        } catch (IOException e) {
            throw new HTTPException(e);
        }
    }

    private void writeItem(Key key, SerializableCacheItem item) throws IOException {
        File metadata = new File(fileManager.resolve(key).getAbsolutePath() + ".metadata");
        if (!metadata.getParentFile().exists()) {
            StorageUtil.ensureDirectoryExists(metadata.getParentFile());
        }
        FileWriter writer = null;
        try {
            JSONObject object = new JSONObject();
            object.put("key", new JSONObject(key.toJSON()));
            object.put("item", new JSONObject(item.toJSON()));
            writer = new FileWriter(metadata);
            writer.write(object.toString());
        } catch (JSONException e) {
            throw new HTTPException(e);
        } finally {
            Closeables.closeQuietly(writer);
        }
    }

    private Pair<Key, CacheItem> readItem(File metadata) throws IOException {
        if (metadata.exists()) {
            FileReader reader = null;
            try {
                reader = new FileReader(metadata);
                String string = IOUtils.toString(reader);
                JSONObject object = new JSONObject(string);
                return Pair.of(Key.parseObject(object.getJSONObject("key")), SerializableCacheItem.parseObject(object.getJSONObject("item")));
            } catch (JSONException e) {
                throw new HTTPException(e);
            } finally {
                Closeables.closeQuietly(reader);
            }
        }
        return null;
    }

    @Override
    public synchronized HTTPResponse update(HTTPRequest request, HTTPResponse response) {
        Key key = Key.create(request, response);
        try {
            SerializableCacheItem item = createCacheItem(response);
            writeItem(key, item);
        } catch (IOException e) {
            throw new HTTPException(e);
        }

        return get(key).getResponse();
    }

    @Override
    public synchronized CacheItem get(Key key) {
        File metadata = new File(fileManager.resolve(key).getAbsolutePath() + ".metadata");
        try {
            Pair<Key, CacheItem> pair = readItem(metadata);
            if (pair != null) {
                return pair.getValue();
            }
        } catch (IOException e) {
            throw new HTTPException(e);
        }
        return null;
    }

    @Override
    public synchronized CacheItem get(HTTPRequest request) {
        File uri = fileManager.resolve(request.getRequestURI());
        File[] files = uri.listFiles((FileFilter) new SuffixFileFilter(".metadata"));
        if (files != null && files.length > 0) {
            for (File file : files) {
                try {
                    Pair<Key, CacheItem> pair = readItem(file);
                    if (pair != null && pair.getKey().getVary().matches(request)) {
                        return pair.getValue();
                    }
                } catch (IOException e) {
                    throw new HTTPException(e);
                }
            }
        }
        return null;
    }

    @Override
    public synchronized void invalidate(URI uri) {
        fileManager.clear(uri);
    }

    @Override
    public synchronized void clear() {
        fileManager.clear();
    }

    @Override
    public synchronized int size() {
        int count = 0;
        File base = fileManager.getBaseDirectory();
        for (File hash : base.listFiles()) {
            for (File uriHash : hash.listFiles()) {
                String[] metadata = uriHash.list(new SuffixFileFilter("metadata"));
                count += metadata.length;
            }
        }
        return count;
    }

    @Override
    public synchronized Iterator<Key> iterator() {
        List<Key> keys = new ArrayList<Key>();
        File base = fileManager.getBaseDirectory();
        for (File hash : base.listFiles()) {
            for (File uriHash : hash.listFiles()) {
                File[] metadata = uriHash.listFiles((FileFilter) new SuffixFileFilter("metadata"));
                for (File m : metadata) {
                    try {
                        Pair<Key, CacheItem> item = readItem(m);
                        if (item != null) {
                            keys.add(item.getKey());
                        }
                    } catch (IOException e) {
                        throw new HTTPException(e);
                    }
                }
            }
        }
        return Collections.unmodifiableList(keys).iterator();
    }
}
