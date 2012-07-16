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

package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.Status;
import org.codehaus.httpcache4j.util.SerializationUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class KeyTest {

    @Test
    public void testSerializationWithEmptyVary() {
        Key key1 = Key.create(URI.create("foo"), new Vary());
        byte[] bytes = SerializationUtils.serialize(key1);
        Key key2 = (Key) SerializationUtils.deserialize(bytes);
        Assert.assertEquals(key1, key2);
    }

    @Test
    public void testSerializationOneItemInVary() {
        Key key1 = Key.create(URI.create("foo"), new Vary(Collections.singletonMap("Accept-Language", "en")));
        byte[] bytes = SerializationUtils.serialize(key1);
        Key key2 = (Key) SerializationUtils.deserialize(bytes);
        Assert.assertEquals(key1, key2);
    }

    @Test
    public void keyHasVariation() {
        Key key1 = Key.create(new HTTPRequest(URI.create("foo")).addHeader("Accept-Language", "en"), new HTTPResponse(null, Status.OK, new Headers().add("Vary", "Accept-Language")));
        Assert.assertEquals("en", key1.getVary().getVaryHeaders().get("Accept-Language"));
    }
}
