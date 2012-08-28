package org.codehaus.httpcache4j.spring;

import com.google.common.io.FileBackedOutputStream;
import org.codehaus.httpcache4j.HTTPMethod;
import org.codehaus.httpcache4j.mutable.MutableRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.AbstractClientHttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
* @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
*/
abstract class AbstractHttpRequest extends AbstractClientHttpRequest {
    protected MutableRequest request;

    public AbstractHttpRequest(URI uri, HttpMethod httpMethod) {
        request = new MutableRequest(uri, HTTPMethod.valueOf(httpMethod.name()));
    }

    @Override
    protected OutputStream getBodyInternal(final HttpHeaders httpHeaders) throws IOException {
        if (!request.getMethod().canHavePayload()) {
            throw new IllegalStateException(String.format("A %s request may not have a body", request.getMethod()));
        }
        FileBackedOutputStream fileBackedStream = new FileBackedOutputStream(2048);
        request.setPayload(new FilebackedStreamPayload(httpHeaders, fileBackedStream));
        return fileBackedStream;
    }

    protected void addHeaders(HttpHeaders httpHeaders) {
        for (Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
            for (String value : entry.getValue()) {
                request.addHeader(entry.getKey(), value);
            }
        }
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.valueOf(request.getMethod().getMethod());
    }

    @Override
    public URI getURI() {
        return request.getRequestURI();
    }
}
