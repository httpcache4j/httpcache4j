package org.codehaus.httpcache4j.storage.ehcache;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.SearchAttribute;
import net.sf.ehcache.config.Searchable;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.cache.*;
import org.codehaus.httpcache4j.payload.FilePayload;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: maedhros
 * Date: 10/22/11
 * Time: 12:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class EhCacheStorage implements CacheStorage {
    private final Ehcache httpcache;
    private final FileManager fileManager;


    public static EhCacheStorage memoryPersistent(File storageDir, int size) {
        return new EhCacheStorage(storageDir, true, size);
    }

    public static EhCacheStorage memoryPersistent(File storageDir) {
        return new EhCacheStorage(storageDir, true, 1000);
    }

    public static EhCacheStorage diskPersistent(File storageDir) {
        return new EhCacheStorage(storageDir, false, 1000);
    }

    public static EhCacheStorage diskPersistent(File storageDir, int size) {
        return new EhCacheStorage(storageDir, false, size);
    }

    public EhCacheStorage(File storageDir, boolean memorypersistent, int size) {
        this(storageDir, createDefaultCache(storageDir, memorypersistent, size));
    }

    private EhCacheStorage(File storageDir, Ehcache cache) {
        fileManager = new FileManager(storageDir);
        httpcache = cache;
        cache.getCacheEventNotificationService().registerListener(new CacheEventListenerAdapter() {
            @Override
            public void notifyElementEvicted(Ehcache cache, Element element) {
                invalidate((Key) element.getKey());
            }
        });
    }

    public HTTPResponse insert(HTTPRequest request, HTTPResponse response) {
        Key key = Key.create(request, response);
        invalidate(key);
        FilePayload file = null;
        if (response.hasPayload()) {
            try {
                file = new FilePayload(fileManager.createFile(key, response.getPayload().getInputStream()), response.getPayload().getMimeType());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        HTTPResponse savedResponse = new HTTPResponse(file, response.getStatus(), response.getHeaders());
        httpcache.put(new Element(key, new SerializableCacheItem(new DefaultCacheItem(savedResponse))));
        return savedResponse;
    }

    private void invalidate(Key key) {
        httpcache.remove(key);
        fileManager.remove(key);
    }

    public HTTPResponse update(HTTPRequest request, HTTPResponse response) {
        Key key = Key.create(request, response);
        CacheItem item = get(key);
        if (item != null) {
            HTTPResponse savedResponse = item.getResponse();
            HTTPResponse updatedResponse = new HTTPResponse(savedResponse.getPayload(), savedResponse.getStatus(), response.getHeaders());
            httpcache.put(new Element(key, new SerializableCacheItem(new DefaultCacheItem(updatedResponse))));
            return updatedResponse;
        }
        return response;
    }

    public CacheItem get(Key key) {
        Element element = httpcache.get(key);
        if (element != null) {
            return (CacheItem) element.getValue();
        }
        return null;
    }

    public CacheItem get(HTTPRequest request) {
        if (httpcache.isSearchable()) {
            List<Result> results = findMatching(request.getRequestURI(), true);
            for (Result result : results) {
                Key key = (Key) result.getKey();
                if (key.getVary().matches(request)) {
                    return (CacheItem) result.getValue();
                }
            }
        }
        else {
            for (Key key : this) {
                if (key.getVary().matches(request)) {
                    return (CacheItem) httpcache.get(key).getValue();
                }
            }
        }
        return null;
    }

    public void invalidate(URI uri) {
        if (httpcache.isSearchable()) {
            List<Result> all = findMatching(uri, false);
            for (Result result : all) {
                httpcache.remove(result.getKey());
            }
        }
        else {
            for (Key key : this) {
                if (uri.equals(key.getURI())) {
                    invalidate(key);
                }
            }
        }
    }

    private List<Result> findMatching(URI uri, boolean withValues) {
        Attribute<String> uriAttribute = httpcache.getSearchAttribute("uri");
        Query query = httpcache.createQuery().addCriteria(uriAttribute.eq(uri.normalize().toString())).includeKeys();
        if (withValues) {
            query.includeValues();
        }
        Results results = query.execute();
        if (results.hasKeys()) {
            return results.all();
        }
        return Collections.emptyList();
    }

    public void clear() {
        httpcache.removeAll();
    }

    public int size() {
        return httpcache.getSize();
    }

    public Iterator<Key> iterator() {
        List<Object> keys = httpcache.getKeys();
        return Lists.<Object, Key>transform(keys, new Function<Object, Key>() {
            public Key apply(Object input) {
                return (Key) input;
            }
        }).iterator();
    }

    public void shutdown() {
        httpcache.getCacheManager().shutdown();
    }

    private static Ehcache createDefaultCache(File storageDir, boolean memoryPersistent, int size) {
        CacheConfiguration config = new CacheConfiguration();
        config.setEternal(true);
        if (memoryPersistent) {
            config.addSearchable(new Searchable().searchAttribute(new SearchAttribute().name("uri").className(URIAttributeExtractor.class.getName())));
        }
        else {
            config.setDiskStorePath(storageDir.getAbsolutePath());
            config.setDiskPersistent(true);
            config.setMaxElementsOnDisk(100000);
            config.setOverflowToDisk(true);
        }
        config.setMaxElementsInMemory(size);
        config.setName("httpcache");
        Cache cache = new Cache(config);
        CacheManager instance = CacheManager.getInstance();
        instance.addCache(cache);
        cache.setCacheManager(instance);

        return cache;
    }
}
