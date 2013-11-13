package org.codehaus.httpcache4j.util;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Digester {
    public static String sha256(String bytes, Charset charset) {
        return sha256(bytes.getBytes(charset));
    }

    public static String sha256(byte[] bytes) {
        return doDigest(bytes, "SHA-1");
    }

    public static String sha1(String bytes, Charset charset) {
        return sha1(bytes.getBytes(charset));
    }

    public static String sha1(byte[] bytes) {
        return doDigest(bytes, "SHA-1");
    }

    public static String md5(String bytes, Charset charset) {
        return md5(bytes.getBytes(charset));
    }

    public static String md5(byte[] bytes) {
        return doDigest(bytes, "MD5");
    }

    private static String doDigest(byte[] bytes, String algorithm) {
        MessageDigest digest = getDigest(algorithm);
        byte[] digested = digest.digest(bytes);
        return Hex.encode(digested).trim();
    }

    public static MessageDigest getDigest(String algorithm)  {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No " + algorithm + "! Is your JRE broken?");
        }
    }
}
