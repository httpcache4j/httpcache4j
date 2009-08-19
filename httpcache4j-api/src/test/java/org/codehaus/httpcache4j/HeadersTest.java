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

import static org.junit.Assert.*;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

/** @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a> */
public class HeadersTest {

    @Test
    public void testAddSimpleHeader() {
        Headers headers = new Headers().add(new Header("foo", "bar"));
        assertNotNull("Header list was null", headers.getHeaders("foo"));
        assertEquals("Header list was null", 1, headers.getHeaders("foo").size());
        assertEquals("Header was not equal", new Header("foo", "bar"), headers.getFirstHeader("foo"));
    }

    @Test
    public void testDateHeader() {
        DateTime now = new DateTime(2008, 10, 12, 15, 0, 0, 0, DateTimeZone.forID("UTC"));
        Header header = HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, now);
        assertNotNull("Header was null", header);
        assertEquals("Sun, 12 Oct 2008 15:00:00 GMT", header.getValue());
        assertEquals(now.getMillis(), HeaderUtils.getHeaderAsDate(header));
    }

    @Test
    public void testParseDateHeader() {
        String value = "Fri, 20 Feb 2009 12:26:45 GMT";
        DateTime dateTime = HeaderUtils.fromHttpDate(new Header(HeaderConstants.DATE, value));
        assertNotNull(dateTime);
        assertEquals(value, HeaderUtils.toHttpDate(HeaderConstants.DATE, dateTime).getValue());
    }

    @Test
    public void testParseDirectives() {
        Header header = new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60");
        assertNotNull(header.getDirectives());
        assertEquals(2, header.getDirectives().size());
        assertEquals("60", header.getDirectives().get("max-age"));
    }

    @Test
    public void testHeadersEquals() {
        Headers h = new Headers().add(HeaderConstants.ACCEPT, "application/xhtml").add("Foo", "Bar").add(HeaderConstants.AGE, "23");
        Headers h2 = new Headers().add(HeaderConstants.ACCEPT, "application/xhtml").add("Foo", "Bar").add(HeaderConstants.AGE, "23");
        assertEquals(h, h2);
    }

    @Test
    public void testHeadersEquals2() {
        Headers h = new Headers().add("Foo", "Bar").add(HeaderConstants.AGE, "23").add(HeaderConstants.ACCEPT, "application/xhtml");
        Headers h2 = new Headers().add(HeaderConstants.ACCEPT, "application/xhtml").add("Foo", "Bar").add(HeaderConstants.AGE, "23");
        assertEquals(h, h2);
    }

    @Test
    public void testNOTHeadersEquals() {
        Headers h = new Headers().add("Foo", "Bar").add(HeaderConstants.AGE, "23").add(HeaderConstants.ACCEPT, "application/xhtml");
        Headers h2 = new Headers();
        assertFalse(h.equals(h2));
    }

    @Test
    public void testHasCacheHeaders() {
        Headers headers = new Headers();
        assertFalse("There was cacheable headers in an empty header map", HeaderUtils.hasCacheableHeaders(headers));
        headers = headers.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        assertFalse("There was no cacheable headers", HeaderUtils.hasCacheableHeaders(headers));
        headers = new Headers().add(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, new DateTime()));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, new DateTime()));
        assertTrue("There was no cacheable headers", HeaderUtils.hasCacheableHeaders(headers));
        headers = new Headers().add(new Header(HeaderConstants.PRAGMA, "private"));
        assertFalse("There was cacheable headers", HeaderUtils.hasCacheableHeaders(headers));
        headers = new Headers();
        headers = headers.add(new Header(HeaderConstants.CACHE_CONTROL, "private"));
        headers = headers.add(new Header(HeaderConstants.ETAG, "\"foo\""));
        assertTrue("There was no cacheable headers", HeaderUtils.hasCacheableHeaders(headers));
    }
}
