package org.codehaus.httpcache4j.resolver;

import java.io.IOException;

import org.codehaus.httpcache4j.HTTPRequest;
import org.codehaus.httpcache4j.HTTPResponse;

public class MonitoredHTTPClientResponseResolver implements ResponseResolver {

    private final HTTPClientResponseResolver resolver;
    private final IdleConnectionMonitor monitor;


    public MonitoredHTTPClientResponseResolver(HTTPClientResponseResolver resolver, IdleConnectionMonitor monitor) {
        this.resolver = resolver;
        this.monitor = monitor;
    }

    public MonitoredHTTPClientResponseResolver(HTTPClientResponseResolver resolver, IdleConnectionMonitor.Configuration configuration) {
        this(resolver, new IdleConnectionMonitor(resolver.getHttpClient().getConnectionManager(), configuration));
    }

    public MonitoredHTTPClientResponseResolver(HTTPClientResponseResolver resolver) {
        this(resolver, new IdleConnectionMonitor.Configuration());
    }

    @Override
    public final HTTPResponse resolve(HTTPRequest request) throws IOException {
        return resolver.resolve(request);
    }

    @Override
    public void shutdown() {
        resolver.shutdown();
        monitor.shutdown();
    }
}
