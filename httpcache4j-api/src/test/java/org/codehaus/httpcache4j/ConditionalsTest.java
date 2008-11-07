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
    public void testIfMatchStar() {
        conditionals.addIfMatch(Tag.parse("*"));
        assertEquals(1, conditionals.getMatch().size());
        try {
            conditionals.addIfMatch(Tag.parse("\"bar\""));
            fail();
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
            fail();
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
            fail();
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
            fail();
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
            fail();
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
            fail();
        }
        catch (IllegalArgumentException expected) {
        }
    }
}
