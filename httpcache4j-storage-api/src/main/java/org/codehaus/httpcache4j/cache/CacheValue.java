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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public class CacheValue implements Iterable<Map.Entry<Vary, CacheItem>> {
    private Map<Vary, CacheItem> variations = new HashMap<Vary, CacheItem>();

    public CacheValue(final Map<Vary, CacheItem> pVariations) {
        Validate.notNull(pVariations, "Variations may not be null");
        variations.putAll(pVariations);
    }

    public Map<Vary, CacheItem> getVariations() {
        return variations;
    }

    public Iterator<Map.Entry<Vary, CacheItem>> iterator() {
        return getVariations().entrySet().iterator();
    }

}