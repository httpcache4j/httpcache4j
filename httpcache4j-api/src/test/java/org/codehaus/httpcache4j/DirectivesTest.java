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

package org.codehaus.httpcache4j;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DirectivesTest {
    @Test
    public void testNoDirectives() {
        Directives dir = new Directives();
        assertEquals(0, dir.size());
    }

    @Test
    public void testSingleDirectiveWithNoValue() {
        Directives dir = new Directives("value");        
        assertEquals(1, dir.size());
        assertEquals("", dir.get("value"));
    }

    @Test
    public void testMultipleDirectives() {
        Directives dir = new Directives("key=value,bar=foo, foo=\"bar\"");        
        assertEquals(3, dir.size());
        assertEquals("value", dir.get("key"));
        assertEquals("foo", dir.get("bar"));
        assertEquals("bar", dir.get("foo"));
    }

    @Test
    public void testMultipleDirectivesToString() {
        String value = "key=value, bar=foo, foo=\"bar\"";
        Directives dir = new Directives(value);
        assertEquals(3, dir.size());
        assertEquals("value", dir.get("key"));
        assertEquals("foo", dir.get("bar"));
        assertEquals("bar", dir.get("foo"));
        assertEquals(value, dir.toString());
    }

    @Test
    public void testSingleDirectiveWithQuotedValueWithComma() {
        Directives dir = new Directives("foo=\"bar,baz\",bar=foo");
        assertEquals(2, dir.size());
        assertEquals("bar,baz", dir.get("foo"));
    }
}
