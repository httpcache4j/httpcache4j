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

import java.net.URI;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public class NullCacheStorage implements CacheStorage {
    public void put(final URI requestURI, final Vary vary, final CacheItem cacheItem) {
    }

    public CacheItem get(final HTTPRequest request) {
        return null;
    }

    public void invalidate(final URI uri) {
    }

    public void clear() {
    }

    public int size() {
        return 0;
    }

    public void invalidate(final URI requestURI, final CacheItem item) {        
    }
}
