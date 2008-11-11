package org.codehaus.httpcache4j;

import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.*;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public class HTTPResponseTest {
    @Test
    public void testNoHeaders() {
        Headers headers = new Headers();
        HTTPResponse response = new HTTPResponse(null, Status.OK, headers);
        assertEquals(0, response.getAllowedMethods().size());
        assertNull(response.getETag());
        assertNull(response.getLastModified());
        assertNull(response.getPayload());
        assertFalse(response.hasPayload());
    }

    @Test
    public void testparseETagHeader() {
        Headers headers = new Headers();
        headers.add(new Header("ETag", "\"abba\""));
        HTTPResponse response = new HTTPResponse(null, Status.OK, headers);
        assertEquals(0, response.getAllowedMethods().size());
        assertNotNull(response.getETag());
        assertEquals(response.getETag().format(), "\"abba\"");
        assertNull(response.getLastModified());
        assertNull(response.getPayload());
        assertFalse(response.hasPayload());
    }

    @Test
    public void testETagHeader() {
        Headers headers = new Headers();
        headers.add(new Header("ETag", "\"abba\""));
        HTTPResponse response = new HTTPResponse(null, Status.OK, headers);
        assertEquals(0, response.getAllowedMethods().size());
        assertNotNull(response.getETag());
        assertEquals(response.getETag().format(), "\"abba\"");
        assertNull(response.getLastModified());
        assertNull(response.getPayload());
        assertFalse(response.hasPayload());
    }

    @Test
    public void testAllowHeaders() {
        Headers headers = new Headers();
        headers.add(HeaderConstants.ALLOW, "GET, POST, OPTIONS");
        HTTPResponse response = new HTTPResponse(null, Status.OK, headers);
        assertEquals(3, response.getAllowedMethods().size());
        assertNull(response.getLastModified());
        assertNull(response.getPayload());
        assertFalse(response.hasPayload());
    }
}
