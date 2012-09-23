package org.codehaus.httpcache4j.util;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class NumberUtils {
    public static int toInt(String input, int defaultValue) {
        if (input == null) return defaultValue;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static long toLong(String value, long defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }

    }

    public static double toDouble(String input, double defaultValue) {
        if (input == null) return defaultValue;
        try {
            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
