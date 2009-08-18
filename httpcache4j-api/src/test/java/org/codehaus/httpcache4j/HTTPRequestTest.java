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

import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.*;

import java.net.URI;

/** @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a> */
public class HTTPRequestTest {
    @Test
    public void testNotSameObject() {
        HTTPRequest request = new HTTPRequest(URI.create("foo"));
        HTTPRequest request2 = request.addHeader(new Header("foo", "bar"));
        Assert.assertNotSame("Request objects were the same", request, request2);
        request = request.conditionals(new Conditionals().addIfNoneMatch(Tag.ALL));
        Assert.assertNotSame("Request objects were the same", request, request2);
        request2 = request.challenge(new UsernamePasswordChallenge("foo", "bar", ChallengeMethod.BASIC));
        Assert.assertNotSame("Request objects were the same", request, request2);
    }
}
