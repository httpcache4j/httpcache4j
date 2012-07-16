package org.codehaus.httpcache4j.util;


import java.io.ByteArrayInputStream;

/**
 * @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
 */
public class NullInputStream extends ByteArrayInputStream {
    public NullInputStream(int length) {
        super(new byte[length]);
    }
}
