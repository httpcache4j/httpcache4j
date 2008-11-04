package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Assert;
import org.junit.Test;

public class CacheItemTest {
    private CacheItem item;
    private DateTime storageTime = new DateTime(2008, 10, 12, 15, 0, 0, 0);
    private DateTime now = new DateTime(2008, 10, 12, 15, 10, 0, 0);

    public void setupItem(Headers headers) {
        DateTimeUtils.setCurrentMillisFixed(storageTime.getMillis());
        item = new CacheItem(new HTTPResponse(null, Status.OK, headers));
        DateTimeUtils.setCurrentMillisFixed(now.getMillis());
    }

    @Test
    public void testIsNotStale() {
        Headers headers = new Headers();
        headers.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=3600"));
        setupItem(headers);
        Assert.assertTrue("Item was stale", !item.isStale());
    }

    @Test
    public void testIsStale() {
        Headers headers = new Headers();
        headers.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        setupItem(headers);
        Assert.assertTrue("Item was stale", item.isStale());
    }

    @Test
    public void testIsStaleExpiresHeader() {
        Headers headers = new Headers();
        headers.add(HTTPUtils.toHttpDate(HeaderConstants.EXPIRES, now));
        setupItem(headers);
        Assert.assertTrue("Item was not stale", item.isStale());
    }

    @Test
    public void testIsNotStaleExpiresHeader() {
        Headers headers = new Headers();
        DateTime future = new DateTime(now);
        future = future.plusHours(1);
        headers.add(HTTPUtils.toHttpDate(HeaderConstants.EXPIRES, future));
        setupItem(headers);
        Assert.assertTrue("Item was stale", !item.isStale());
    }

    @Test
    public void testIsStaleExpiresHeaderWithInvalidDate() {
        Headers headers = new Headers();
        headers.add(new Header(HeaderConstants.EXPIRES, "foo"));
        setupItem(headers);
        Assert.assertTrue("Item was stale", item.isStale());
    }

    @Test
    public void testIsStaleExpiresHeaderWithCacheControl() {
        Headers headers = new Headers();
        //This should be preferred by the isStale method.
        headers.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        DateTime future = new DateTime(now);
        future = future.plusHours(1); //We now say that the expires is not stale.
        headers.add(HTTPUtils.toHttpDate(HeaderConstants.EXPIRES, future));
        setupItem(headers);
        Assert.assertTrue("Item was stale", item.isStale());
    }
}
