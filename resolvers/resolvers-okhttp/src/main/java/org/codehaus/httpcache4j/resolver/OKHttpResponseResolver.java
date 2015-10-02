package org.codehaus.httpcache4j.resolver;

import com.squareup.okhttp.*;
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
import java.util.concurrent.TimeUnit;

public class OKHttpResponseResolver extends AbstractResponseResolver {
    private final OkHttpClient client;

    protected OKHttpResponseResolver(ResolverConfiguration configuration) {
        super(configuration);
        client = new OkHttpClient();
        client.setAuthenticator(new NullAuthenticator());
        client.setFollowRedirects(false);
        client.setFollowSslRedirects(false);
        client.setCache(null);
        ConnectionConfiguration connConfig = configuration.getConnectionConfiguration();
        client.setConnectionPool(new ConnectionPool(
                connConfig.getMaxConnections().orElse(ConnectionPool.getDefault().getConnectionCount()),
                (5 * 60 * 1000) // 5 minutes
                ));
        connConfig.getConnectionRequestTimeout().ifPresent(i -> client.setReadTimeout(i, TimeUnit.MILLISECONDS));
        connConfig.getSocketTimeout().ifPresent(i -> client.setConnectTimeout(i, TimeUnit.MILLISECONDS));
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(connConfig.getMaxConnections().orElse(dispatcher.getMaxRequests()));
        dispatcher.setMaxRequestsPerHost(connConfig.getDefaultConnectionsPerHost().orElse(dispatcher.getMaxRequestsPerHost()));
        client.setDispatcher(dispatcher);

        if (connConfig.getTimeout().isPresent()) {
            client.setConnectTimeout(connConfig.getTimeout().get(), TimeUnit.MILLISECONDS);
        }
        HTTPHost proxyHost = configuration.getProxyAuthenticator().getConfiguration().getHost();
        if (proxyHost != null) {
            client.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost.getHost(), proxyHost.getPort())));
        }
    }

    @Override
    protected HTTPResponse resolveImpl(HTTPRequest request) throws IOException {
        final Request req = transformRequest(request);
        Response response = client.newCall(req).execute();
        return transformResponse(response);
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
        client.getDispatcher().getExecutorService().shutdown();
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
            try {
                available = false;
                return delegate.byteStream();
            } catch (IOException e) {
                throw new HTTPException(e);
            }
        }

        @Override
        public long length() {
            try {
                return delegate.contentLength();
            } catch (IOException e) {
                throw new HTTPException(e);
            }
        }

        @Override
        public boolean isAvailable() {
            return available;
        }
    }

    private static class NullAuthenticator implements Authenticator {
        @Override
        public Request authenticate(Proxy proxy, Response response) throws IOException {
            return null;
        }

        @Override
        public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
            return null;
        }
    }
}
