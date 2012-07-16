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

import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeaders;

/**
 * @author imyousuf
 */
public class VaryResourceServlet extends HttpServlet {

    public static final String XML_HELLO_WORLD = "<xml>Hello World!</xml>";
    public static final String PLAIN_HELLO_WORLD = "Hello World!";
    private int count = 0;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        System.out.println("Server: Request! " + ++count + " - " + request.getRequestURI() + " @ Accept: " + acceptHeader);
        if (acceptHeader.startsWith("text/xml")) {
            writeTextXml(request, response);
        } else {
            writeTextPlain(request, response);
        }
    }

    private void writeTextPlain(HttpServletRequest request,
                                HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        commonWrites(response, PLAIN_HELLO_WORLD);
    }

    protected void commonWrites(HttpServletResponse response, String output) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(HttpHeaders.CACHE_CONTROL, "max-age=30000");
        Date date = new Date();
        response.setHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT);
        response.setDateHeader(HttpHeaders.DATE, date.getTime());
        response.getWriter().write(output);
        response.getWriter().close();
    }

    private void writeTextXml(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/xml");
        commonWrites(response, XML_HELLO_WORLD);
    }
}
