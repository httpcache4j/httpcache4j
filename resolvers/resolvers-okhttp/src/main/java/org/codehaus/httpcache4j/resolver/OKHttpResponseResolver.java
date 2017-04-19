package org.codehaus.httpcache4j.resolver;

import okhttp3.*;
import okio.BufferedSink;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.payload.Payload;
import org.codehaus.httpcache4j.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class OKHttpResponseResolver extends AbstractResponseResolver {
    private final OkHttpClient client;

    public OKHttpResponseResolver(ResolverConfiguration config) {
        this(config, builder -> {
        });
    }

    public OKHttpResponseResolver(ResolverConfiguration configuration, Consumer<OkHttpClient.Builder> configF) {
        super(configuration);
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.authenticator(new NullAuthenticator());
        builder.followRedirects(false);
        builder.followSslRedirects(false);
        builder.cache(null);
        ConnectionConfiguration connConfig = configuration.getConnectionConfiguration();
        builder.connectionPool(new ConnectionPool(
                connConfig.getMaxConnections().orElse(new ConnectionPool().connectionCount()),
                (5 * 60 * 1000), // 5 minutes,
                TimeUnit.MILLISECONDS
        ));
        connConfig.getConnectionRequestTimeout().ifPresent(i -> builder.readTimeout(i, TimeUnit.MILLISECONDS));
        connConfig.getSocketTimeout().ifPresent(i -> builder.connectTimeout(i, TimeUnit.MILLISECONDS));
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(connConfig.getMaxConnections().orElse(dispatcher.getMaxRequests()));
        dispatcher.setMaxRequestsPerHost(connConfig.getDefaultConnectionsPerHost().orElse(dispatcher.getMaxRequestsPerHost()));
        builder.dispatcher(dispatcher);

        if (connConfig.getTimeout().isPresent()) {
            builder.connectTimeout(connConfig.getTimeout().get(), TimeUnit.MILLISECONDS);
        }
        HTTPHost proxyHost = configuration.getProxyAuthenticator().getConfiguration().getHost();
        if (proxyHost != null) {
            builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost.getHost(), proxyHost.getPort())));
        }

        configF.accept(builder);

        this.client = builder.build();
    }

    @Override
    protected CompletableFuture<HTTPResponse> resolveImpl(HTTPRequest request) {
        CompletableFuture<HTTPResponse> promise = new CompletableFuture<>();
        final Request req = transformRequest(request);
        Call call = client.newCall(req);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e != null) {
                    promise.completeExceptionally(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                promise.complete(transformResponse(response));
            }
        });
        return promise;
    }

    private Request transformRequest(HTTPRequest request) {
        Request.Builder builder = new Request.Builder();
        builder.addHeader(HeaderConstants.USER_AGENT, getConfiguration().getUserAgent());
        Headers requestHeaders = request.getHeaders();
        for (Header header : requestHeaders) {
            builder.addHeader(header.getName(), header.getValue());
        }
        Optional<RequestBody> payload = request.getPayload().map(PayloadRequestBody::new);
        builder.method(request.getMethod().getMethod(), payload.orElse(null));
        builder.url(HttpUrl.get(request.getNormalizedURI()));
        return builder.build();
    }

    private HTTPResponse transformResponse(Response response) throws IOException {
        Status status = Status.valueOf(response.code());
        StatusLine line = new StatusLine(status, response.message());

        return new HTTPResponse(
                Optional.ofNullable(response.body()).map(PayloadResponseBody::new),
                line,
                new Headers(response.headers().toMultimap())
        );
    }

    @Override
    public void shutdown() {
        client.dispatcher().executorService().shutdown();
    }

    public OkHttpClient getClient() {
        return client;
    }

    private static class PayloadRequestBody extends RequestBody {
        private final Payload payload;

        public PayloadRequestBody(Payload payload) {
            this.payload = payload;
        }

        @Override
        public MediaType contentType() {
            return MediaType.parse(payload.getMimeType().toString());
        }

        @Override
        public void writeTo(BufferedSink bufferedSink) throws IOException {
            IOUtils.copy(payload.getInputStream(), bufferedSink.outputStream());
        }
    }

    private static class PayloadResponseBody implements Payload {
        private final ResponseBody delegate;
        private boolean available;

        public PayloadResponseBody(ResponseBody payload) {
            this.delegate = payload;
        }

        @Override
        public MIMEType getMimeType() {
            return MIMEType.valueOf(delegate.contentType().toString());
        }

        @Override
        public InputStream getInputStream() {
            available = false;
            return delegate.byteStream();
        }

        @Override
        public long length() {
            return delegate.contentLength();
        }

        @Override
        public boolean isAvailable() {
            return available;
        }
    }

    private static class NullAuthenticator implements Authenticator {

        @Override
        public Request authenticate(Route route, Response response) throws IOException {
            return null;
        }
    }
}
