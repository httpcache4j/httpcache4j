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

/** @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a> */
public class MIMETypeTest {

    @Test
    public void testValidMimeType() {
        MIMEType type = MIMEType.valueOf("foo/bar");
        MIMEType newType = MIMEType.valueOf("foo", "bar");
        assertEquals(type, newType);
    }

    @Test
    public void testInValidMimeType() {
        try {
            MIMEType.valueOf("foobar");
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
            //expected
        }
    }

    @Test
    public void testValidMimeTypeWithParameter() {
        MIMEType type = MIMEType.valueOf("foo/bar;charset=UTF-8");
        assertEquals("Wrong number of parameters", 1, type.getParameters().size());
        Parameter param = type.getParameters().get(0);
        assertEquals("Wrong parameter name", "charset", param.getName());
        assertEquals("Wrong parameter value", "UTF-8", param.getValue());
        MIMEType newType = MIMEType.valueOf("foo", "bar").addParameter("charset", "UTF-8");
        assertEquals("New type did not match old type", newType, type);
    }

    @Test
    public void testValidMimeTypeWithParametersAndLotsofWhitespaceBetweenParamaters() {
        MIMEType type = MIMEType.valueOf("foo/bar;charset=UTF-8            \n;    random=true");
        assertEquals("Wrong number of parameters", 2, type.getParameters().size());
        Parameter param = type.getParameters().get(1);
        assertEquals("Wrong parameter name", "charset", param.getName());
        assertEquals("Wrong parameter value", "UTF-8", param.getValue());
        param = type.getParameters().get(0);
        assertEquals("Wrong parameter name", "random", param.getName());
        assertEquals("Wrong parameter value", "true", param.getValue());
        MIMEType newType = MIMEType.valueOf("foo", "bar").addParameter("random", "true").addParameter("charset", "UTF-8");
        assertEquals("New type did not match old type", newType, type);
    }

    @Test
    public void testValidMimeTypeWithInvalidParameter() {
        try {
            MIMEType.valueOf("foo/bar;charset?UTF-8");
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException expected) {
            //expected
        }
    }

    @Test
    public void testIncludes() {
        MIMEType type = MIMEType.valueOf("image/*");
        MIMEType jpegType = MIMEType.valueOf("image/jpg");
        assertTrue("Image type did not include jpeg type", type.includes(jpegType));
        type = MIMEType.ALL;
        assertTrue("All type did not include jpeg type", type.includes(jpegType));
        type = MIMEType.valueOf("image", "jpg");
        assertTrue("Same type was not the same as jpeg type", type.includes(jpegType));
        assertTrue("type type did not include null type", type.includes(null));
    }

    @Test
    public void testNotIncludes() {
        MIMEType type = MIMEType.valueOf("image/*");
        MIMEType jpegType = MIMEType.valueOf("image/jpg");
        assertFalse("jpeg type did include ALL image type", jpegType.includes(type));
        type = MIMEType.ALL;
        assertFalse("jpeg type included ALL type ", jpegType.includes(type));
        type = MIMEType.valueOf("video/*");
        assertFalse("jpeg type included ALL video type ", jpegType.includes(type));
    }
}
