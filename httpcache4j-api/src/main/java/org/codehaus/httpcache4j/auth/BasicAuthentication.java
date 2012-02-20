package org.codehaus.httpcache4j.auth;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.httpcache4j.UsernamePasswordChallenge;

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
public class BasicAuthentication {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static String getHeaderValue(UsernamePasswordChallenge challenge) {
        String basicString = challenge.getIdentifier() + ":" + new String(challenge.getPassword());
        return "Basic " + new String(Base64.encodeBase64(basicString.getBytes(UTF_8)));
    }
    
}
