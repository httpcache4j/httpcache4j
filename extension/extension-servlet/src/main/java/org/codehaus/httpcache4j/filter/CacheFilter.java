package org.codehaus.httpcache4j.filter;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.cache.CacheStorage;
import org.codehaus.httpcache4j.cache.HTTPCache;
import org.codehaus.httpcache4j.mutable.MutableHeaders;
import org.codehaus.httpcache4j.resolver.ResponseResolver;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public abstract class CacheFilter implements Filter {
    private final HTTPCache cache;

    protected CacheFilter(CacheStorage storage, ResponseResolver resolver) {
        cache = new HTTPCache(storage, resolver);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest || response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }
        doFilterImpl((HttpServletRequest) request, (HttpServletResponse) response);
    }

    protected void doFilterImpl(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HTTPRequest r = translate(request);
        HTTPResponse resp = cache.doCachedRequest(r);
        write(resp, response);
    }

    private void write(HTTPResponse response, HttpServletResponse servletResponse) throws IOException {
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

    private HTTPRequest translate(HttpServletRequest request) {
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

    private List<Header> toHeaders(final String name, List<String> headers) {
        return Lists.transform(headers, new Function<String, Header>() {
            public Header apply(String from) {
                return new Header(name, from);
            }
        });
    }

    public void destroy() {

    }
}
