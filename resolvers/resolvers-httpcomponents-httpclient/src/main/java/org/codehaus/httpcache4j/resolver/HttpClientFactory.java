package org.codehaus.httpcache4j.resolver;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.*;
import org.apache.http.client.protocol.RequestExpectContinue;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.*;
import org.codehaus.httpcache4j.HTTPHost;
import org.codehaus.httpcache4j.auth.ProxyAuthenticator;

import java.util.Collections;
import java.util.Map;

public class HttpClientFactory {

    public CloseableHttpClient configure(HttpClientBuilder builder, ResolverConfiguration configuration) {

        builder.disableAuthCaching();
        ConnectionConfiguration config = configuration.getConnectionConfiguration();

        builder.setConnectionManager(configurationManager(config));
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
        if (config.getConnectionRequestTimeout().isPresent()) {
            requestConfig.setConnectionRequestTimeout(config.getConnectionRequestTimeout().get());
        }
        if (config.getSocketTimeout().isPresent()) {
            requestConfig.setSocketTimeout(config.getSocketTimeout().get());
        }
        requestConfig.setAuthenticationEnabled(false);
        requestConfig.setStaleConnectionCheckEnabled(false);
        if (proxyHost != null) {
            requestConfig.setProxy(new HttpHost(proxyHost.getHost(), proxyHost.getPort(), proxyHost.getScheme()));
        }
        return requestConfig.build();
    }


}
