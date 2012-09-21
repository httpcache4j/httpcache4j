package org.codehaus.httpcache4j.spring;

import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Header;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.AbstractClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;

/**
* @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
*/
class HttpResponse extends AbstractClientHttpResponse {
    private final HTTPResponse response;

    public HttpResponse(HTTPResponse response) {
        this.response = response;
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return response.getStatus().getCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return response.getStatusLine().getMessage();
    }

    @Override
    public void close() {
        response.consume();
    }

    @Override
    public InputStream getBody() throws IOException {
        if (response.hasPayload()) {
            return response.getPayload().getInputStream();
        }
        return null;
    }

    @Override
    public HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        for (Header header : response.getHeaders()) {
            headers.add(header.getName(), header.getValue());
        }
        return headers;
    }
}
