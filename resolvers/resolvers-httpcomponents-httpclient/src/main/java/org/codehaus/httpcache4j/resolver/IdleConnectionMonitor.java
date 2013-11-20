package org.codehaus.httpcache4j.resolver;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.http.conn.ClientConnectionManager;

public class IdleConnectionMonitor {
    private final ClientConnectionManager manager;
    private final Configuration configuration;

    public IdleConnectionMonitor(ClientConnectionManager manager, Configuration configuration) {
        this.manager = manager;
        this.configuration = configuration;
    }

    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setName(IdleConnectionMonitor.class.getSimpleName());
            thread.setDaemon(true);
            return thread;
        }
    });

    public void start() {
        service.scheduleAtFixedRate(new Monitor(), configuration.getInitialDelay(), configuration.getSchedule(), TimeUnit.SECONDS);
    }

    public void shutdown() {
        service.shutdown();
    }

    private class Monitor implements Runnable {

        @Override
        public void run() {
            // Close expired connections
            manager.closeExpiredConnections();
            if (configuration.shouldCloseIdleConnections()) {
                // Optionally, close connections
                // that have been idle longer than 30 sec
                manager.closeIdleConnections(configuration.getIdleTime(), TimeUnit.SECONDS);
            }
        }
    }

    public static class Configuration {
        private final int initialDelay;
        private final int schedule;
        private final int idleTime;

        public Configuration() {
            this(10, 5, -1);
        }

        public Configuration(int initialDelay, int schedule) {
            this(initialDelay, schedule, -1);
        }

        public Configuration(int initialDelay, int schedule, int idleTime) {
            this.initialDelay = initialDelay;
            this.schedule = schedule;
            this.idleTime = idleTime;
        }

        public int getInitialDelay() {
            return initialDelay;
        }

        public int getSchedule() {
            return schedule;
        }

        public boolean shouldCloseIdleConnections() {
            return idleTime > 0;
        }

        public int getIdleTime() {
            return idleTime;
        }
    }
}
