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
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;

public class CacheItemTest {
    private CacheItem item;
    private DateTime storageTime = new DateTime(2008, 10, 12, 15, 0, 0, 0);
    private DateTime now = storageTime.plusMinutes(1);

    @Before
    public void before() {
        DateTimeUtils.setCurrentMillisFixed(now.getMillis());

    }

    public void setupItem(Headers headers) {
        item = new CacheItem(new HTTPResponse(null, Status.OK, headers), storageTime);
    }

    @Test
    public void testIsNotStale() {
        Headers headers = createDefaultHeaders().add(new Header(CACHE_CONTROL, "private, max-age=3600"));
        setupItem(headers);
        Assert.assertFalse("Item was stale", item.isStale());
        item = new CacheItem(item.getResponse());
        DateTimeUtils.setCurrentMillisFixed(now.plusSeconds(3600).getMillis());
        Assert.assertTrue("Item was not stale", item.isStale());
    }

    private Headers createDefaultHeaders() {
        return new Headers().add(HeaderUtils.toHttpDate(DATE, new DateTime()));
    }

    @Test
    public void testIsStale() {
        Headers headers = createDefaultHeaders().add(new Header(CACHE_CONTROL, "private, max-age=60"));
        setupItem(headers);
        DateTimeUtils.setCurrentMillisFixed(now.plusMinutes(1).getMillis());
        Assert.assertTrue("Item was not stale", item.isStale());
    }

    @Test
    public void testIsStaleExpiresHeader() {
        Headers headers = createDefaultHeaders().add(HeaderUtils.toHttpDate(EXPIRES, now));
        setupItem(headers);
        Assert.assertTrue("Item was not stale", item.isStale());
    }

    @Test
    public void testIsNotStaleExpiresHeader() {
        Headers headers = createDefaultHeaders();
        DateTime future = new DateTime(now);
        future = future.plusHours(1);
        headers = headers.add(HeaderUtils.toHttpDate(EXPIRES, future));
        setupItem(headers);
        Assert.assertTrue("Item was stale", !item.isStale());
    }

    @Test
    public void testIsStaleEqualToDateHeader() {
        Headers headers = new Headers();
        DateTime future = new DateTime(now);
        future = future.plusHours(1);
        headers = headers.add(HeaderUtils.toHttpDate(EXPIRES, future));
        headers = headers.add(HeaderUtils.toHttpDate(DATE, future));
        setupItem(headers);
        Assert.assertTrue("Item was stale", item.isStale());
    }

    public void testIsStaleExpiresHeaderWithInvalidDate() {
        Headers headers = new Headers().add(new Header(EXPIRES, "foo"));
        setupItem(headers);
        Assert.assertTrue("Item was stale", item.isStale());
    }

    @Test
    public void testIsStaleExpiresHeaderWithCacheControl() {
        Headers headers = createDefaultHeaders();
        //This should be preferred by the isStale method.
        headers = headers.add(new Header(CACHE_CONTROL, "private, max-age=60"));
        DateTime future = new DateTime(now);
        future = future.plusHours(24); //We now say that the expires is not stale.
        headers = headers.add(HeaderUtils.toHttpDate(EXPIRES, future));
        setupItem(headers);
        Assert.assertTrue("Item was not stale", item.isStale());
    }
}
