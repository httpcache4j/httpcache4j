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
import org.codehaus.httpcache4j.HTTPResponse;

import java.net.URI;
import java.util.Map;
import java.util.Iterator;
import java.util.Collections;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public class NullCacheStorage implements CacheStorage {
    public HTTPResponse insert(Key key, HTTPResponse response) {
        return response;
    }

    public HTTPResponse update(Key key, HTTPResponse response) {
        return response;
    }

    public CacheItem get(final HTTPRequest request) {
        return null;
    }

    public void invalidate(Key key) {
    }

    public void invalidate(final URI uri) {
    }

    public void clear() {
    }

    public int size() {
        return 0;
    }

    public Iterator<Key> iterator() {
        return Collections.<Key>emptySet().iterator();
    }
}
