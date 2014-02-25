package org.codehaus.httpcache4j.util;

import java.io.*;
import java.nio.charset.Charset;

public final class IOUtils {
    private static final int BUF_SIZE = 0x1000; // 4K
    private IOUtils() {
    }

    public static String toString(InputStream from) throws IOException {
        return toString(from, Charsets.UTF_8);
    }

    public static String toString(InputStream from, Charset charset) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(from, charset));
            String line;
            while((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        finally {
            closeQuietly(reader);
        }
        return sb.toString();
    }

    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            copy(is, os);
        } finally {
            closeQuietly(is);
        }
        return os.toByteArray();
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

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ignore) {
        }
    }
}
