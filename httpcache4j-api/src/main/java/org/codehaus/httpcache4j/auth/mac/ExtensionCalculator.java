package org.codehaus.httpcache4j.auth.mac;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.util.Pair;

/**
 * @author Erlend Hamnaberg<erlend@hamnaberg.net>
 */
public interface ExtensionCalculator {
    Pair<HTTPRequest, String> calculate(HTTPRequest request);

    ExtensionCalculator NULL = new ExtensionCalculator() {
        public Pair<HTTPRequest, String> calculate(HTTPRequest request) {
            return Pair.of(request, null);
        }
    };
}
