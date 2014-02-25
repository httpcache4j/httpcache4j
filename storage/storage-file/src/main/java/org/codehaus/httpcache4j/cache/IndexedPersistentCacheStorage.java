package org.codehaus.httpcache4j.cache;

import com.google.common.cache.*;
import net.hamnaberg.funclite.CollectionOps;
import net.hamnaberg.funclite.Predicate;
import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.util.Pair;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class IndexedPersistentCacheStorage implements CacheStorage, RemovalListener<Key, CacheItem> {
    private final FilePersistentCacheStorage backing;
    private final Cache<Key, CacheItem> index;
    private final CacheLoader<Key, CacheItem> loader;

    public IndexedPersistentCacheStorage(File storageDir) {
        this(storageDir, 1000);
    }

    public IndexedPersistentCacheStorage(File storageDir, int maxSize) {
        backing = new FilePersistentCacheStorage(storageDir);
        loader = new CacheLoader<Key, CacheItem>() {
            @Override
            public CacheItem load(Key key) throws Exception {
                return backing.get(key);
            }
        };
        index = CacheBuilder.newBuilder().removalListener(this).maximumSize(maxSize).build(loader);
    }

    @Override
    public HTTPResponse insert(HTTPRequest request, HTTPResponse response) {
        Key key = Key.create(request, response);
        HTTPResponse inserted = backing.insert(request, response);
        index.put(key, new DefaultCacheItem(inserted));
        return inserted;
    }

    @Override
    public HTTPResponse update(HTTPRequest request, HTTPResponse response) {
        Key key = Key.create(request, response);
        HTTPResponse updated = backing.update(request, response);
        index.put(key, new DefaultCacheItem(updated));
        return updated;
    }

    @Override
    public CacheItem get(Key key) {
        try {
            return loader.load(key);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public CacheItem get(final HTTPRequest request) {
        Set<Key> keys = index.asMap().keySet();
        List<Key> filtered = CollectionOps.filter(keys, new Predicate<Key>() {
            @Override
            public boolean apply(Key input) {
                if (input.getURI().equals(request.getNormalizedURI())) {
                    return input.getVary().matches(request);
                }
                return false;
            }
        });
        if (filtered.isEmpty()) {
            Pair<Key, CacheItem> keyAndItem = backing.getItem(request);
            if (keyAndItem != null) {
                index.put(keyAndItem.getKey(), keyAndItem.getValue());
            }
        }
        return null;
    }

    @Override
    public void invalidate(final URI uri) {
        Set<Key> keys = index.asMap().keySet();
        List<Key> filtered = CollectionOps.filter(keys, new Predicate<Key>() {
            @Override
            public boolean apply(Key input) {
                return input.getURI().equals(uri);
            }
        });
        index.invalidateAll(filtered);
        backing.invalidate(uri);
    }

    @Override
    public void clear() {
        index.invalidateAll();
        backing.clear();
    }

    @Override
    public int size() {
        return (int) index.size();
    }

    @Override
    public Iterator<Key> iterator() {
        return index.asMap().keySet().iterator();
    }

    @Override
    public void onRemoval(RemovalNotification<Key, CacheItem> notification) {
        backing.invalidate(notification.getKey());
    }

    @Override
    public void shutdown() {
        backing.shutdown();
    }
}
