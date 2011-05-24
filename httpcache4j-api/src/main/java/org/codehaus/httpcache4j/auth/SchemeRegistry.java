package org.codehaus.httpcache4j.auth;

import org.codehaus.httpcache4j.HTTPHost;

public interface SchemeRegistry {
    void register(HTTPHost host, AuthScheme scheme);

    boolean matches(HTTPHost host);

    void clear();

    AuthScheme get(HTTPHost host);

    void remove(HTTPHost host);
}
