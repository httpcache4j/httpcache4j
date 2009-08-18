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

import org.junit.Test;
import org.junit.Assert;
import org.junit.Before;
import org.codehaus.httpcache4j.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.Years;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class TTLTest {
    private int DEFAULT_TTL = 10; //seconds
    private DateTime storageTime = new DateTime(2008, 10, 12, 15, 0, 0, 0);
    private DateTime dateTime = storageTime;
    private DateTime now = new DateTime(2008, 10, 12, 15, 10, 0, 0);

    @Before
    public void before() {
        DateTimeUtils.setCurrentMillisFixed(now.getMillis());
    }

    @Test
    public void testDefaultTTL() {
        long ttl = CacheItem.getTTL(new HTTPResponse(null, Status.OK, createDefaultHeaders()), DEFAULT_TTL);
        Assert.assertEquals(DEFAULT_TTL, ttl);
    }

    @Test
    public void testDefaultTTLWith0MaxAge() {
        long ttl = CacheItem.getTTL(new HTTPResponse(null, Status.OK, createDefaultHeaders().add(HeaderConstants.CACHE_CONTROL, "max-age=0")), DEFAULT_TTL);
        Assert.assertEquals(DEFAULT_TTL, ttl);
    }

    @Test
    public void test100MaxAge() {
        long ttl = CacheItem.getTTL(new HTTPResponse(null, Status.OK, createDefaultHeaders().add(HeaderConstants.CACHE_CONTROL, "max-age=100")), DEFAULT_TTL);
        Assert.assertEquals(100, ttl);
    }

    @Test
    public void testDefaultTTLWith10MaxAgeAndExpires() {
        final DateTime expires = now.plus(Years.years(1));
        final Headers headers = createDefaultHeaders().add(HeaderConstants.CACHE_CONTROL, "max-age=10").add(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, expires));
        long ttl = CacheItem.getTTL(new HTTPResponse(null, Status.OK, headers), DEFAULT_TTL);
        Assert.assertEquals(DEFAULT_TTL, ttl);
    }

    @Test
    public void testDefaultTTLExpires() {
        final DateTime expires = now.plus(Years.years(1));
        final Headers headers = createDefaultHeaders().add(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, expires));
        long ttl = CacheItem.getTTL(new HTTPResponse(null, Status.OK, headers), DEFAULT_TTL);
        Assert.assertEquals(31536600, ttl);
    }

    private Headers createDefaultHeaders() {
        return new Headers().add(HeaderUtils.toHttpDate(HeaderConstants.DATE, dateTime));
    }
}
