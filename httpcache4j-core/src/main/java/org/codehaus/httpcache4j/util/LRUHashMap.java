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

package org.codehaus.httpcache4j.util;

import java.util.LinkedHashMap;
import java.util.Map;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
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
