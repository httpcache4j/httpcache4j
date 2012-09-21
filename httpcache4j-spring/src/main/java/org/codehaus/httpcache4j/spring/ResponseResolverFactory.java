package org.codehaus.httpcache4j.spring;

import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.resolver.ResponseResolver;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;

/**
 * @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
 */
public class ResponseResolverFactory implements ClientHttpRequestFactory, DisposableBean {
    private ResponseResolver resolver;

    public ResponseResolverFactory(ResponseResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public ClientHttpRequest createRequest(final URI uri, final HttpMethod httpMethod) throws IOException {
        return new AbstractHttpRequest(uri, httpMethod) {
            @Override
            protected ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
                addHeaders(headers);
                final HTTPResponse response = resolver.resolve(request.toRequest());
                return new HttpResponse(response);
            }
        };
    }

    @Override
    public void destroy() throws Exception {
        resolver.shutdown();
    }
}
