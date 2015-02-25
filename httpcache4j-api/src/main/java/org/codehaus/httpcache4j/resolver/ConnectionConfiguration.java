package org.codehaus.httpcache4j.resolver;

import net.hamnaberg.funclite.Optional;
import org.codehaus.httpcache4j.HTTPHost;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class ConnectionConfiguration {
    private final Optional<Integer> timeout;
    private final Optional<Integer> socketTimeout;
    private final Optional<Integer> connectionRequestTimeout;
    private final Optional<Integer> defaultConnectionsPerHost;
    private final Optional<Integer> maxConnections;
    private final Map<HTTPHost, Integer> connectionsPerHost = new HashMap<>();

    public ConnectionConfiguration(Optional<Integer> timeout, Optional<Integer> socketTimeout, Optional<Integer> connectionRequestTimeout, Optional<Integer> defaultConnectionsPerHost, Optional<Integer> maxConnections, Map<HTTPHost, Integer> connectionsPerHost) {
        this.timeout = timeout;
        this.socketTimeout = socketTimeout;
        this.connectionRequestTimeout = connectionRequestTimeout;
        this.defaultConnectionsPerHost = defaultConnectionsPerHost;
        this.maxConnections = maxConnections;
        this.connectionsPerHost.putAll(connectionsPerHost);
    }

    public ConnectionConfiguration() {
        this(Optional.<Integer>none(), Optional.<Integer>none(), Optional.<Integer>none(), Optional.<Integer>none(), Optional.<Integer>none(), Collections.<HTTPHost, Integer>emptyMap());
    }

    public Optional<Integer> getTimeout() {
        return timeout;
    }

    public Optional<Integer> getSocketTimeout() {
        return socketTimeout;
    }

    public Optional<Integer> getConnectionRequestTimeout() { return connectionRequestTimeout; }

    public Optional<Integer> getDefaultConnectionsPerHost() {
        return defaultConnectionsPerHost;
    }

    public Optional<Integer> getMaxConnections() {
        return maxConnections;
    }

    public Map<HTTPHost, Integer> getConnectionsPerHost() {
        return connectionsPerHost;
    }

    /**
     * Mutable builder.
     */
    public static class Builder {
        private Optional<Integer> timeout = Optional.none();
        private Optional<Integer> socketTimeout = Optional.none();
        private Optional<Integer> connectionRequestTimeout = Optional.none();
        private Optional<Integer> defaultConnectionPerHost = Optional.none();
        private Optional<Integer> maxConnections = Optional.none();
        private final Map<HTTPHost, Integer> connectionsPerHost = new HashMap<HTTPHost, Integer>();

        public Builder setTimeout(int timeout) {
            this.timeout = Optional.fromNullable(timeout);
            return this;
        }

        public Builder setSocketTimeout(int socketTimeout) {
            this.socketTimeout = Optional.fromNullable(socketTimeout);
            return this;
        }

        public Builder setConnectionRequestTimeout(int connectionRequestTimeout) {
            this.connectionRequestTimeout = Optional.fromNullable(connectionRequestTimeout);
            return this;
        }

        public Builder setDefaultConnectionPerHost(int defaultConnectionPerHost) {
            this.defaultConnectionPerHost = Optional.fromNullable(defaultConnectionPerHost);
            return this;
        }

        public Builder setMaxConnections(int maxConnections) {
            this.maxConnections = Optional.fromNullable(maxConnections);
            return this;
        }

        public Builder add(HTTPHost host, int connections) {
            this.connectionsPerHost.put(host, connections);
            return this;
        }

        public ConnectionConfiguration build() {
            return new ConnectionConfiguration(timeout, socketTimeout, connectionRequestTimeout, defaultConnectionPerHost, maxConnections, connectionsPerHost);
        }
    }
}
