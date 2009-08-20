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

import org.codehaus.httpcache4j.util.CacheStatisticsMXBean;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
class CacheStatistics implements CacheStatisticsMXBean {
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();

    public long getHits() {
        return hits.get();
    }

    public void hit() {
        hits.getAndIncrement();
    }

    public void miss() {
        misses.getAndIncrement();
    }

    public long getMisses() {
        return misses.get();
    }

    public void clear() {
        hits.set(0L);
        misses.set(0L);
    }
}
