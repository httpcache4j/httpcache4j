package org.codehaus.httpcache4j.util;

import org.codehaus.httpcache4j.cache.CacheItem;
import org.codehaus.httpcache4j.cache.Key;
import org.codehaus.httpcache4j.cache.MemoryCacheStorage;
import org.codehaus.httpcache4j.cache.Vary;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public class InvalidateOnRemoveLRUHashMap extends LinkedHashMap<URI, Map<Vary, CacheItem>> {
    private static final long serialVersionUID = -8600084275381371031L;
    private final int capacity;
    private transient RemoveListener listener;

    public InvalidateOnRemoveLRUHashMap(final int capacity) {
        super(capacity);
        this.capacity = capacity;
    }

    public InvalidateOnRemoveLRUHashMap(InvalidateOnRemoveLRUHashMap map) {
        super(map);
        this.capacity = map.capacity;
        this.listener = map.listener;
    }

    public InvalidateOnRemoveLRUHashMap copy() {
        return new InvalidateOnRemoveLRUHashMap(this);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<URI, Map<Vary, CacheItem>> eldest) {
        return size() > capacity;
    }

    @Override
    @Deprecated
    public Map<Vary, CacheItem> remove(Object key) {
        throw new IllegalArgumentException("Use remove(Key) instead");
    }

    public CacheItem remove(Key key) {
        Map<Vary, CacheItem> varyCacheItemMap = super.get(key.getURI());
        if (varyCacheItemMap != null) {
            CacheItem item = varyCacheItemMap.remove(key.getVary());
            if (listener != null) {
                listener.onRemoveFromMap(key);
            }
            if (varyCacheItemMap.isEmpty()) {
                super.remove(key.getURI());
            }
            return item;
        }
        return null;
    }

    public void remove(URI uri) {
        Map<Vary, CacheItem> varyCacheItemMap = super.get(uri);
        if (varyCacheItemMap != null) {
            for (Vary vary : varyCacheItemMap.keySet()) {
                Key key = Key.create(uri, vary);
                if (listener != null) {
                    listener.onRemoveFromMap(key);
                }
            }
            super.remove(uri);
        }
    }

    public void setListener(RemoveListener listener) {
        this.listener = listener;
    }

    public static interface RemoveListener {
        public void onRemoveFromMap(Key key);
    }
}
