package org.codehaus.httpcache4j.util;

public class StringUtils {
    private StringUtils() {}

    public static boolean isNullOrEmpty(String input) {
        return input == null || input.trim().isEmpty();
    }
}
