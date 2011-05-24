package org.codehaus.httpcache4j.resolver.ning;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.ning.http.client.*;
import com.ning.http.client.generators.InputStreamBodyGenerator;
import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.auth.*;
import org.codehaus.httpcache4j.mutable.MutableHeaders;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.codehaus.httpcache4j.resolver.AbstractResponseResolver;
import org.codehaus.httpcache4j.resolver.ResolverConfiguration;
import org.codehaus.httpcache4j.util.ResponseWriter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.codehaus.httpcache4j.HTTPMethod.*;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class NingResponseResolver extends AbstractResponseResolver {
    private final AsyncHttpClient client;

    protected NingResponseResolver(ResolverConfiguration configuration, AsyncHttpClientConfig asyncConfig) {
        super(configuration);
        Validate.notNull(asyncConfig, "Async config may not be null");
        client = new AsyncHttpClient(new AsyncHttpClientConfig.Builder(asyncConfig).setUserAgent(configuration.getUserAgent()).build());
    }

    public NingResponseResolver(ResolverConfiguration configuration) {
        this(configuration, new AsyncHttpClientConfig.Builder().build());
    }

    public NingResponseResolver(ProxyAuthenticator proxyAuthenticator, Authenticator authenticator) {
        this(new ResolverConfiguration(proxyAuthenticator, authenticator));
    }

    @Override
    protected HTTPResponse resolveImpl(HTTPRequest request) throws IOException {
        Future<Response> responseFuture = execute(request);
        return translate(responseFuture);
    }

    public void shutdown() {
        client.close();
    }

    private HTTPResponse translate(Future<Response> responseFuture) throws IOException {
        try {
            Response response = responseFuture.get();
            StatusLine line = new StatusLine(Status.valueOf(response.getStatusCode()), response.getStatusText());
            FluentCaseInsensitiveStringsMap headers = response.getHeaders();
            MutableHeaders convertedHeaders = new MutableHeaders();
            for (Map.Entry<String, List<String>> entry : headers) {
                final String key = entry.getKey();
                List<String> values = entry.getValue();
                convertedHeaders.add(Lists.transform(values, stringToHeader(key)));
            }
            InputStream stream = response.getResponseBodyAsStream();

            String contentType = response.getContentType();
            return new HTTPResponse(new InputStreamPayload(stream, contentType != null ? MIMEType.valueOf(contentType) : null), line, convertedHeaders.toHeaders());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new HTTPException(e.getCause());
        }

        throw new HTTPException("Not possible to get response");
    }

    private Future<Response> execute(final HTTPRequest request) throws IOException {
        AsyncHttpClient.BoundRequestBuilder builder = builder(request.getRequestURI(), request.getMethod());
        if (request.getMethod().canHavePayload() && request.hasPayload()) {
            if (getConfiguration().isUseChunked()) {
                builder.setBody(new InputStreamBodyGenerator(request.getPayload().getInputStream()));
            }
            else {
                builder.setBody(request.getPayload().getInputStream());
            }
        }
        for (Header header : request.getAllHeaders()) {
            builder.addHeader(header.getName(), header.getValue());
        }
        return builder.execute();
    }

    private AsyncHttpClient.BoundRequestBuilder builder(URI uri, HTTPMethod method) {
        if (DELETE.equals(method)) {
            return client.prepareDelete(uri.toString());
        }
        else if (GET.equals(method)) {
            return client.prepareGet(uri.toString());
        }
        else if (HEAD.equals(method)) {
            return client.prepareHead(uri.toString());
        }
        else if (OPTIONS.equals(method)) {
            return client.prepareOptions(uri.toString());
        }
        else if (POST.equals(method)) {
            return client.preparePost(uri.toString());
        }
        else if (PUT.equals(method)) {
            return client.preparePut(uri.toString());
        }
        throw new IllegalArgumentException("Unable to create request for method " + method);
    }

    private Function<String, Header> stringToHeader(final String key) {
        return new Function<String, Header>() {
            public Header apply(String from) {
                return new Header(key, from);
            }
        };
    }
}
