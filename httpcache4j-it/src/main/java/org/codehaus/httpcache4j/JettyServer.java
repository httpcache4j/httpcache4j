/*
 * Copyright (c) 2009. The Codehaus. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.codehaus.httpcache4j;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.escenic.http.servlet.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class JettyServer {
    public static final int PORT = 10987;

    private final Server server;

    public JettyServer() {
        this(PORT);
    }

    public JettyServer(int port) {
        server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        addFilter(context, CacheControlFilter.class, "cc");
        addFilter(context, ETagFilter.class, "etag");
        addFilter(context, IfModifiedSinceFilter.class, "lm");
        addFilter(context, BasicAuthFilter.class, "basic");
        addFilter(context, DigestAuthFilter.class, "digest");
        context.addServlet(RestServlet.class, "/*");

        context.setContextPath("/");
        server.setHandler(context);

        server.setSendDateHeader(true);
        server.setStopAtShutdown(true);
    }

    private void addFilter(ServletContextHandler handler, Class filter, String pathSpec) {
        FilterHolder holder = new FilterHolder(filter);
        if (pathSpec != null) {
            holder.setInitParameter("path", pathSpec);
        }
        handler.addFilter(holder, "/*", FilterMapping.DEFAULT);
    }

    public void start() {
        try {
            server.start();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            server.stop();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        JettyServer server = new JettyServer();
        server.start();
        try {
            server.server.join();
        } catch (InterruptedException e) {
            server.stop();
        }

    }

}
