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

import org.codehaus.httpcache4j.mutable.MutableHeaders;
import org.codehaus.httpcache4j.util.AuthDirectivesParser;
import org.codehaus.httpcache4j.util.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/** @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a> */
public class HeadersTest {

    @Test
    public void testHeaderEquality() {
        Header header1 = new Header("foo", "bar");
        Header header2 = new Header("FOO", "bar");
        Header header3 = new Header("FoO", "bar");
        assertEquals(header1, header2);
        assertEquals(header1, header3);
        assertEquals(header2, header3);
        assertEquals(header1.hashCode(), header2.hashCode());
        assertEquals(header1.hashCode(), header3.hashCode());
        assertEquals(header3.hashCode(), header2.hashCode());
    }

    @Test
    public void testAddSimpleHeader() {
        Headers headers = new Headers().add(new Header("foo", "bar"));
        assertNotNull("Header list was null", headers.getHeaders("foo"));
        assertEquals("Header list was null", 1, headers.getHeaders("foo").size());
        assertEquals("Header was not equal", new Header("foo", "bar"), headers.getFirstHeader("foo").get());
    }

    @Test
    public void testDateHeader() {
        LocalDateTime now = LocalDateTime.of(2008, 10, 12, 15, 0, 0, 0);
        Header header = HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, now);
        assertNotNull("Header was null", header);
        assertEquals("Sun, 12 Oct 2008 15:00:00 GMT", header.getValue());
        assertEquals(now.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli(), HeaderUtils.getHeaderAsDate(header));
    }

    @Test
    public void testWronglyformattedDateHeader() {
        Optional<LocalDateTime> header = HeaderUtils.fromHttpDate(new Header(HeaderConstants.EXPIRES, "-1"));
        assertFalse("Header value was here", header.isPresent());
    }

    @Test
    public void testParseDateHeader() {
        String value = "Fri, 20 Feb 2009 12:26:45 GMT";
        Optional<LocalDateTime> dateTime = HeaderUtils.fromHttpDate(new Header(HeaderConstants.DATE, value));
        assertTrue("Incorrect formatted date", dateTime.isPresent());
        assertEquals(value, HeaderUtils.toHttpDate(HeaderConstants.DATE, dateTime.get()).getValue());
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
    public void makeSureWeCanParseToString() {
        Headers h = new Headers().add("Foo", "Bar").add(HeaderConstants.AGE, "23").add(HeaderConstants.ACCEPT, "application/xhtml");
        String string = h.toString();
        Headers h2 = Headers.parse(string);
        assertEquals(h, h2);
    }

    @Test
    public void containsHeaders() {
        Headers h = new Headers().add("Foo", "bar");
        assertTrue("did not contain", h.contains("FOO"));
        assertTrue("did not contain", h.contains("foo"));
        assertTrue("did not contain", h.contains("Foo"));
        assertTrue("did not contain", h.contains("FoO"));
        assertTrue("did not contain", h.contains("fOO"));
        assertFalse("did contain", h.contains("f00"));
    }

    @Test
    public void storeAndParseHeaders() {
        MutableHeaders headers = new MutableHeaders();
        headers.add("Content-Type", "text/plain");
        headers.add("Content-Length", "23");
        String string = headers.toString();
        Headers parsed = Headers.parse(string);
        assertEquals(headers.toHeaders(), parsed);
    }

    @Test
    public void multipleAuthChallenges() throws IOException {
        /**
         * Note: User agents will need to take special care in parsing the WWW-
         Authenticate or Proxy-Authenticate header field value if it contains
         more than one challenge, or if more than one WWW-Authenticate header
         field is provided, since the contents of a challenge may itself
         contain a comma-separated list of authentication parameters.
         */
        try (InputStream is = getClass().getResourceAsStream("/multiple-auth.txt")){
            String value = new String(IOUtils.toByteArray(is), StandardCharsets.ISO_8859_1);
            Iterable<Directive> parsed = AuthDirectivesParser.parse(value);
            Directives directives = new Directives(parsed);
            assertNotNull(directives.getAsDirective("Basic"));
            assertNotNull(directives.getAsDirective("Digest"));
        }
    }

}
