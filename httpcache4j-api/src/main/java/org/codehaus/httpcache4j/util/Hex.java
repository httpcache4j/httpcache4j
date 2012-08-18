package org.codehaus.httpcache4j.util;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class Hex {

    public static String encode(byte[] bytes) {
        // TODO(user): Use c.g.common.base.ByteArrays once it is open sourced.
        StringBuilder sb = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            sb.append(hexDigits[(b >> 4) & 0xf]).append(hexDigits[b & 0xf]);
        }
        return sb.toString();
    }
    private static final char[] hexDigits = "0123456789abcdef".toCharArray();
}
