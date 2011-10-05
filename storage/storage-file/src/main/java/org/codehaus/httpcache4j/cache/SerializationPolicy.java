package org.codehaus.httpcache4j.cache;

public interface SerializationPolicy {
    boolean shouldWePersist(long numberOfModifications, long lastSerializationTime);
}
