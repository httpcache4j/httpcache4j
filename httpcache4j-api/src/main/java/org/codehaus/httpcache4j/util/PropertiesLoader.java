package org.codehaus.httpcache4j.util;

import com.google.common.io.Closeables;

import java.io.*;
import java.util.Properties;

public final class PropertiesLoader {
    private PropertiesLoader() {
    }

    public static Properties get(Reader reader) {
        Properties properties = new Properties();
        try {
            properties.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            Closeables.closeQuietly(reader);
        }
        return properties;
    }

    public static Properties get(InputStream stream) {
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            Closeables.closeQuietly(stream);
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
