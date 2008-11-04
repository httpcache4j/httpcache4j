package org.codehaus.httpcache4j.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class LRUHashMap<K, V> extends LinkedHashMap<K, V> {
    private int maxSize;

    public LRUHashMap(int initialCapacity) {
        this(initialCapacity, 0.75f);
        maxSize = initialCapacity;
    }

    public LRUHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true);
    }

    public LRUHashMap() {
        this(100);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return isFull();
    }

    public boolean isFull() {
        return size() == maxSize;
    }
}
