package org.httpcache4j.resolver.ning;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.Response;
import org.apache.log4j.BasicConfigurator;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.auth.*;
import org.codehaus.httpcache4j.mutable.MutableHeaders;
import org.codehaus.httpcache4j.payload.InputStreamPayload;
import org.codehaus.httpcache4j.resolver.AbstractResponseResolver;
import org.codehaus.httpcache4j.util.ResponseWriter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.codehaus.httpcache4j.HTTPMethod.*;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class NingResponseResolver extends AbstractResponseResolver {
    private final AsyncHttpClient client;

    public NingResponseResolver(ProxyAuthenticator proxyAuthenticator, Authenticator authenticator) {
        super(proxyAuthenticator, authenticator);
        client = new AsyncHttpClient();
    }

    @Override
    protected HTTPResponse resolveImpl(HTTPRequest request) throws IOException {
        Request realRequest = translate(request);
        return translate(client.executeRequest(realRequest));
    }

    private HTTPResponse translate(Future<Response> responseFuture) throws IOException {
        try {
            System.out.println("NingResponseResolver.translate");
            Response response = responseFuture.get();
            System.out.println("NingResponseResolver.translate");
            StatusLine line = new StatusLine(Status.valueOf(response.getStatusCode()), response.getStatusText());
            com.ning.http.client.Headers headers = response.getHeaders();
            MutableHeaders convertedHeaders = new MutableHeaders();
            for (Map.Entry<String, List<String>> entry : headers) {
                final String key = entry.getKey();
                List<String> values = entry.getValue();
                convertedHeaders.add(key, Lists.transform(values, stringToHeader(key)));
            }
            InputStream stream = response.getResponseBodyAsStream();
            
            return new HTTPResponse(new InputStreamPayload(stream, MIMEType.valueOf(response.getContentType())), line, convertedHeaders.toHeaders());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new HTTPException(e.getCause());
        }

        return null;
    }

    private Request translate(HTTPRequest request) {
        AsyncHttpClient.BoundRequestBuilder builder = builder(request.getRequestURI(), request.getMethod());
        if (request.getMethod().canHavePayload()) {
            builder = builder.setBody(request.getPayload().getInputStream());
        }
        for (Header header : request.getAllHeaders()) {
            builder = builder.addHeader(header.getName(), header.getValue());
        }
        return builder.build();
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
        return null;
    }

    private Function<String, Header> stringToHeader(final String key) {
        return new Function<String, Header>() {
            public Header apply(String from) {
                return new Header(key, from);
            }
        };
    }

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        NingResponseResolver resolver = new NingResponseResolver(new DefaultProxyAuthenticator(new ProxyConfiguration()), new DefaultAuthenticator());
        AsyncHttpClient.BoundRequestBuilder builder = resolver.builder(URI.create("http://www.vg.no"), GET);
        Future<String> resp = builder.execute(new AsyncCompletionHandler<String>() {
            @Override
            public String onCompleted(Response response) throws Exception {
                return response.getStatusText();
            }
        });
        String fo = resp.get(100, TimeUnit.MILLISECONDS);
        System.out.println("fo = " + fo);
        HTTPResponse response = resolver.resolve(new HTTPRequest(URI.create("http://www.vg.no")));
        ResponseWriter writer = new ResponseWriter(response);
        writer.write();
    }
}
