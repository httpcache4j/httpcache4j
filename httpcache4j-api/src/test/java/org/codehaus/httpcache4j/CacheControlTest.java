package org.codehaus.httpcache4j;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class CacheControlTest {

    @Test
    public void testPrivateMaxAge() {
        Header header = new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60");
        CacheControl control = new CacheControl(header.getDirectives());
        assertTrue(control.isPrivate());
        assertEquals(60, control.getMaxAge());
        assertEquals(-1, control.getMaxStale());
        assertEquals(-1, control.getMinFresh());
        assertEquals(-1, control.getSMaxAge());

        assertFalse(control.isPublic());
        assertFalse(control.isMustRevalidate());
        assertFalse(control.isNoCache());
        assertFalse(control.isNoStore());
        assertFalse(control.isNoTransform());
        assertFalse(control.isProxyRevalidate());
    }
    @Test
    public void testNoTransformMustRevalidate() {
        Header header = new Header(HeaderConstants.CACHE_CONTROL, "no-transform, must-revalidate");
        CacheControl control = new CacheControl(header.getDirectives());
        assertFalse(control.isPrivate());
        assertEquals(-1, control.getMaxAge());
        assertEquals(-1, control.getMaxStale());
        assertEquals(-1, control.getMinFresh());
        assertEquals(-1, control.getSMaxAge());

        assertFalse(control.isPublic());
        assertTrue(control.isMustRevalidate());
        assertFalse(control.isNoCache());
        assertFalse(control.isNoStore());
        assertTrue(control.isNoTransform());
        assertFalse(control.isProxyRevalidate());
    }

    @Test
    public void testNoCacheNoStore() {
        Header header = new Header(HeaderConstants.CACHE_CONTROL, "no-cache, no-store");
        CacheControl control = new CacheControl(header.getDirectives());
        assertEquals(-1, control.getMaxAge());
        assertEquals(-1, control.getMaxStale());
        assertEquals(-1, control.getMinFresh());
        assertEquals(-1, control.getSMaxAge());

        assertFalse(control.isPrivate());
        assertFalse(control.isPublic());
        assertFalse(control.isMustRevalidate());
        assertTrue(control.isNoCache());
        assertTrue(control.isNoStore());
        assertFalse(control.isNoTransform());
        assertFalse(control.isProxyRevalidate());
    }

    @Test
    public void testPublicMaxAge0WithMustRevalidate() {
        Header header = new Header(HeaderConstants.CACHE_CONTROL, "public, max-age=0, must-revalidate");
        CacheControl control = new CacheControl(header.getDirectives());
        assertEquals(0, control.getMaxAge());
        assertEquals(-1, control.getMaxStale());
        assertEquals(-1, control.getMinFresh());
        assertEquals(-1, control.getSMaxAge());

        assertFalse(control.isPrivate());
        assertTrue(control.isPublic());
        assertTrue(control.isMustRevalidate());
        assertFalse(control.isNoCache());
        assertFalse(control.isNoStore());
        assertFalse(control.isNoTransform());
        assertFalse(control.isProxyRevalidate());
    }

}
