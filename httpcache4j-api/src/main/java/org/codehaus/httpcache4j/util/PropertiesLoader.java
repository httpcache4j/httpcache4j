package org.codehaus.httpcache4j.util;

import java.io.*;
import java.util.Properties;

public final class PropertiesLoader {
    private PropertiesLoader() {
    }

    public static Properties get(Reader reader) {
        Properties properties = new Properties();
        try(Reader r = reader) {
            properties.load(r);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    public static Properties get(InputStream stream) {
        Properties properties = new Properties();
        try(InputStream is = stream) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return properties;
    }

    public static Properties get(File file) {
        try {
            return get(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
