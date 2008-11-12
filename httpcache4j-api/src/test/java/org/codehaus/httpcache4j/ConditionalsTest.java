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
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ConditionalsTest {
    private Conditionals conditionals;

    @Before
    public void setupConditionals() {
        conditionals = new Conditionals();
    }

    @Test
    public void testIfMatch() {
        conditionals.addIfMatch(Tag.parse("\"foo\""));
        assertEquals(1, conditionals.getMatch().size());
        conditionals.addIfMatch(Tag.parse("\"bar\""));
        assertEquals(2, conditionals.getMatch().size());
        Header header = new Header(HeaderConstants.IF_MATCH, "\"foo\", \"bar\"");
        assertEquals(header, conditionals.toHeaders().getFirstHeader(HeaderConstants.IF_MATCH));
    }

    @Test
    public void testIfMatchWithNullTag() {
        conditionals.addIfMatch(null);
        assertEquals(1, conditionals.getMatch().size());

        try {
            conditionals.addIfMatch(Tag.parse("\"bar\""));
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            //expected
        }
        assertEquals(1, conditionals.getMatch().size());
    }

    @Test
    public void testIfNoneMatchWithNullTag() {
        conditionals.addIfNoneMatch(null);
        assertEquals(1, conditionals.getNonMatch().size());
        try {
            conditionals.addIfNoneMatch(Tag.parse("\"bar\""));
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            //expected
        }
        assertEquals(1, conditionals.getNonMatch().size());
    }

    @Test
    public void testIfMatchStar() {
        conditionals.addIfMatch(Tag.parse("*"));
        assertEquals(1, conditionals.getMatch().size());
        try {
            conditionals.addIfMatch(Tag.parse("\"bar\""));
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testIfNoneMatchStar() {
        conditionals.addIfNoneMatch(Tag.parse("*"));
        assertEquals(1, conditionals.getNonMatch().size());
        try {
            conditionals.addIfNoneMatch(Tag.parse("\"bar\""));
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testIfNoneMatch() {
        conditionals.addIfNoneMatch(Tag.parse("\"foo\""));
        assertEquals(1, conditionals.getNonMatch().size());
        conditionals.addIfNoneMatch(Tag.parse("\"bar\""));
        assertEquals(2, conditionals.getNonMatch().size());
        Header header = new Header(HeaderConstants.IF_NON_MATCH, "\"foo\", \"bar\"");
        assertEquals(header, conditionals.toHeaders().getFirstHeader(HeaderConstants.IF_NON_MATCH));
    }

    @Test
    public void testIfNoneMatchAndIfMatch() {
        conditionals.addIfNoneMatch(Tag.parse("\"foo\""));
        assertEquals(1, conditionals.getNonMatch().size());
        try {
            conditionals.addIfMatch(Tag.parse("\"bar\""));
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testIfModifiedSince() {
        DateTime dateTime = new DateTime();
        conditionals.setIfModifiedSince(dateTime);
        assertEquals(dateTime, conditionals.getModifiedSince());
        Header header = HTTPUtils.toHttpDate(HeaderConstants.IF_MODIFIED_SINCE, dateTime);
        assertEquals(header, conditionals.toHeaders().getFirstHeader(HeaderConstants.IF_MODIFIED_SINCE));
    }

    @Test
    public void testIfUnmodifiedSince() {
        DateTime dateTime = new DateTime();
        conditionals.setIfUnModifiedSince(dateTime);
        assertEquals(dateTime, conditionals.getUnModifiedSince());
        Header header = HTTPUtils.toHttpDate(HeaderConstants.IF_UNMODIFIED_SINCE, dateTime);
        assertEquals(header, conditionals.toHeaders().getFirstHeader(HeaderConstants.IF_UNMODIFIED_SINCE));
    }

    @Test
    public void testIfModifiedSinceAndIfMatch() {
        conditionals.addIfMatch(Tag.parse("\"bar\""));
        assertEquals(1, conditionals.getMatch().size());
        DateTime dateTime = new DateTime();
        try {
            conditionals.setIfModifiedSince(dateTime);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
        }
        assertNull(conditionals.getModifiedSince());
    }

    @Test
    public void testIfModifiedSinceAndIfNoneMatch() {
        conditionals.addIfNoneMatch(Tag.parse("\"bar\""));
        assertEquals(1, conditionals.getNonMatch().size());
        DateTime dateTime = new DateTime();
        conditionals.setIfModifiedSince(dateTime);
        assertEquals(dateTime, conditionals.getModifiedSince());
    }

    @Test
    public void testIfUnModifiedSinceAndIfMatch() {
        conditionals.addIfMatch(Tag.parse("\"bar\""));
        assertEquals(1, conditionals.getMatch().size());
        DateTime dateTime = new DateTime();
        conditionals.setIfUnModifiedSince(dateTime);
        assertEquals(dateTime, conditionals.getUnModifiedSince());
    }

    @Test
    public void testIfUnModifiedSinceAndIfNoneMatch() {
        conditionals.addIfNoneMatch(Tag.parse("\"bar\""));
        assertEquals(1, conditionals.getNonMatch().size());
        DateTime dateTime = new DateTime();
        try {
            conditionals.setIfUnModifiedSince(dateTime);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
        }
        assertNull(conditionals.getUnModifiedSince());
    }

    @Test
    public void testIfModifiedSinceAndIfUnmodifiedSince() {
        DateTime dateTime = new DateTime();
        conditionals.setIfUnModifiedSince(dateTime);
        assertEquals(dateTime, conditionals.getUnModifiedSince());
        try {
            conditionals.setIfModifiedSince(dateTime);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
        }
    }
}
