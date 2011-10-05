package org.codehaus.httpcache4j.cache;

public class DefaultSerializationPolicy implements SerializationPolicy {
    private static final long PERSISTENT_TIMEOUT = 60000L;
    private static final int PERSISTENT_TRESHOLD = 100;

    private final long timeout;
    private final long threshold;

    public DefaultSerializationPolicy() {
        this(PERSISTENT_TIMEOUT, PERSISTENT_TRESHOLD);
    }

    public DefaultSerializationPolicy(long timeout, long threshold) {
        this.timeout = timeout;
        this.threshold = threshold;
    }

    public boolean shouldWePersist(long numberOfModifications, long lastSerializationTime) {
        return isWithinTreshold(numberOfModifications) && hasTimeoutOccured(lastSerializationTime);
    }

    private boolean hasTimeoutOccured(long lastSerializationTime) {
        return System.currentTimeMillis() > lastSerializationTime + timeout;
    }

    private boolean isWithinTreshold(long numberOfModifications) {
        return numberOfModifications % threshold == 0;
    }
}
