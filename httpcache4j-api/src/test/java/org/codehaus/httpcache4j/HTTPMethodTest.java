package org.codehaus.httpcache4j;

import net.hamnaberg.funclite.CollectionOps;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.codehaus.httpcache4j.HTTPMethod.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class HTTPMethodTest {
       static Set<HTTPMethod> defaultMethods =  CollectionOps.setOf(
            CONNECT,
            DELETE,
            GET,
            HEAD,
            OPTIONS,
            PATCH,
            POST,
            PURGE,
            PUT,
            TRACE
    );

    @Test
    public void testDefaultMethods() {
        Set<String> methods = CollectionOps.setOf(
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
        Assert.assertEquals(defaultMethods, actual);
        Iterator<HTTPMethod> defaultIterator = defaultMethods.iterator();
        Iterator<HTTPMethod> actualIterator = actual.iterator();
        while(actualIterator.hasNext()) {
            HTTPMethod m = defaultIterator.next();
            HTTPMethod am = actualIterator.next();
            Assert.assertSame(m, am);
        }
    }

    @Test
    public void testUnknown() {
        Assert.assertFalse(defaultMethods.contains(HTTPMethod.valueOf("UNKNOWN")));
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
