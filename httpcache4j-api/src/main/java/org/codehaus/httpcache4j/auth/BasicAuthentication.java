package org.codehaus.httpcache4j.auth;

import java.nio.charset.Charset;

import org.codehaus.httpcache4j.UsernamePasswordChallenge;
import org.codehaus.httpcache4j.util.Base64;

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
public class BasicAuthentication {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static String getHeaderValue(UsernamePasswordChallenge challenge) {
        String basicString = challenge.getIdentifier() + ":" + new String(challenge.getPassword());
        return "Basic " + Base64.encodeBytes(basicString.getBytes(UTF_8));
    }
    
}
