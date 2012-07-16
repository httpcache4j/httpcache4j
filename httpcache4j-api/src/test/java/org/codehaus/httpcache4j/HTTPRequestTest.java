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

import org.codehaus.httpcache4j.payload.ClosedInputStreamPayload;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.util.NullInputStream;
import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.codehaus.httpcache4j.preference.Preferences;

import java.io.InputStream;
import java.net.URI;
import java.util.Locale;

/** @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a> */
public class HTTPRequestTest {
    private static final URI REQUEST_URI = URI.create("foo");

    @Test
    public void testNotSameObject() {
        HTTPRequest request = new HTTPRequest(REQUEST_URI);
        HTTPRequest request2 = request.addHeader(new Header("foo", "bar"));
        Assert.assertNotSame("Request objects were the same", request, request2);
        request = request.conditionals(new Conditionals().addIfNoneMatch(Tag.ALL));
        Assert.assertNotSame("Request objects were the same", request, request2);
        request2 = request.challenge(new UsernamePasswordChallenge("foo", "bar"));
        Assert.assertNotSame("Request objects were the same", request, request2);
    }

    @Test
    public void testAllHeaders() {
        Conditionals conditionals = new Conditionals().addIfMatch(new Tag("2345"));
        Preferences preferences = new Preferences().addLocale(Locale.UK).addMIMEType(MIMEType.valueOf("application/xml"));
        HTTPRequest request = new HTTPRequest(REQUEST_URI);
        request = request.addHeader("X-Foo-Bar", "Foo");
        request = request.conditionals(conditionals);
        request = request.preferences(preferences);
        Headers headers = new Headers().add("X-Foo-Bar", "Foo").add("If-Match", new Tag("2345").format()).add(HeaderConstants.ACCEPT_LANGUAGE, "en").add(HeaderConstants.ACCEPT, "application/xml");
        Assert.assertEquals(headers, request.getAllHeaders());
    }

    @Test
    public void issueHTJC123() {
        String mimeType = "application/atom+xml;type=entry";
        Headers headers = new Headers();
        headers = headers.add("Content-Type", mimeType);
        HTTPRequest request = new HTTPRequest(URI.create("http://example.com/"), HTTPMethod.POST);
        request = request.headers(headers);
        request = request.payload(new InputStreamPayload(new NullInputStream(10), new MIMEType(mimeType)));
        Headers all = request.getAllHeaders();
        assertEquals(1, all.getHeaders("Content-Type").size());
    }

    @Test(expected = IllegalStateException.class)
    public void testISEWhenSettingPayloadOnGETRequest() {
        HTTPRequest request = new HTTPRequest(REQUEST_URI);
        try {
            request.payload(new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM));
        } catch (IllegalStateException e) {
            Assert.assertTrue(e.getMessage().contains("GET"));
            throw e;
        }
    }

    @Test
    public void testSettingPayloadOnPUTAndPOSTRequestIsOK() {
        HTTPRequest request = new HTTPRequest(REQUEST_URI, HTTPMethod.POST);
        Payload payload = new ClosedInputStreamPayload(MIMEType.APPLICATION_OCTET_STREAM);
        request.payload(payload);
        request = new HTTPRequest(REQUEST_URI, HTTPMethod.PUT);
        request.payload(payload);
    }
}
