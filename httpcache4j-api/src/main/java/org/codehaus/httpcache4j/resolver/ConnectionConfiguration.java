package org.codehaus.httpcache4j.resolver;

import com.google.common.base.Optional;
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
    private final Optional<Integer> defaultConnectionsPerHost;
    private final Optional<Integer> maxConnections;
    private final Map<HTTPHost, Integer> connectionsPerHost = new HashMap<HTTPHost, Integer>();

    public ConnectionConfiguration(Optional<Integer> timeout, Optional<Integer> socketTimeout, Optional<Integer> defaultConnectionsPerHost, Optional<Integer> maxConnections, Map<HTTPHost, Integer> connectionsPerHost) {
        this.timeout = timeout;
        this.socketTimeout = socketTimeout;
        this.defaultConnectionsPerHost = defaultConnectionsPerHost;
        this.maxConnections = maxConnections;
        this.connectionsPerHost.putAll(connectionsPerHost);
    }

    public ConnectionConfiguration() {
        this(Optional.<Integer>absent(), Optional.<Integer>absent(), Optional.<Integer>absent(), Optional.<Integer>absent(), Collections.<HTTPHost, Integer>emptyMap());
    }

    public Optional<Integer> getTimeout() {
        return timeout;
    }

    public Optional<Integer> getSocketTimeout() {
        return socketTimeout;
    }

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
        private Optional<Integer> timeout = Optional.absent();
        private Optional<Integer> socketTimeout = Optional.absent();
        private Optional<Integer> defaultConnectionPerHost = Optional.absent();
        private Optional<Integer> maxConnections = Optional.absent();
        private final Map<HTTPHost, Integer> connectionsPerHost = new HashMap<HTTPHost, Integer>();

        public Builder setTimeout(int timeout) {
            this.timeout = Optional.of(timeout);
            return this;
        }

        public Builder setSocketTimeout(int socketTimeout) {
            this.socketTimeout = Optional.of(socketTimeout);
            return this;
        }

        public Builder setDefaultConnectionPerHost(int defaultConnectionPerHost) {
            this.defaultConnectionPerHost = Optional.of(defaultConnectionPerHost);
            return this;
        }

        public Builder setMaxConnections(int maxConnections) {
            this.maxConnections = Optional.of(maxConnections);
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
