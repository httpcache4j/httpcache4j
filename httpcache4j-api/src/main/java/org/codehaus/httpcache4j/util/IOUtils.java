package org.codehaus.httpcache4j.util;

import java.io.*;
import java.util.function.Function;

public final class IOUtils {
    private static final int BUF_SIZE = 0x1000; // 4K
    private IOUtils() {
    }

    public static long copy(InputStream from, OutputStream to) throws IOException {
        if (from == null) {
            throw new IllegalArgumentException("null from not allowed");
        }
        if (to == null) {
            throw new IllegalArgumentException("null to not allowed");
        }
        byte[] buf = new byte[BUF_SIZE];
        long total = 0;
        while (true) {
            int r = from.read(buf);
            if (r == -1) {
                break;
            }
            to.write(buf, 0, r);
            total += r;
        }
        return total;
    }

    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copy(is, bos);
        return bos.toByteArray();
    }
}
