package org.codehaus.httpcache4j.util;

import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;

public final class URISchemeDefaults {
    private final Map<String, Integer> map = Maps.newHashMap();

    public URISchemeDefaults(Map<String, Integer> defaults) {
        map.putAll(defaults);
    }

    public URISchemeDefaults() {
        map.put("http", 80);
        map.put("https", 443);
        map.put("ftp", 21);
        map.put("ssh", 22);
    }

    public Optional<Integer> getPort(String scheme) {
        return Optional.fromNullable(map.get(scheme));
    }
}
