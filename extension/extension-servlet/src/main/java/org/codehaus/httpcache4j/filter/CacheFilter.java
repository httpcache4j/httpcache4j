package org.codehaus.httpcache4j.filter;

import org.codehaus.httpcache4j.cache.CacheHandler;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public abstract class CacheFilter implements Filter {
    private CacheHandler handler;

    protected FilterConfig config;

    public final void init(FilterConfig filterConfig) throws ServletException {
        this.config = filterConfig;
        handler = newCacheHandler();
    }

    protected CacheHandler newCacheHandler() {
        return CacheHandler.newInstance(config.getServletContext());
    }


    public final void doFilter(ServletRequest request, ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest || response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }
        RequestDispatcher dispatcher = new FilterRequestDispatcher(chain);
        handler.service(dispatcher, (HttpServletRequest) request, (HttpServletResponse) response);
    }

    public void destroy() {

    }

    private static class FilterRequestDispatcher implements RequestDispatcher {
        private final FilterChain chain;

        public FilterRequestDispatcher(FilterChain chain) {
            this.chain = chain;
        }

        public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            chain.doFilter(request, response);
        }

        public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
            chain.doFilter(request, response);
        }
    }
}
