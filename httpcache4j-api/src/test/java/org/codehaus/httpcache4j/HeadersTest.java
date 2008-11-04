package org.codehaus.httpcache4j;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class HeadersTest {
    private Headers headers;

    @Before
    public void setupHeaders() {
        headers = new Headers();
    }

    @Test
    public void testAddSimpleHeader() {
        headers.add(new Header("foo", "bar"));
        assertNotNull("Header list was null", headers.getHeaders("foo"));
        assertEquals("Header was not equal", new Header("foo", "bar"), headers.getFirstHeader("foo"));
    }

    @Test
    public void testDateHeader() {
        DateTime now = new DateTime(2008, 10, 12, 15, 0, 0, 0);
        Header header = HTTPUtils.toHttpDate(HeaderConstants.EXPIRES, now);
        assertNotNull("Header was null", header);
        DateTimeFormatter formatter = DateTimeFormat.forPattern(HTTPUtils.PATTERN_RFC1123);
        assertEquals(formatter.print(now), header.getValue());
        assertEquals(now.getMillis(), HTTPUtils.getHeaderAsDate(header));
    }

    @Test
    public void testParseDirectives() {
        Header header = new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60");
        assertNotNull(header.getDirectives());
        assertEquals(2, header.getDirectives().size());
        assertEquals("60", header.getDirectives().get("max-age"));
    }
}
