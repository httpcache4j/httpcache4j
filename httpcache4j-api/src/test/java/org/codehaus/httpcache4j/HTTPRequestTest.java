package org.codehaus.httpcache4j;

import org.junit.Test;
import static org.junit.Assert.*;

import java.net.URI;


/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
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
