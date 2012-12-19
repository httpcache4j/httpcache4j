/*
 * Copyright (c) 2010. The Codehaus. All Rights Reserved.
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
 */

package org.codehaus.httpcache4j.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class URIBuilderTest {
    @Test
    public void testConstructFromExistingURI() {
        URI uri = URI.create("http://www.example.com/a/path/here");
        URIBuilder.fromURI(uri);
    }

    @Test
    public void testEqualityFromExistingURI() {
        URI uri = URI.create("http://www.example.com/a/path/here");
        URIBuilder builder = URIBuilder.fromURI(uri);
        URI actual = builder.toURI();
        Assert.assertEquals("URIs did not match",uri, actual);
    }

    @Test
    public void testEqualityFromExistingURIWithQueryParameters() {
        URI uri = URI.create("http://www.example.com/a/path/here?foo=bar&bar=foo&baz=foo&bazz=s");
        URIBuilder builder = URIBuilder.fromURI(uri);
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
    }

    @Test
    public void testBuildPathFromBaseURI() {
        URI base = URI.create("http://www.example.com");
        URI uri = URI.create("http://www.example.com/a/path/here");
        URIBuilder builder = URIBuilder.fromURI(base).withPath("a", "path", "here");
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
    }

    @Test
    public void testBuildToBaseURI() {
        URI base = URI.create("http://www.example.com");
        URIBuilder builder = URIBuilder.empty();
        builder = builder.withScheme("http").withHost("www.example.com");
        Assert.assertEquals("URIs did not match", base, builder.toURI());
    }

    @Test
    public void testBuildWithPathToURI() {
        URI uri = URI.create("http://www.example.com/a/path/here");
        URIBuilder builder = URIBuilder.empty();
        builder = builder.withScheme("http").withHost("www.example.com").withPath("a", "path", "here");
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
    }

    @Test
    public void testBuildWithQueryParameters() {
        URI uri = URI.create("http://www.example.com/a/path/here?foo=bar&bar=foo");
        URIBuilder builder = URIBuilder.empty();
        builder = builder.withScheme("http").withHost("www.example.com").withPath("a", "path", "here").addParameter("foo", "bar").addParameter("bar", "foo");
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
    }

    @Test
    public void testNormalizedBuildWithQueryParameters() {
        URI uri = URI.create("http://www.example.com/a/path/here?bar=foo&foo=bar");
        URIBuilder builder = URIBuilder.empty();
        builder = builder.withScheme("http").withHost("www.example.com").withPath("a", "path", "here").addParameter("foo", "bar").addParameter("bar", "foo");
        Assert.assertEquals("URIs did not match", uri, builder.toNormalizedURI());
    }

    @Test
    public void testFromURIThenResetAndBuildWithQueryParameters() {
        URI uri = URI.create("http://www.example.com/a/path/here?foo=bar&bar=foo");
        URIBuilder builder = URIBuilder.fromURI(uri);
        builder = builder.withScheme("http").withHost("www.example.com").withPath("a", "path", "here").noParameters().addParameter("foo", "bar").addParameter("bar", "foo");
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
    }

    @Test
    public void testFromURIWithSingleParameter() {
        URI uri = URI.create("http://www.example.com/a/path/here?foo=bar");
        URIBuilder builder = URIBuilder.fromURI(uri);
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
    }

    @Test
    public void testBuildRelativeURI() {
        URI uri = URI.create("/a/relative/PATH");
        URIBuilder builder = URIBuilder.empty();
        builder = builder.withPath("a", "relative", "PATH");
        Assert.assertTrue(builder.isRelative());
        Assert.assertEquals("URIs did not match", uri, builder.toAbsoluteURI());
    }

    @Test
    public void testBuildRelativeURIWithMultiplePathInvocations() {
        URI uri = URI.create("/PATH");
        URIBuilder builder = URIBuilder.empty();
        builder = builder.withPath("a").withPath("relative").withPath("PATH");
        Assert.assertTrue(builder.isRelative());
        Assert.assertEquals("URIs did not match", uri, builder.toAbsoluteURI());
    }

    @Test
    public void testBuildWithAbsoultePathWeNeedToRetain() {
        URI uri = URI.create("/PATH");
        URIBuilder builder = URIBuilder.fromURI(uri);
        builder = builder.addPath("bar");
        Assert.assertTrue(builder.isRelative());
        Assert.assertEquals("URIs did not match", URI.create("/PATH/bar"), builder.toURI());
    }
    
    @Test
    public void testAddRawPath() {
        URI uri = URI.create("/PATH/boo/foo");
        URIBuilder builder = URIBuilder.empty();
        builder = builder.addRawPath("/PATH");
        builder = builder.addRawPath("boo/");
        builder = builder.addRawPath("foo");
        Assert.assertTrue(builder.isRelative());
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
    }

    @Test
    public void testRawPath() {
        URI uri = URI.create("/PATH/boo/foo");
        URIBuilder builder = URIBuilder.empty();
        builder = builder.withRawPath("/PATH/boo/foo");
        Assert.assertTrue(builder.isRelative());
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
    }

    @Test
    public void testBuildRelativeURIWithoutStartingSlash() {
        URI uri = URI.create("a/relative/PATH");
        URIBuilder builder = URIBuilder.empty();
        builder = builder.withPath("a", "relative", "PATH");
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
    }

    @Test
    public void testFromExistingURIWithEscapedChars() {
        URI uri = URI.create("http://example.com/A+Testing+Escaped");
        URIBuilder builder = URIBuilder.fromURI(uri);
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
        uri = URI.create("http://example.com/%2524+%2526+%253C+%253E+%253F+%253B+%2523+%253A+%253D+%252C+%2522+%2527+%257E+%252B");
        builder = URIBuilder.fromURI(uri);
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
    }

    @Test
    public void testFromExistingURIWithEscapedQueryString() {
        URI uri = URI.create("http://example.com?q=A+Testing+Escaped");
        URIBuilder builder = URIBuilder.empty();
        builder = builder.withScheme("http").withHost("example.com").addParameter("q", "A+Testing+Escaped");
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
        uri = URI.create("http://example.com?q=%2524+%2526+%253C+%253E+%253F+%253B+%2523+%253A+%253D+%252C+%2522+%2527+%257E+%252B");
        builder = URIBuilder.empty().withScheme("http").withHost("example.com").noParameters().addParameter("q", URIEncoder.encodeUTF8("$ & < > ? ; # : = , \" ' ~ +"));
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
    }

    @Test
    public void testFromExistingURIWithEscapedQueryStringWithPlusSign() {
        URI uri = URI.create("http://example.com?q=a%252Bb");
        URIBuilder builder = URIBuilder.empty();
        builder = builder.withScheme("http").withHost("example.com").addParameter("q", URIEncoder.encodeUTF8("a+b"));
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
    }    

    @Test
    public void testFromExistingQueryWithNullValue() {
        URI uri = URI.create("http://example.com?q=");
        URIBuilder builder = URIBuilder.empty().withScheme("http").withHost("example.com").addParameter("q", null);
        Assert.assertEquals("URIs did not match", uri, builder.toURI());
    }

    @Test
    public void uriEndsWithSlashMustBeRetained() {
        URI uri = URI.create("http://example.com/foo/");
        Assert.assertEquals("URIs did not match", uri, URIBuilder.fromURI(uri).toURI());
    }
}
