package org.codehaus.httpcache4j.util;


import java.io.ByteArrayInputStream;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class NullInputStream extends ByteArrayInputStream {
    public NullInputStream(int length) {
        super(new byte[length]);
    }
}
