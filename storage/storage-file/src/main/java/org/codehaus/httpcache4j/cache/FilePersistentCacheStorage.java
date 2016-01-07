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

import org.codehaus.httpcache4j.HTTPException;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.annotation.Beta;
import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.util.Pair;
import org.codehaus.httpcache4j.util.PropertiesLoader;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Completely file-persistent storage, also for metadata.
 *
 * TODO: This will be greatly improved by Java 7 NIO 2
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
@Beta
public class FilePersistentCacheStorage implements CacheStorage {
    private final FileManager fileManager;

    public FilePersistentCacheStorage(final File storageDirectory) {
        fileManager = new FileManager(storageDirectory);
    }

    FileManager getFileManager() {
        return fileManager;
    }

    private SerializableCacheItem createCacheItem(HTTPResponse response) {
        return new SerializableCacheItem(new DefaultCacheItem(response));
    }

    private HTTPResponse createCacheableResponse(Key key, HTTPResponse response) throws IOException {
        Optional<Payload> payload = Optional.empty();
        if (response.hasPayload()) {
            Payload p = response.getPayload().get();
            File file = fileManager.createFile(key, p.getInputStream());
            if (file != null && file.exists()) {
                payload = Optional.of(new FilePayload(file, p.getMimeType()));
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
            fileManager.ensureDirectoryExists(metadata.getParentFile());
        }
        FileWriter writer = null;
        try {
            Properties properties = new Properties();
            properties.putAll(key.toProperties());
            properties.putAll(item.toProperties());
            writer = new FileWriter(metadata);
            properties.store(writer, null);
        } finally {
            if (writer != null) {
                writer.close();
            }
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
        Optional<Pair<Key, CacheItem>> item = getItem(request);
        if (item.isPresent()) {
            return item.get().getValue();
        }
        return null;
    }

    synchronized Optional<Pair<Key, CacheItem>> getItem(HTTPRequest request) {
        File uri = fileManager.resolve(request.getNormalizedURI());
        if (uri.exists()) {
            DirectoryStream<Path> paths = getMetadata(uri);
            Stream<Path> stream = StreamSupport.stream(paths.spliterator(), false);
            return stream.map(f -> readItem(f.toFile())).filter(p -> p != null && p.getKey().getVary().matches(request)).findFirst();
        }
        return Optional.empty();
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
        final AtomicInteger count = new AtomicInteger();
        File base = fileManager.getBaseDirectory();
        try {
            Files.walkFileTree(base.toPath(), new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (isMetdata(file)) {
                        count.incrementAndGet();
                    }
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return count.get();
    }

    private boolean isMetdata(Path file) {
        return file.toFile().getName().endsWith(".metadata");
    }

    @Override
    public synchronized Iterator<Key> iterator() {
        File base = fileManager.getBaseDirectory();
        Stream<Path> stream = list(base.toPath()).flatMap(this::list).flatMap(p -> list(p, Optional.of(this::isMetdata)));
        Stream<Key> keyStream = stream.map(p -> readItem(p.toFile())).map(Pair::getKey);
        return keyStream.iterator();
    }

    private Stream<Path> list(Path d)  {
        try {
            return Files.list(d);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private Stream<Path> list(Path d, Optional<DirectoryStream.Filter<Path>> ff)  {
        if (ff.isPresent()) {
            try {
                return StreamSupport.stream(Files.newDirectoryStream(d, ff.get()).spliterator(), false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return list(d);
    }

    @Override
    public void shutdown() {
    }

    private DirectoryStream<Path> getMetadata(File uri) {
        DirectoryStream<Path> paths;
        try {
            paths = Files.newDirectoryStream(uri.toPath(), this::isMetdata);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return paths;
    }
}
