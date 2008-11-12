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

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.util.LRUHashMap;
import org.codehaus.httpcache4j.payload.Payload;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public class LRUMemoryCacheStorage extends MemoryCacheStorage {

    public LRUMemoryCacheStorage() {
        this(1000);
    }

    public LRUMemoryCacheStorage(int capacity) {
        cache = new InvalidatingLRUHashMap(capacity);
    }

    private class InvalidatingLRUHashMap extends LRUHashMap<URI, CacheValue> {
        public InvalidatingLRUHashMap(final int capacity) {
            super(capacity);
        }

        @Override
        public CacheValue remove(final Object key) {
            final CacheValue value = super.remove(key);
            invalidate(value, null);
            return value;
        }
    }
}