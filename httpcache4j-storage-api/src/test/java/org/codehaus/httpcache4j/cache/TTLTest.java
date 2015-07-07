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

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class TTLTest {
    private int DEFAULT_TTL = 10; //seconds
    private LocalDateTime storageTime = LocalDateTime.of(2008, 10, 12, 15, 0, 0, 0);
    private LocalDateTime dateTime = storageTime;
    private LocalDateTime now = LocalDateTime.of(2008, 10, 12, 15, 10, 0, 0);

    @Before
    public void before() {
        setClock(now);
    }

    private Clock setClock(LocalDateTime dt) {
        return Clock.fixed(dt.toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));
    }

    @Test
    public void testDefaultTTL() {
        long ttl = DefaultCacheItem.getTTL(new HTTPResponse(Status.OK, createDefaultHeaders()), DEFAULT_TTL);
        Assert.assertEquals(DEFAULT_TTL, ttl);
    }

    @Test
    public void testDefaultTTLWith0MaxAge() {
        long ttl = DefaultCacheItem.getTTL(new HTTPResponse(Status.OK, createDefaultHeaders().add(HeaderConstants.CACHE_CONTROL, "max-age=0")), DEFAULT_TTL);
        Assert.assertEquals(DEFAULT_TTL, ttl);
    }

    @Test
    public void test100MaxAge() {
        long ttl = DefaultCacheItem.getTTL(new HTTPResponse(Status.OK, createDefaultHeaders().add(HeaderConstants.CACHE_CONTROL, "max-age=100")), DEFAULT_TTL);
        Assert.assertEquals(100, ttl);
    }

    @Test
    public void testDefaultTTLWith10MaxAgeAndExpires() {
        final LocalDateTime expires = now.plusYears(1);
        final Headers headers = createDefaultHeaders().add(HeaderConstants.CACHE_CONTROL, "max-age=10").add(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, expires));
        long ttl = DefaultCacheItem.getTTL(new HTTPResponse(Status.OK, headers), DEFAULT_TTL);
        Assert.assertEquals(DEFAULT_TTL, ttl);
    }

    @Test
    public void testDefaultTTLExpires() {
        final LocalDateTime expires = now.plusYears(1);
        final Headers headers = createDefaultHeaders().add(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, expires));
        long ttl = DefaultCacheItem.getTTL(new HTTPResponse(Status.OK, headers), DEFAULT_TTL);
        Assert.assertEquals(31536600, ttl);
    }

    private Headers createDefaultHeaders() {
        return new Headers().add(HeaderUtils.toHttpDate(HeaderConstants.DATE, dateTime));
    }
}
