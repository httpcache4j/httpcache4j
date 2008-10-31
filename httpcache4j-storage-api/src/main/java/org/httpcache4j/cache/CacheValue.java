package org.httpcache4j.cache;


import org.apache.commons.lang.Validate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class CacheValue implements Iterable<Map.Entry<Vary, CacheItem>> {
    private Map<Vary, CacheItem> variations = new HashMap<Vary, CacheItem>();

    public CacheValue(final Map<Vary, CacheItem> pVariations) {
        Validate.notNull(pVariations, "Variations may not be null");
        variations.putAll(pVariations);
    }

    public Map<Vary, CacheItem> getVariations() {
        return variations;
    }

    public Iterator<Map.Entry<Vary, CacheItem>> iterator() {
        return getVariations().entrySet().iterator();
    }

}