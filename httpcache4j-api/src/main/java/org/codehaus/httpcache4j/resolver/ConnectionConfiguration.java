package org.codehaus.httpcache4j.resolver;

import org.codehaus.httpcache4j.HTTPHost;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class ConnectionConfiguration {
    private final int timeout;
    private final int socketTimeout;
    private final int defaultConnectionsPerHost;
    private final int maxConnections;
    private final Map<HTTPHost, Integer> connectionsPerHost = new HashMap<HTTPHost, Integer>();

    public ConnectionConfiguration(int timeout, int socketTimeout, int defaultConnectionsPerHost, int maxConnections, Map<HTTPHost, Integer> connectionsPerHost) {
        this.timeout = timeout;
        this.socketTimeout = socketTimeout;
        this.defaultConnectionsPerHost = defaultConnectionsPerHost;
        this.maxConnections = maxConnections;
        this.connectionsPerHost.putAll(connectionsPerHost);
    }

    public ConnectionConfiguration() {
        this(0, 0, 5, 20, Collections.<HTTPHost, Integer>emptyMap());
    }

    public int getTimeout() {
        return timeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public int getDefaultConnectionsPerHost() {
        return defaultConnectionsPerHost;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public Map<HTTPHost, Integer> getConnectionsPerHost() {
        return connectionsPerHost;
    }

    /**
     * Mutable builder.
     */
    public static class Builder {
        private int timeout = 0;
        private int socketTimeout = 0;
        private int defaultConnectionPerHost = 5;
        private int maxConnections = 20;
        private final Map<HTTPHost, Integer> connectionsPerHost = new HashMap<HTTPHost, Integer>();

        public int getTimeout() {
            return timeout;
        }

        public Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public int getSocketTimeout() {
            return socketTimeout;
        }

        public Builder setSocketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public int getDefaultConnectionPerHost() {
            return defaultConnectionPerHost;
        }

        public Builder setDefaultConnectionPerHost(int defaultConnectionPerHost) {
            this.defaultConnectionPerHost = defaultConnectionPerHost;
            return this;
        }

        public int getMaxConnections() {
            return maxConnections;
        }

        public Builder setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder add(HTTPHost host, int connections) {
            this.connectionsPerHost.put(host, connections);
            return this;
        }

        public ConnectionConfiguration build() {
            return new ConnectionConfiguration(timeout, socketTimeout, defaultConnectionPerHost, maxConnections, connectionsPerHost);
        }
    }
}
