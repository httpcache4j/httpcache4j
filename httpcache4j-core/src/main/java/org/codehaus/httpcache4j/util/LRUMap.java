package org.codehaus.httpcache4j.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class LRUMap<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;
    private transient CopyOnWriteArrayList<ModificationListener<K, V>> listeners = new CopyOnWriteArrayList<ModificationListener<K, V>>();

    public LRUMap(final int capacity) {
        super(capacity);
        this.capacity = capacity;
    }

    public LRUMap(Map<? extends K, ? extends V> m, int capacity) {
        super(m);
        this.capacity = capacity;
    }

    @Override
    protected final boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        boolean remove = size() > capacity;
        if (remove) {
            for (ModificationListener<K, V> listener : listeners) {
                listener.onRemove(eldest.getKey(), eldest.getValue());
            }
        }
        return remove;
    }

    public int getCapacity() {
        return capacity;
    }

    public void addListener(ModificationListener<K, V> listener) {
        listeners.add(listener);
    }

    public void removeListener(ModificationListener<K, V> listener) {
        listeners.remove(listener);
    }

    public List<ModificationListener<K, V>> getListeners() {
        return listeners;
    }

    public void removeListeners() {
        listeners.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        V remove = super.remove(key);
        if (remove != null) {
            for (ModificationListener<K, V> listener : listeners) {
                listener.onRemove((K)key, remove);
            }
        }
        return remove;
    }

    @Override
    public V put(K key, V value) {
        V put = super.put(key, value);
        for (ModificationListener<K, V> listener : listeners) {
            listener.onPut(key, value);
        }
        return put;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public static interface ModificationListener<K, V> {
        public void onPut(K key, V value);
        public void onRemove(K key, V value);
    }
}
