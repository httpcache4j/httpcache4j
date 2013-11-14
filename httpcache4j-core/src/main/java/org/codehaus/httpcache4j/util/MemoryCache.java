package org.codehaus.httpcache4j.util;

import org.codehaus.httpcache4j.cache.CacheItem;
import org.codehaus.httpcache4j.cache.Key;
import org.codehaus.httpcache4j.cache.Vary;

import java.net.URI;

public class MemoryCache extends LRUMap<URI, LRUMap<Vary, CacheItem>> {
    private static final long serialVersionUID = -8600084275381371031L;
    private KeyListener listener;

    public MemoryCache(final int capacity) {
        super(capacity);
    }

    public MemoryCache(MemoryCache map) {
        super(map, map.getCapacity());
        setKeyListener(map.listener);
    }

    public MemoryCache copy() {
        return new MemoryCache(this);
    }

    @Override
    public LRUMap<Vary, CacheItem> put(final URI uri, LRUMap<Vary, CacheItem> value) {
        value.addListener(new KeyModificationListener(uri));
        return super.put(uri, value);
    }

    public CacheItem remove(Key key) {
        LRUMap<Vary, CacheItem> varyCacheItemMap = super.get(key.getURI());
        if (varyCacheItemMap != null) {
            CacheItem item = varyCacheItemMap.remove(key.getVary());
            if (varyCacheItemMap.isEmpty()) {
                super.remove(key.getURI());
            }
            return item;
        }
        return null;
    }

    public void setKeyListener(final KeyListener listener) {
        this.listener = listener;
        if (listener != null) {
            addListener(new ListenerImpl(listener));
        }
        else {
            removeListeners();
        }
    }

    public static interface KeyListener {
        public void onRemove(Key key);
    }

    private static class ListenerImpl implements ModificationListener<URI, LRUMap<Vary, CacheItem>> {
        private final KeyListener listener;

        public ListenerImpl(KeyListener listener) {
            this.listener = listener;
        }

        @Override
        public void onPut(URI key, LRUMap<Vary, CacheItem> value) {
        }

        @Override
        public void onRemove(URI uri, LRUMap<Vary, CacheItem> value) {
            for (Vary vary : value.keySet()) {
                listener.onRemove(Key.create(uri, vary));
            }
            value.removeListeners();
        }
    }

    private class KeyModificationListener implements ModificationListener<Vary, CacheItem> {
        private final URI uri;

        public KeyModificationListener(URI uri) {
            this.uri = uri;
        }

        @Override
        public void onRemove(Vary key, CacheItem value) {
            if (listener != null) {
                Key k = Key.create(uri, key);
                listener.onRemove(k);
            }
        }

        @Override
        public void onPut(Vary key, CacheItem value) {
        }
    }
}
