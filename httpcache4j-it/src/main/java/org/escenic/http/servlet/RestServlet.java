/*
 * Copyright (c) 2007, Escenic AS
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Escenic AS nor the names of its contributors may
 *       be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY ESCENIC AS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL ESCENIC AS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.escenic.http.servlet;

import org.escenic.http.SimpleRepresentation;
import org.escenic.http.Representation;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:mogsie@escenic.com">Erik Mogensen</a>
 * @author last modified by $Author: ermo $
 * @version $Id: //depot/branches/personal/mogsie/esi-testcase/src/main/java/com/escenic/people/mogsie/esi/RestServlet.java#3 $
 */
public class RestServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {
    System.out.println("RestServlet.doGet");
    //pResponse.setDateHeader("Date", System.currentTimeMillis());
    String path = AbstractEsiFilter.getFileName(pRequest);
    if (path == null || path.length() == 0 || path.equals("/")) {
      InputStream input = getClass().getResourceAsStream("/index.html");
      ServletOutputStream output = pResponse.getOutputStream();
      int b;
      while ((b = input.read()) != -1) {
        output.write(b);
      }
      return;
    }
    Representation repr = SimpleRepresentation.get(AbstractEsiFilter.getFileName(pRequest));
    if (repr == null) {
      pResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    pResponse.setStatus(HttpServletResponse.SC_OK);
    if (repr.getContentType() != null) {
      pResponse.setContentType(repr.getContentType());
    }
    pResponse.getOutputStream().write(repr.toByteArray());
  }

  @Override
  protected void doPut(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {
    System.out.println("RestServlet.doPut");
    Representation d = SimpleRepresentation.get(AbstractEsiFilter.getFileName(pRequest));
    if (d == null) {
      d = new SimpleRepresentation();
      SimpleRepresentation.put(AbstractEsiFilter.getFileName(pRequest), d);
    }
    d.accept(pRequest.getInputStream());
    if (pRequest.getContentType() != null) {
      d.setContentType(pRequest.getContentType());
    }
    pResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  @Override
  protected void doDelete(HttpServletRequest pRequest, HttpServletResponse pResponse) throws ServletException, IOException {
    System.out.println("RestServlet.doDelete");
    SimpleRepresentation.remove(AbstractEsiFilter.getFileName(pRequest));
    pResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

}
