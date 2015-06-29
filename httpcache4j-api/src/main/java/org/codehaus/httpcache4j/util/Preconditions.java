package org.codehaus.httpcache4j.util;

/**
 * Created by maedhros on 29/06/15.
 */
public class Preconditions {
    public static void checkArgument(boolean expr, String msg) {
        if (!expr) throw new IllegalArgumentException(msg);
    }
}
