package org.codehaus.httpcache4j.servlet;

import org.codehaus.httpcache4j.cache.CacheHandler;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public abstract class CacheServlet implements Servlet {
    protected ServletConfig config;
    private CacheHandler handler;

    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        handler = newCacheHandler();
    }

    protected CacheHandler newCacheHandler() {
        return CacheHandler.newInstance(config.getServletContext());
    }

    public ServletConfig getServletConfig() {
        return config;
    }

    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        if (!(request instanceof HttpServletRequest || response instanceof HttpServletResponse)) {
            throw new ServletException("Not HTTP servlet, cannot continue");
        }
        //TODO get the dispatcher correctly. Currently sending null to get the build back on track
        handler.service(null, (HttpServletRequest)request, (HttpServletResponse)response);
    }

    public String getServletInfo() {
        return config.getServletName();
    }

    public void destroy() {
    }
}
