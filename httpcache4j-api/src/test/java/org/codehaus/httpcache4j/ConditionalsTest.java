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
import org.junit.Test;

public class ConditionalsTest {
    @Test
    public void testIfMatch() {
        Conditionals conditionals = new Conditionals();
        conditionals = conditionals.addIfMatch(Tag.parse("\"foo\""));
        assertEquals(1, conditionals.getMatch().size());
        conditionals = conditionals.addIfMatch(Tag.parse("\"bar\""));
        assertEquals(2, conditionals.getMatch().size());
        Header header = new Header(HeaderConstants.IF_MATCH, "\"foo\",\"bar\"");
        assertEquals(header, conditionals.toHeaders().getFirstHeader(HeaderConstants.IF_MATCH));
    }

    @Test
    public void testIfMatchDuplicate() {
        Conditionals conditionals = new Conditionals();
        conditionals = conditionals.addIfMatch(Tag.parse("\"foo\""));
        conditionals = conditionals.addIfMatch(Tag.parse("\"foo\""));
        assertEquals(1, conditionals.getMatch().size());
    }

    @Test
    public void testIfNoneMatchDuplicate() {
        Conditionals conditionals = new Conditionals();
        conditionals = conditionals.addIfNoneMatch(Tag.parse("\"foo\""));
        conditionals = conditionals.addIfNoneMatch(Tag.parse("\"foo\""));
        assertEquals(1, conditionals.getNoneMatch().size());
    }

    @Test
    public void testIfMatchWithNullTag() {
        Conditionals conditionals = new Conditionals();
        conditionals = conditionals.addIfMatch(null);
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
        Conditionals conditionals = new Conditionals();
        conditionals = conditionals.addIfNoneMatch(null);
        assertEquals(1, conditionals.getNoneMatch().size());
        try {
            conditionals.addIfNoneMatch(Tag.parse("\"bar\""));
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            //expected
        }
        assertEquals(1, conditionals.getNoneMatch().size());
    }

    @Test
    public void testIfMatchStar() {
        Conditionals conditionals = new Conditionals();
        conditionals = conditionals.addIfMatch(Tag.parse("*"));
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
        Conditionals conditionals = new Conditionals();
        conditionals = conditionals.addIfNoneMatch(Tag.parse("*"));
        assertEquals(1, conditionals.getNoneMatch().size());
        try {
            conditionals.addIfNoneMatch(Tag.parse("\"bar\""));
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testIfNoneMatch() {
        Conditionals conditionals = new Conditionals();
        conditionals = conditionals.addIfNoneMatch(Tag.parse("\"foo\""));
        assertEquals(1, conditionals.getNoneMatch().size());
        conditionals = conditionals.addIfNoneMatch(Tag.parse("\"bar\""));
        assertEquals(2, conditionals.getNoneMatch().size());
        Header header = new Header(HeaderConstants.IF_NONE_MATCH, "\"foo\",\"bar\"");
        assertEquals(header, conditionals.toHeaders().getFirstHeader(HeaderConstants.IF_NONE_MATCH));
    }

    @Test
    public void testIfNoneMatchAndIfMatch() {
        Conditionals conditionals = new Conditionals();
        conditionals = conditionals.addIfNoneMatch(Tag.parse("\"foo\""));
        assertEquals(1, conditionals.getNoneMatch().size());
        try {
            conditionals.addIfMatch(Tag.parse("\"bar\""));
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testIfModifiedSince() {
        Conditionals conditionals = new Conditionals();
        DateTime dateTime = new DateTime();
        conditionals = conditionals.ifModifiedSince(dateTime);
        Header header = HeaderUtils.toHttpDate(HeaderConstants.IF_MODIFIED_SINCE, dateTime);
        assertEquals(header, conditionals.toHeaders().getFirstHeader(HeaderConstants.IF_MODIFIED_SINCE));
    }

    @Test
    public void testIfUnmodifiedSince() {
        Conditionals conditionals = new Conditionals();
        DateTime dateTime = new DateTime();
        conditionals = conditionals.ifUnModifiedSince(dateTime);
        Header header = HeaderUtils.toHttpDate(HeaderConstants.IF_UNMODIFIED_SINCE, dateTime);
        assertEquals(header, conditionals.toHeaders().getFirstHeader(HeaderConstants.IF_UNMODIFIED_SINCE));
    }

    @Test
    public void testIfModifiedSinceAndIfMatch() {
        Conditionals conditionals = new Conditionals();
        conditionals = conditionals.addIfMatch(Tag.parse("\"bar\""));
        assertEquals(1, conditionals.getMatch().size());
        DateTime dateTime = new DateTime();
        try {
            conditionals.ifModifiedSince(dateTime);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
        }
        assertNull(conditionals.getModifiedSince());
    }

    @Test
    public void testIfModifiedSinceAndIfNoneMatch() {
        Conditionals conditionals = new Conditionals();
        conditionals = conditionals.addIfNoneMatch(Tag.parse("\"bar\""));
        assertEquals(1, conditionals.getNoneMatch().size());
        DateTime dateTime = new DateTime();
        conditionals = conditionals.ifModifiedSince(dateTime);
        assertNotNull(conditionals.getModifiedSince());
    }

    @Test
    public void testIfUnModifiedSinceAndIfMatch() {
        Conditionals conditionals = new Conditionals();
        conditionals = conditionals.addIfMatch(Tag.parse("\"bar\""));
        assertEquals(1, conditionals.getMatch().size());
        DateTime dateTime = new DateTime();
        conditionals = conditionals.ifUnModifiedSince(dateTime);
        assertNotNull(conditionals.getUnModifiedSince());
    }

    @Test
    public void testIfUnModifiedSinceAndIfNoneMatch() {
        Conditionals conditionals = new Conditionals();
        conditionals = conditionals.addIfNoneMatch(Tag.parse("\"bar\""));
        assertEquals(1, conditionals.getNoneMatch().size());
        DateTime dateTime = new DateTime();
        try {
            conditionals.ifUnModifiedSince(dateTime);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
        }
        assertNull(conditionals.getUnModifiedSince());
    }

    @Test
    public void testIfModifiedSinceAndIfUnmodifiedSince() {
        Conditionals conditionals = new Conditionals();
        DateTime dateTime = new DateTime();
        conditionals = conditionals.ifUnModifiedSince(dateTime);
        assertNotNull(conditionals.getUnModifiedSince());
        try {
            conditionals.ifModifiedSince(dateTime);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
        }
    }

    @Test
    public void testImmutability() {
        Conditionals conditionals = new Conditionals();
        DateTime dateTime = new DateTime();
        Conditionals conditionals2 = conditionals.ifUnModifiedSince(dateTime);
        assertNotSame(conditionals, conditionals2);
        assertEquals(0, conditionals.getNoneMatch().size());
        assertEquals(0, conditionals.getMatch().size());
        assertNull(conditionals.getModifiedSince());
        assertNull(conditionals.getUnModifiedSince());

        assertEquals(0, conditionals2.getNoneMatch().size());
        assertEquals(0, conditionals2.getMatch().size());
        assertNull(conditionals2.getModifiedSince());
        assertNotNull(conditionals2.getUnModifiedSince());

    }
}
