package org.codehaus.httpcache4j.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private AtomicInteger ThreadCounter = new AtomicInteger();
    private ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private final String name;
    private final boolean daemon;

    public NamedThreadFactory(String name, boolean daemon) {
        this.name = name;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = threadFactory.newThread(r);
        thread.setName(name + "-" + ThreadCounter.incrementAndGet());
        thread.setDaemon(daemon);
        return thread;
    }
}
