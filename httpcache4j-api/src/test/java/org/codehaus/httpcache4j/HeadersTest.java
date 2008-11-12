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

package org.codehaus.httpcache4j;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
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

    @Test
    public void testHasCacheHeaders() {
        headers = new Headers();
        assertFalse("There was cacheable headers in an empty header map",HTTPUtils.hasCacheableHeaders(headers));
        headers.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        assertTrue("There was no cacheable headers", HTTPUtils.hasCacheableHeaders(headers));
        headers = new Headers();
        headers.add(HTTPUtils.toHttpDate(HeaderConstants.EXPIRES, new DateTime()));
        assertTrue("There was no cacheable headers", HTTPUtils.hasCacheableHeaders(headers));
        headers = new Headers();
        headers.add(new Header(HeaderConstants.PRAGMA, "private"));
        assertFalse("There was cacheable headers", HTTPUtils.hasCacheableHeaders(headers));
        headers = new Headers();
        headers.add(new Header(HeaderConstants.PRAGMA, "private"));
        headers.add(new Header(HeaderConstants.ETAG, "\"foo\""));
        assertTrue("There was no cacheable headers", HTTPUtils.hasCacheableHeaders(headers));
    }
}
