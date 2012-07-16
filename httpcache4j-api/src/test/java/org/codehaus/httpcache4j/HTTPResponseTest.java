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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.io.CharStreams;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.payload.StringPayload;
import org.junit.Test;

import javax.annotation.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.junit.Assert.*;

/** @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a> */
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
        Headers headers = new Headers().add(new Header("ETag", "\"abba\""));
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
        Headers headers = new Headers().add(new Header("ETag", "\"abba\""));
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
        Headers headers = new Headers().add(HeaderConstants.ALLOW, "GET, POST, OPTIONS");
        HTTPResponse response = new HTTPResponse(null, Status.OK, headers);
        assertEquals(3, response.getAllowedMethods().size());
        assertNull(response.getLastModified());
        assertNull(response.getPayload());
        assertFalse(response.hasPayload());
    }

    @Test
    public void responseShouldHaveCachedValueSet() {
        Headers headers = new Headers().add(CacheHeaderBuilder.getBuilder().createHITXCacheHeader());
        HTTPResponse response = new HTTPResponse(null, Status.OK, headers);
        assertTrue(response.isCached());
    }

    @Test
    public void transformShouldGiveUseSomethingUseful() {
        HTTPResponse response = new HTTPResponse(new StringPayload("Hello", MIMEType.valueOf("text/plain")), Status.OK, new Headers());
        Optional<String> result = response.transform(new Function<Payload, String>() {
            @Override
            public String apply(Payload input) {
                assertEquals(MIMEType.valueOf("text/plain"), input.getMimeType());
                try {
                    return CharStreams.toString(new InputStreamReader(input.getInputStream()));
                } catch (IOException e) {
                    fail("Exception raised when parsing string");
                    throw new RuntimeException(e);
                }
            }
        });

        assertTrue(result.isPresent());
        assertEquals("Hello", result.get());
    }
}
