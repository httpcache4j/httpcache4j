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

import com.google.common.annotations.Beta;
import com.google.common.cache.Cache;
import com.google.common.collect.Iterators;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.util.Pair;
import org.codehaus.httpcache4j.util.PropertiesLoader;
import org.codehaus.httpcache4j.util.StorageUtil;

import java.io.*;
import java.net.URI;
import java.util.*;

/**
 * Completely file-persistent storage, also for metadata.
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
@Beta
public class PersistentCacheStorage2 implements CacheStorage {
    private final FileManager fileManager;

    public PersistentCacheStorage2(final File storageDirectory) {
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
            Properties properties = new Properties();
            properties.putAll(key.toProperties());
            properties.putAll(item.toProperties());
            writer = new FileWriter(metadata);
            properties.store(writer, null);
        } finally {
            Closeables.closeQuietly(writer);
        }
    }

    private Pair<Key, CacheItem> readItem(File metadata) {
        if (metadata.exists()) {
            Properties properties = PropertiesLoader.get(metadata);
            return Pair.of(Key.parse(properties), SerializableCacheItem.parse(properties));
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
        Pair<Key, CacheItem> pair = readItem(metadata);
        if (pair != null) {
            return pair.getValue();
        }
        return null;
    }

    @Override
    public synchronized CacheItem get(HTTPRequest request) {
        Pair<Key, CacheItem> item = getItem(request);
        if (item != null) {
            return item.getValue();
        }
        return null;
    }

    synchronized Pair<Key, CacheItem> getItem(HTTPRequest request) {
        File uri = fileManager.resolve(request.getRequestURI());
        File[] files = uri.listFiles((FileFilter) new SuffixFileFilter("metadata"));
        for (File file : new FilesIterable(files)) {
            Pair<Key, CacheItem> pair = readItem(file);
            if (pair != null && pair.getKey().getVary().matches(request)) {
                return pair;
            }
        }
        return null;
    }

    @Override
    public synchronized void invalidate(URI uri) {
        fileManager.clear(uri);
    }

    synchronized void invalidate(Key key) {
        fileManager.remove(key);
    }

    @Override
    public synchronized void clear() {
        fileManager.clear();
    }

    @Override
    public synchronized int size() {
        int count = 0;
        File base = fileManager.getBaseDirectory();
        for (File hash : new FilesIterable(base.listFiles())) {
            for (File uriHash : new FilesIterable(hash.listFiles())) {
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
        for (File hash : new FilesIterable(base.listFiles())) {
            for (File uriHash : new FilesIterable(hash.listFiles())) {
                File[] metadata = uriHash.listFiles((FileFilter) new SuffixFileFilter("metadata"));
                for (File m : new FilesIterable(metadata)) {
                    Pair<Key, CacheItem> item = readItem(m);
                    if (item != null) {
                        keys.add(item.getKey());
                    }
                }
            }
        }
        return Collections.unmodifiableList(keys).iterator();
    }

    private static class FilesIterable implements Iterable<File> {
        private File[] files;

        public FilesIterable(File[] files) {
            this.files = files;
        }

        @Override
        public Iterator<File> iterator() {
            if (files == null) {
                return Collections.<File>emptyList().iterator();
            }
            return Iterators.forArray(files);
        }
    }

    private static class SuffixFileFilter implements FileFilter, FilenameFilter {
        private String extension;
        public SuffixFileFilter(String extension) {
            this.extension = extension;
        }

        @Override
        public boolean accept(File pathname) {
            String ext = Files.getFileExtension(pathname.getName());
            return extension.equals(ext);
        }

        @Override
        public boolean accept(File dir, String name) {
            return accept(new File(dir, name));
        }
    }
}
