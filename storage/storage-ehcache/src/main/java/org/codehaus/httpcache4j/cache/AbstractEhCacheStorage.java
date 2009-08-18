/*
 * Copyright (c) 2009. The Codehaus. All Rights Reserved.
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
 */

package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.HTTPRequest;
import org.joda.time.DateTime;
import net.sf.ehcache.Element;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

import java.net.URI;
import java.util.Iterator;

/**
 * @author <a href="mailto:erlend@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public abstract class AbstractEhCacheStorage extends AbstractMapBasedCacheStorage {
    private final CacheManager cacheManager;
    protected final Ehcache cache;

    public AbstractEhCacheStorage(final Ehcache cache) {
        cacheManager = new CacheManager();
        cacheManager.addCache(cache);
        this.cache = cacheManager.getEhcache(cache.getName());
    }

    protected HTTPResponse putImpl(Key key, DateTime requestTime, HTTPResponse response) {
        cache.put(new Element(key, new CacheItem(response)));
        return response;
    }

    protected void invalidate(Key key) {
        cache.remove(key);
    }

    protected HTTPResponse get(Key key) {
        return null;
    }

    public CacheItem get(HTTPRequest request) {
        for (Key key : this) {
            if (key.getURI().equals(request.getRequestURI()) && key.getVary().matches(request)) {
                return (CacheItem) cache.get(key).getObjectValue();
            }
        }
        return null;
    }

    public void invalidate(URI uri) {
        for (Key key : this) {
           if (key.getURI().equals(uri)) {
               cache.remove(key);
           }
        }
    }

    public void clear() {
        cache.removeAll();
    }

    public int size() {
        return cache.getSize();
    }

    public Iterator<Key> iterator() {
        return cache.getKeys().iterator();
    }
}
