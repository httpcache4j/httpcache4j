package org.codehaus.httpcache4j.auth.mac;

import org.codehaus.httpcache4j.Challenge;

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
public class MacChallenge implements Challenge {

    private final String id;
    private final String key;
    private final Algorithm algorithm;
    private final ExtensionCalculator extensionCalculator;

    public MacChallenge(String id, String key, Algorithm algorithm, ExtensionCalculator extensionCalculator) {
        this.id = id;
        this.key = key;
        this.algorithm = algorithm;
        this.extensionCalculator = extensionCalculator == null ? ExtensionCalculator.NULL : extensionCalculator;
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

    public ExtensionCalculator getExtensionCalculator() {
        return extensionCalculator;
    }
}
