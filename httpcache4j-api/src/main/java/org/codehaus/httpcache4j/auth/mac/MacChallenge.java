package org.codehaus.httpcache4j.auth.mac;

import org.codehaus.httpcache4j.Challenge;

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
public class MacChallenge implements Challenge {

    private final String id;
    private final String key;
    private final Algorithm algorithm;
    private final String ext;

    public MacChallenge(String id, String key, Algorithm algorithm, String ext) {
        this.id = id;
        this.key = key;
        this.algorithm = algorithm;
        this.ext = ext;
    }

    public String getIdentifier() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public String getExt() {
        return ext;
    }
}
