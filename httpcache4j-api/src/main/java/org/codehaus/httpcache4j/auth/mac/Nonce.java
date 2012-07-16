package org.codehaus.httpcache4j.auth.mac;

import java.security.SecureRandom;

import org.codehaus.httpcache4j.util.Hex;
import org.codehaus.httpcache4j.util.SecureRandomFactory;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class Nonce {
    private static SecureRandom rnd = SecureRandomFactory.getRandom();
    private static final int OFFSET = 8;

    private final String nonce;

    Nonce(String nonce) { //For testing
        this.nonce = nonce;
    }

    public static Nonce generate() {
        byte[] bytes = new byte[1024];
        rnd.nextBytes(bytes);
        String s = Hex.encode(bytes);
        int length = s.length();
        int index = rnd.nextInt(length);
        while((index + OFFSET) >= length) {
            index = rnd.nextInt(length);
        }
        String substring = s.substring(index, index + OFFSET);
        return new Nonce(substring);
    }

    public String format() {
        return nonce;
    }

    @Override
    public String toString() {
        return "Nonce{" +
                "'" + nonce + "'" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Nonce nonce1 = (Nonce) o;

        if (nonce != null ? !nonce.equals(nonce1.nonce) : nonce1.nonce != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return nonce != null ? nonce.hashCode() : 0;
    }
}
