package org.codehaus.httpcache4j.util;

import org.codehaus.httpcache4j.cache.CacheItem;
import org.codehaus.httpcache4j.cache.Key;
import org.codehaus.httpcache4j.cache.MemoryCacheStorage;

import java.util.LinkedHashMap;
import java.util.Map;

public class InvalidateOnRemoveLRUHashMap extends LinkedHashMap<Key, CacheItem> {
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
    protected boolean removeEldestEntry(Map.Entry<Key, CacheItem> eldest) {
        return size() > capacity;
    }

    @Override
    public CacheItem remove(Object key) {
        CacheItem remove = super.remove(key);
        if (listener != null) {
            listener.onRemoveFromMap((Key) key);
        }
        return remove;
    }

    public void setListener(RemoveListener listener) {
        this.listener = listener;
    }

    public static interface RemoveListener {
        public void onRemoveFromMap(Key key);
    }
}
