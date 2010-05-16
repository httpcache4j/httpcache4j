package org.codehaus.httpcache4j.cache;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.cache.CacheStorage;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.mutable.MutableHeaders;
import org.codehaus.httpcache4j.resolver.HTTPClientResponseResolver;
import org.codehaus.httpcache4j.resolver.ResponseResolver;
import org.codehaus.httpcache4j.storage.jdbc.H2CacheStorage;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class CacheHandler {
    protected final HTTPCache cache;

    public static CacheHandler newInstance(ServletContext servletContext) {
        File tempDirectory = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        return new CacheHandler(new H2CacheStorage(tempDirectory), HTTPClientResponseResolver.createMultithreadedInstance());
    }

    public CacheHandler(CacheStorage storage, ResponseResolver resolver) {
        cache = new HTTPCache(storage, resolver);
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HTTPRequest r = translate(request);
        HTTPResponse resp = cache.doCachedRequest(r);
        write(resp, response);
    }

    protected void write(HTTPResponse response, HttpServletResponse servletResponse) throws IOException {
        Headers headers = response.getHeaders();
        Status status = response.getStatus();
        if (status.isClientError() || status.isServerError()) {
            StatusLine line = response.getStatusLine();
            servletResponse.sendError(status.getCode(), line.getMessage());
        }
        else {
            servletResponse.setStatus(status.getCode());
        }
        for (Header header : headers) {
            servletResponse.addHeader(header.getName(), header.getValue());
        }

        if (response.hasPayload()) {
            ServletOutputStream stream = servletResponse.getOutputStream();
            try {
                IOUtils.copy(response.getPayload().getInputStream(), stream);
            }
            finally {
                response.consume();
                stream.flush();
                stream.close();
            }
        }
    }

    protected HTTPRequest translate(HttpServletRequest request) {
        MutableHeaders headers = new MutableHeaders();
        List<String> names = Collections.<String>list(request.getHeaderNames());
        for (String name : names) {
            headers.add(name, toHeaders(name, Collections.<String>list(request.getHeaders(name))));
        }
        HTTPRequest req = new HTTPRequest(getRequestURI(request), HTTPMethod.valueOf(request.getMethod()));
        return req.headers(headers.toHeaders());
    }

    protected URI getRequestURI(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        if (request.getQueryString() != null) {
            url.append("?");
            url.append(request.getQueryString());
        }
        return URI.create(url.toString());
    }

    protected final List<Header> toHeaders(final String name, List<String> headers) {
        return Lists.transform(headers, new Function<String, Header>() {
            public Header apply(String from) {
                return new Header(name, from);
            }
        });
    }
}
