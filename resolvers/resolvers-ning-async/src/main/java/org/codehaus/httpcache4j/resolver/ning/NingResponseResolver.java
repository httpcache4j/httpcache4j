package org.codehaus.httpcache4j.resolver.ning;

import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.*;
import org.asynchttpclient.request.body.generator.InputStreamBodyGenerator;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.auth.Authenticator;
import org.codehaus.httpcache4j.auth.ProxyAuthenticator;
import org.codehaus.httpcache4j.resolver.AbstractResponseResolver;
import org.codehaus.httpcache4j.resolver.ConnectionConfiguration;
import org.codehaus.httpcache4j.resolver.ResolverConfiguration;
import org.codehaus.httpcache4j.resolver.ResponseCreator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.codehaus.httpcache4j.HTTPMethod.*;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class NingResponseResolver extends AbstractResponseResolver {
    private final AsyncHttpClient client;

    protected NingResponseResolver(ResolverConfiguration configuration, DefaultAsyncHttpClientConfig.Builder builder, Consumer<DefaultAsyncHttpClientConfig.Builder> configF) {
        super(configuration);
        builder.setUserAgent(configuration.getUserAgent());
        builder.setFollowRedirect(false);
        ConnectionConfiguration connectionConfiguration = configureConnections(configuration, builder);
        configF.accept(builder);
        if (!connectionConfiguration.getConnectionsPerHost().isEmpty()) {
            throw new UnsupportedOperationException("This Resolver does not support connections per host");
        }
        client = new DefaultAsyncHttpClient(builder.build());
    }

    private ConnectionConfiguration configureConnections(ResolverConfiguration configuration, DefaultAsyncHttpClientConfig.Builder config) {
        ConnectionConfiguration connectionConfiguration = configuration.getConnectionConfiguration();
        if (connectionConfiguration.getMaxConnections().isPresent()) {
            config.setMaxConnections(connectionConfiguration.getMaxConnections().get());
        }
        if (connectionConfiguration.getDefaultConnectionsPerHost().isPresent()) {
            config.setMaxConnectionsPerHost(connectionConfiguration.getDefaultConnectionsPerHost().get());
        }
        if (connectionConfiguration.getTimeout().isPresent()) {
            config.setReadTimeout(connectionConfiguration.getTimeout().get());
        }
        if (connectionConfiguration.getSocketTimeout().isPresent()) {
            config.setConnectTimeout(connectionConfiguration.getSocketTimeout().get());
        }
        return connectionConfiguration;
    }

    public NingResponseResolver(ResolverConfiguration configuration) {
        this(configuration, new DefaultAsyncHttpClientConfig.Builder(), config -> {});
    }

    public NingResponseResolver(ResolverConfiguration configuration, Consumer<DefaultAsyncHttpClientConfig.Builder> configF) {
        this(configuration, new DefaultAsyncHttpClientConfig.Builder(), configF);
    }

    public NingResponseResolver(ProxyAuthenticator proxyAuthenticator, Authenticator authenticator) {
        this(new ResolverConfiguration(proxyAuthenticator, authenticator, new ConnectionConfiguration()));
    }

    public static NingResponseResolver newInstance(ResolverConfiguration configuration) {
        return new NingResponseResolver(configuration);
    }

    public static NingResponseResolver newInstance(ConnectionConfiguration configuration) {
        return newInstance(new ResolverConfiguration().withConnectionConfiguration(configuration));
    }

    public static NingResponseResolver newInstance() {
        return newInstance(new ConnectionConfiguration());
    }

    @Override
    protected CompletableFuture<HTTPResponse> resolveImpl(HTTPRequest request) {
        CompletableFuture<Response> responseFuture = execute(request);
        return translate(responseFuture);
    }

    public void shutdown() {
        try {
            client.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<HTTPResponse> translate(CompletableFuture<Response> responseFuture) {
        return responseFuture.thenApply(response -> {
            StatusLine line = new StatusLine(Status.valueOf(response.getStatusCode()), response.getStatusText());
            HttpHeaders headers = response.getHeaders();
            Optional<InputStream> stream = Optional.ofNullable(response.getResponseBodyAsStream());
            List<Header> headerList = StreamSupport.stream(headers.spliterator(), false).map(e -> new Header(e.getKey(), e.getValue())).collect(Collectors.toList());
            return ResponseCreator.createResponse(line, new Headers(headerList), stream);
        });
    }

    private CompletableFuture<Response> execute(final HTTPRequest request) {
        BoundRequestBuilder builder = builder(request.getNormalizedURI(), request.getMethod());
        if (request.getMethod().canHavePayload()) {
            request.getPayload().ifPresent(p -> {
                if (getConfiguration().isUseChunked()) {
                    builder.setBody(new InputStreamBodyGenerator(p.getInputStream()));
                } else {
                    builder.setBody(p.getInputStream());
                }
            });
        }
        for (Header header : request.getAllHeaders()) {
            builder.addHeader(header.getName(), header.getValue());
        }
        return builder.execute().toCompletableFuture();
    }

    private BoundRequestBuilder builder(URI uri, HTTPMethod method) {
        if (DELETE.equals(method)) {
            return client.prepareDelete(uri.toString());
        } else if (GET.equals(method)) {
            return client.prepareGet(uri.toString());
        } else if (HEAD.equals(method)) {
            return client.prepareHead(uri.toString());
        } else if (OPTIONS.equals(method)) {
            return client.prepareOptions(uri.toString());
        } else if (POST.equals(method)) {
            return client.preparePost(uri.toString());
        } else if (PUT.equals(method)) {
            return client.preparePut(uri.toString());
        }
        throw new IllegalArgumentException("Unable to create request for method " + method);
    }
}
