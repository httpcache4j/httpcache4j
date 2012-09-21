package org.codehaus.httpcache4j.spring;

import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.cache.HTTPCache;
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
public class HTTPCacheFactory implements ClientHttpRequestFactory, DisposableBean {
    private HTTPCache cache;

    public HTTPCacheFactory(HTTPCache cache) {
        this.cache = cache;
    }

    @Override
    public ClientHttpRequest createRequest(final URI uri, final HttpMethod httpMethod) throws IOException {
        return new AbstractHttpRequest(uri, httpMethod) {
            @Override
            protected ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
                addHeaders(headers);
                final HTTPResponse response = cache.execute(request.toRequest());
                return new HttpResponse(response);
            }
        };
    }

    @Override
    public void destroy() throws Exception {
        cache.shutdown();
    }
}
