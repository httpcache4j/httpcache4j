package org.codehaus.httpcache4j.cache;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.mutable.MutableHeaders;
import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.storage.jdbc.H2CacheStorage;
import org.joda.time.DateTime;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class CacheHandler {
    protected final CacheStorage cache;
    protected final CacheControl cacheControl;
    private final File tempDirectory;

    public static CacheHandler newInstance(ServletContext servletContext) {
        File tempDirectory = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        String rule = servletContext.getInitParameter("cache-control");
        return new CacheHandler(new H2CacheStorage(tempDirectory), new CacheControl(rule), tempDirectory);
    }

    public CacheHandler(CacheStorage storage, CacheControl control, File tempDirectory) {
        cache = storage;
        cacheControl = control;
        this.tempDirectory = tempDirectory;
    }

    public void service(RequestDispatcher dispatcher, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HTTPRequest r = translate(request);
        CacheItem item = cache.get(r);
        HTTPResponse resp;
        if (item != null) {
            resp = withCacheControl(item.getResponse());
        }
        else {
            ResponseWrapper wrapper = new ResponseWrapper(response, new File(tempDirectory, UUID.randomUUID().toString()));
            dispatcher.forward(request, wrapper);
            r = translate(request);
            resp = translate(wrapper);
        }
        write(resp, response);        
    }

    private HTTPResponse translate(ResponseWrapper response) {
        StatusLine line = response.getStatus();
        if (line == null) {
            line = new StatusLine(Status.OK);
        }
        return new HTTPResponse(new FilePayload(response.getTempFile(), MIMEType.valueOf(response.getContentType())), line, response.getHeaders());
    }

    private HTTPResponse withCacheControl(HTTPResponse response) {
        Headers headers = response.getHeaders();
        headers = headers.remove(HeaderConstants.CACHE_CONTROL);
        headers = headers.add(cacheControl.toHeader());
        return new HTTPResponse(response.getPayload(), response.getStatusLine(), headers);
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

    private static class ResponseWrapper extends HttpServletResponseWrapper {
        private final File tempFile;
        private final WrappedServletOutputStream stream;
        private final MutableHeaders headers = new MutableHeaders();
        private StatusLine status;

        public ResponseWrapper(HttpServletResponse response, File tempFile) throws IOException {
            super(response);
            this.tempFile = tempFile;
            tempFile.deleteOnExit();
            stream = new WrappedServletOutputStream(new FileOutputStream(tempFile));
        }

        @Override
        public void setDateHeader(String name, long date) {
            headers.set(HeaderUtils.toHttpDate(name, new DateTime(date)));
        }

        @Override
        public void addDateHeader(String name, long date) {
            headers.add(HeaderUtils.toHttpDate(name, new DateTime(date)));
        }

        @Override
        public void setHeader(String name, String value) {
            headers.set(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            headers.add(name, value);
        }

        @Override
        public void setIntHeader(String name, int value) {
            headers.set(name, String.valueOf(value));
        }

        @Override
        public void addIntHeader(String name, int value) {
            headers.add(name, String.valueOf(value));
        }

        public File getTempFile() {
            return tempFile;
        }

        public void deleteTempFile() {
            tempFile.delete();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return stream;
        }

        @Override
        public void setStatus(int sc) {
            super.setStatus(sc);
            status = new StatusLine(Status.valueOf(sc));
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            super.sendError(sc, msg);
            status = new StatusLine(Status.valueOf(sc), msg);
        }

        public StatusLine getStatus() {
            return status;
        }

        public Headers getHeaders() {
            if (!headers.hasHeader(HeaderConstants.DATE)) {
                headers.set(HeaderUtils.toHttpDate(HeaderConstants.DATE, new DateTime()));
            }
            return headers.toHeaders();
        }
    }

    private static class WrappedServletOutputStream extends ServletOutputStream {
        private OutputStream delegate;

        public WrappedServletOutputStream(OutputStream delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
        }                
    }
}
