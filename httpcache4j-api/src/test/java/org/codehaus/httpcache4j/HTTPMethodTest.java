package org.codehaus.httpcache4j;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.codehaus.httpcache4j.HTTPMethod.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class HTTPMethodTest {

    @Test
    public void testDefaultMethods() {
        List<String> methods = Arrays.asList(
                "connect",
                "DELEte",
                "geT",
                "HEAD",
                "OpTiOnS",
                "patch",
                "post",
                "pURGe",
                "put",
                "Trace"
        );
        Set<HTTPMethod> actual = new LinkedHashSet<HTTPMethod>();
        for (String method : methods) {
            actual.add(HTTPMethod.valueOf(method));
        }
        Assert.assertEquals(defaultMethods.size(), actual.size());
        ArrayList<HTTPMethod> expected = new ArrayList<>(defaultMethods.values());
        Collections.sort(expected);
        Iterator<HTTPMethod> defaultIterator = expected.iterator();
        Iterator<HTTPMethod> actualIterator = actual.iterator();
        while(actualIterator.hasNext()) {
            HTTPMethod m = defaultIterator.next();
            HTTPMethod am = actualIterator.next();
            Assert.assertSame(m, am);
        }
    }

    @Test
    public void testUnknown() {
        Assert.assertFalse(defaultMethods.containsValue(HTTPMethod.valueOf("UNKNOWN")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        HTTPMethod.valueOf(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() {
        HTTPMethod.valueOf("");
        HTTPMethod.valueOf(" ");
    }
}
