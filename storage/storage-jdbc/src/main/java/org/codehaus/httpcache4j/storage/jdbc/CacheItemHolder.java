/*
 * Copyright (c) 2010. The Codehaus. All Rights Reserved.
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

package org.codehaus.httpcache4j.storage.jdbc;

import org.codehaus.httpcache4j.cache.CacheItem;
import org.codehaus.httpcache4j.cache.Vary;

import java.net.URI;

/**
* @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
* @version $Revision: $
*/
class CacheItemHolder {
    private URI uri;
    private Vary vary;
    private CacheItem cacheItem;

    CacheItemHolder(URI uri, Vary vary, CacheItem response) {
        this.uri = uri;
        this.vary = vary;
        this.cacheItem = response;
    }

    public URI getUri() {
        return uri;
    }

    public Vary getVary() {
        return vary;
    }

    public CacheItem getCacheItem() {
        return cacheItem;
    }
}
