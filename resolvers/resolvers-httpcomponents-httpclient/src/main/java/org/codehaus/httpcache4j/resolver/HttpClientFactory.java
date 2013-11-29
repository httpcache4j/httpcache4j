package org.codehaus.httpcache4j.resolver;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.protocol.*;
import org.apache.http.client.protocol.RequestExpectContinue;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.*;
import org.codehaus.httpcache4j.HTTPHost;
import org.codehaus.httpcache4j.auth.ProxyAuthenticator;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class HttpClientFactory {

    public HttpClientBuilder createHttpClientBuilder() {
        return HttpClientBuilder.create();
    }

    public CloseableHttpClient configure(HttpClientBuilder builder,
                                         ResolverConfiguration configuration,
                                         IdleConnectionMonitor.Configuration idleConfig) {

        builder.disableAuthCaching();
        ConnectionConfiguration config = configuration.getConnectionConfiguration();

        HttpClientConnectionManager connManager = configurationManager(config);

        builder.setConnectionManager(connManager);
        SocketConfig.Builder socketConfig = SocketConfig.copy(SocketConfig.DEFAULT);
        if (config.getSocketTimeout().isPresent()) {
            socketConfig.setSoTimeout(config.getSocketTimeout().get());
        }
        builder.setDefaultSocketConfig(socketConfig.build());

        ProxyAuthenticator proxyAuthenticator = configuration.getProxyAuthenticator();
        HTTPHost proxyHost = proxyAuthenticator.getConfiguration().getHost();

        RequestConfig requestC = requestConfig(proxyHost, config);
        builder.setDefaultRequestConfig(requestC);
        builder.setHttpProcessor(httpProcessor(configuration));

        if (idleConfig != null) {
            IdleConnectionMonitor monitor = new IdleConnectionMonitor(connManager, idleConfig);
            monitor.start();

            return new MonitoredCloseableHttpClient(builder.build(), monitor);
        }

        return builder.build();
    }

    protected HttpProcessor httpProcessor(ResolverConfiguration configuration) {
        HttpProcessorBuilder b = HttpProcessorBuilder.create();
        b.addAll(
                new RequestDefaultHeaders(Collections.<Header>emptyList()),
                new RequestContent(true),
                new RequestTargetHost(),
                new RequestClientConnControl(),
                new RequestUserAgent(configuration.getUserAgent()),
                new RequestExpectContinue());
        b.add(new RequestAddCookies());
        b.add(new RequestAcceptEncoding());
        b.add(new ResponseProcessCookies());
        b.add(new ResponseContentEncoding());
        return b.build();
    }

    protected HttpClientConnectionManager configurationManager(ConnectionConfiguration config) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        if (config.getDefaultConnectionsPerHost().isPresent()) {
            connectionManager.setDefaultMaxPerRoute(config.getDefaultConnectionsPerHost().get());
        }
        if (config.getMaxConnections().isPresent()) {
            connectionManager.setMaxTotal(config.getMaxConnections().get());
        }
        for (Map.Entry<HTTPHost, Integer> entry : config.getConnectionsPerHost().entrySet()) {
            HTTPHost host = entry.getKey();
            connectionManager.setMaxPerRoute(new HttpRoute(new HttpHost(host.getHost(), host.getPort(), host.getScheme())), entry.getValue());
        }
        return connectionManager;
    }

    protected RequestConfig requestConfig(HTTPHost proxyHost, ConnectionConfiguration config) {
        RequestConfig.Builder requestConfig = RequestConfig.copy(RequestConfig.DEFAULT);
        if (config.getTimeout().isPresent()) {
            requestConfig.setConnectTimeout(config.getTimeout().get());
        }
        requestConfig.setAuthenticationEnabled(false);
        requestConfig.setStaleConnectionCheckEnabled(false);
        if (proxyHost != null) {
            requestConfig.setProxy(new HttpHost(proxyHost.getHost(), proxyHost.getPort(), proxyHost.getScheme()));
        }
        return requestConfig.build();
    }

    public static class MonitoredCloseableHttpClient extends CloseableHttpClient {
        private final CloseableHttpClient delegate;
        private final IdleConnectionMonitor monitor;

        public MonitoredCloseableHttpClient(CloseableHttpClient delegate, IdleConnectionMonitor monitor) {
            this.delegate = delegate;
            this.monitor = monitor;
        }

        @Override
        protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
            return delegate.execute(target, request, context);
        }

        @Override
        public void close() throws IOException {
            monitor.shutdown();
            delegate.close();
        }

        @Override
        public HttpParams getParams() {
            return delegate.getParams();
        }

        @Override
        public ClientConnectionManager getConnectionManager() {
            return delegate.getConnectionManager();
        }
    }

}
