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
import static org.junit.Assert.*;

import java.net.URI;

/** @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a> */
public class HTTPRequestTest {
    @Test
    public void testParseQueryString() {
        URI uri = URI.create("http://foo.bar.com/?foo=bar&bar=com");
        HTTPRequest request = new HTTPRequest(uri, HTTPMethod.GET);
        assertEquals("Incorrect number of parameters", 2, request.getParameters().size());
        assertParameter(request.getParameters().get(0), "foo", "bar");
        assertParameter(request.getParameters().get(1), "bar", "com");
        uri = URI.create("http://foo.bar.com/?foo=bar&bar=com&baz=");
        request = new HTTPRequest(uri, HTTPMethod.GET);
        assertEquals("Incorrect number of parameters", 3, request.getParameters().size());
        assertParameter(request.getParameters().get(0), "foo", "bar");
        assertParameter(request.getParameters().get(1), "bar", "com");
        assertParameter(request.getParameters().get(2), "baz", "");
    }

    private void assertParameter(Parameter parameter, String expectedName, String expectedValue) {
        assertEquals("Wrong parameter name", expectedName, parameter.getName());
        assertEquals("Wrong parameter value", expectedValue, parameter.getValue());
    }
}
