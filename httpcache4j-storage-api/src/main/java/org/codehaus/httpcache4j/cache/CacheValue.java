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

import org.apache.commons.lang.Validate;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public class CacheValue implements Iterable<Map.Entry<Vary, CacheItem>>, Serializable {
    private final Map<Vary, CacheItem> variations = new ConcurrentHashMap<Vary, CacheItem>();
    private static final long serialVersionUID = 1589764737954233106L;

    public CacheValue(final Map<Vary, CacheItem> pVariations) {
        Validate.notNull(pVariations, "Variations may not be null");
        variations.putAll(pVariations);
    }

    public Iterator<Map.Entry<Vary, CacheItem>> iterator() {
        return Collections.unmodifiableMap(variations).entrySet().iterator();
    }

    public void remove(Vary vary) {
        variations.remove(vary);
    }

    public boolean isEmpty() {
        return variations.isEmpty();
    }

    public void add(Vary vary, CacheItem cacheItem) {
        variations.put(vary, cacheItem);
    }

    public int size() {
        return variations.size();
    }
}