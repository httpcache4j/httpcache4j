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

import org.codehaus.httpcache4j.*;
import static org.codehaus.httpcache4j.HeaderConstants.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

import java.time.*;

public class CacheItemTest {
    private CacheItem item;
    private final LocalDateTime storageTime = LocalDateTime.of(2008, 10, 12, 15, 0, 0, 0);
    private final LocalDateTime now = storageTime.plusMinutes(1);
    private Clock clock;

    @Before
    public void before() {
        setClock(now);
    }

    private LocalDateTime newDate() {
        return LocalDateTime.now(clock);
    }

    private void setClock(LocalDateTime dt) {
        clock = Clock.fixed(dt.atZone(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
    }

    public void setupItem(Headers headers) {
        item = new DefaultCacheItem(new HTTPResponse(null, Status.OK, headers), storageTime);
    }

    @Test
    public void testIsNotStale() {
        Headers headers = createDefaultHeaders().add(new Header(CACHE_CONTROL, "private, max-age=3600"));
        setupItem(headers);
        Assert.assertFalse("Item was stale", item.isStale(newDate()));
        item = new DefaultCacheItem(item.getResponse(), now.minusMinutes(61));
        Assert.assertTrue("Item was not stale", item.isStale(newDate()));
    }

    private Headers createDefaultHeaders() {
        return new Headers().add(HeaderUtils.toHttpDate(DATE, newDate()));
    }

    @Test
    public void testIsStale() {
        Headers headers = createDefaultHeaders().add(new Header(CACHE_CONTROL, "private, max-age=60"));
        setupItem(headers);
        setClock(now.plusMinutes(1));
        Assert.assertTrue("Item was not stale", item.isStale(newDate()));
    }

    @Test
    public void testIsStaleExpiresHeader() {
        Headers headers = createDefaultHeaders().add(HeaderUtils.toHttpDate(EXPIRES, now));
        setupItem(headers);
        Assert.assertTrue("Item was not stale", item.isStale(newDate()));
    }

    @Test
    public void testIsNotStaleExpiresHeader() {
        Headers headers = createDefaultHeaders();
        LocalDateTime future = now.plusHours(1);
        headers = headers.add(HeaderUtils.toHttpDate(EXPIRES, future));
        setupItem(headers);
        Assert.assertTrue("Item was stale", !item.isStale(newDate()));
    }

    @Test
    public void testIsStaleEqualToDateHeader() {
        Headers headers = new Headers();
        LocalDateTime future = now.plusHours(1);
        headers = headers.add(HeaderUtils.toHttpDate(EXPIRES, future));
        headers = headers.add(HeaderUtils.toHttpDate(DATE, future));
        setupItem(headers);
        Assert.assertTrue("Item was stale", item.isStale(now));
    }

    @Test
    public void testIsStaleExpiresHeaderWithInvalidDate() {
        Headers headers = new Headers().add(new Header(EXPIRES, "foo"));
        setupItem(headers);
        Assert.assertTrue("Item was stale", item.isStale(now));
    }

    @Test
    public void testIsStaleExpiresHeaderWithCacheControl() {
        Headers headers = createDefaultHeaders();
        //This should be preferred by the isStale method.
        headers = headers.add(new Header(CACHE_CONTROL, "private, max-age=60"));
        LocalDateTime future = now.plusHours(24);
        headers = headers.add(HeaderUtils.toHttpDate(EXPIRES, future));
        setupItem(headers);
        Assert.assertTrue("Item was not stale", item.isStale(newDate()));
    }

    private LocalDateTime createDateTime(int seconds) {
        return now.plusSeconds(seconds);
    }

    @Test
    public void ageShouldBe10Seconds() {
        Headers headers = new Headers().add(HeaderUtils.toHttpDate("Date", now));
        LocalDateTime dateTime = createDateTime(10);
        setClock(dateTime);
        HTTPResponse cachedResponse = new HTTPResponse(null, Status.OK, headers);
        LocalDateTime requestTime = newDate();
        long age = new DefaultCacheItem(cachedResponse, now).getAge(requestTime);
        Assert.assertEquals(10, age);
    }
}
