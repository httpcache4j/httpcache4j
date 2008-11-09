package org.codehaus.httpcache4j;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @author last modified by $Author: $
 * @version $Id: $
 */
public class Challenge {
    private String identifier;
    private char[] password;
    private ChallengeMethod method;

    public Challenge(String identifier, char[] password, ChallengeMethod method) {
        this.identifier = identifier;
        this.password = password != null ? password.clone() : null;
        this.method = method;
    }

    public String getIdentifier() {
        return identifier;
    }

    public char[] getPassword() {
        return password;
    }

    public ChallengeMethod getMethod() {
        return method;
    }
}