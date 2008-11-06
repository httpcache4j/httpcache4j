package org.codehaus.httpcache4j;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class MIMETypeTest {

    @Test
    public void testValidMimeType() {
        MIMEType type = new MIMEType("foo/bar");
        assertTrue(type.matches("foo/bar"));
        MIMEType newType = new MIMEType("foo", "bar");
        assertEquals(type, newType);
    }

    @Test
    public void testInValidMimeType() {
        try {
            new MIMEType("foobar");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            //expected
        }
    }

    @Test
    public void testValidMimeTypeWithParameter() {
        MIMEType type = new MIMEType("foo/bar;charset=UTF-8");
        assertEquals("Wrong number of parameters",1, type.getParameters().size());
        Parameter param = type.getParameters().get(0);
        assertEquals("Wrong parameter name", "charset", param.getName());
        assertEquals("Wrong parameter value", "UTF-8", param.getValue());
        MIMEType newType = new MIMEType("foo", "bar");
        newType.addParameter("charset", "UTF-8");
        assertTrue("New type did not match old type",newType.matches(type));
    }

    @Test
    public void testValidMimeTypeWithInvalidParameter() {
        try {
            new MIMEType("foo/bar;charset?UTF-8");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            //expected
        }

    }
}
