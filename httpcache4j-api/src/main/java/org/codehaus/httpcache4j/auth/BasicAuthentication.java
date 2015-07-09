package org.codehaus.httpcache4j.auth;

import java.nio.charset.Charset;

import org.codehaus.httpcache4j.UsernamePasswordChallenge;
import java.util.Base64;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public class BasicAuthentication {
    private static final Charset UTF_8 = Charset.forName("UTF-8");

    public static String getHeaderValue(UsernamePasswordChallenge challenge) {
        String basicString = challenge.getIdentifier() + ":" + new String(challenge.getPassword());
        return "Basic " + Base64.getEncoder().encodeToString(basicString.getBytes(UTF_8));
    }
    
}
