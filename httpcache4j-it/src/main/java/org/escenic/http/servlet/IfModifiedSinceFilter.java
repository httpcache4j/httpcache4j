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

import org.escenic.http.Representation;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Checks for conditional methods returning 304 or 412 appropriately. This filter returns 304 NOT MODIFIED if an If-Modified-Since
 * header is equal to the resource's modification time. The filter returns 412 PRECONDITION FAILED if an If-Unmodified-Since header
 * is <em>not</em> equal to the resource's modification time.  (That's a tripple negative, sorry!) Put in other words, It _works_ if
 * the If-Unmodified-Since header is equal to the resource's modification time
 *
 * @author <a href="mailto:mogsie@escenic.com">Erik Mogensen</a>
 * @author last modified by $Author: ermo $
 * @version $Id: //depot/branches/personal/mogsie/esi-testcase/src/main/java/com/escenic/people/mogsie/esi/IfModifiedSinceFilter.java#3 $
 */
public class IfModifiedSinceFilter extends AbstractEsiFilter {
  public void doFilterImpl(final HttpServletRequest pRequest, final HttpServletResponse pResponse, final FilterChain pChain, final PathElement pPath, Representation pRepresentation) throws IOException, ServletException {
    if (pRepresentation != null) {
      long ifModifiedSince = pRequest.getDateHeader("If-Modified-Since");
      if (ifModifiedSince != -1) {
        if (ifModifiedSince == pRepresentation.getLastModified()) {
          pResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
          return;
        }
      }
      long ifUnmodifiedSince = pRequest.getDateHeader("If-Unmodified-Since");
      if (ifUnmodifiedSince != -1) {
        if (ifUnmodifiedSince != pRepresentation.getLastModified()) {
          pResponse.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
          return;
        }
      }
    }
    pChain.doFilter(pRequest, pResponse);
  }
}
