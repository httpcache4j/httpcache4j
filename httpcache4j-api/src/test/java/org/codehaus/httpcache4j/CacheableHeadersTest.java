package org.codehaus.httpcache4j;

import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: maedhros
 * Date: Nov 24, 2010
 * Time: 2:03:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class CacheableHeadersTest {

    @Test
    public void cacheControlprivateMaxAgeIsCacheable() {
        Headers headers = new Headers().add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        assertTrue("There was no cacheable headers", HeaderUtils.hasCacheableHeaders(headers));
    }

    @Test
    public void expiresAndDateSetToTheSameInstantIsNotCacheable() {
        Headers headers = new Headers().add(HeaderUtils.toHttpDate(HeaderConstants.DATE, new DateTime()));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, new DateTime()));
        assertFalse("There was cacheable headers", HeaderUtils.hasCacheableHeaders(headers));
    }

    @Test
    public void expiresSetTo0isNotCacheable() {
        Headers headers = new Headers().add(HeaderUtils.toHttpDate(HeaderConstants.DATE, new DateTime()));
        headers = headers.add(HeaderConstants.EXPIRES, "0");
        assertFalse("There was cacheable headers", HeaderUtils.hasCacheableHeaders(headers));
    }

    @Test
    public void expiresWithOutDateIsNotCacheable() {
        Headers headers = new Headers().add(HeaderConstants.EXPIRES, "0");
        assertFalse("There was cacheable headers", HeaderUtils.hasCacheableHeaders(headers));
    }

    @Test
    public void cacheControlNoStoreIsNotCacheable() {
        Headers headers = new Headers().add(new Header(HeaderConstants.CACHE_CONTROL, "no-store"));
        assertFalse("There was cacheable headers", HeaderUtils.hasCacheableHeaders(headers));
    }

    @Test
    public void cacheControlNoCacheIsNotCacheable() {
        Headers headers = new Headers().add(new Header(HeaderConstants.PRAGMA, "no-cache"));
        assertFalse("There was cacheable headers", HeaderUtils.hasCacheableHeaders(headers));
    }


    @Test
    public void onlyETagIsCacheable() {
        Headers headers = new Headers().add(HeaderConstants.ETAG, new Tag("foo").format());
        assertTrue("There was no cacheable headers", HeaderUtils.hasCacheableHeaders(headers));
    }

    @Test
    public void emptyHeadersIsNotCacheable() {
        Headers headers = new Headers();
        assertTrue("No headers is cacheable", HeaderUtils.hasCacheableHeaders(headers));
    }
}
