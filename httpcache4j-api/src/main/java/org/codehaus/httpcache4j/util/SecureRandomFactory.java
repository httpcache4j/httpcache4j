package org.codehaus.httpcache4j.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class SecureRandomFactory {
    public static SecureRandom getRandom() {
        return getRandom(null);
    }

    public static SecureRandom getRandom(byte[] seed) {
        try {
            SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
            if (seed != null) {
                rnd.setSeed(seed);
            }
            return rnd;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
