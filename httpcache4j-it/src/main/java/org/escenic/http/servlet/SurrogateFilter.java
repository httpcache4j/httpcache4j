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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import java.io.IOException;

/**
 * Invoked if the path contains surrogate.
 * Looks for an ESI/1.0 token in the surrogate capability header and sets the surrogate-control header if present
 * Example of capability header:
 * <tt>Surrogate-Capability: orcl="webcache/1.0 Surrogate/1.0 ESI/1.0 ESI-Inline/1.0 ESI-INV/1.0 ORAESI/9.0.4"</tt>
 * @author <a href="mailto:mogsie@escenic.com">Erik Mogensen</a>
 * @author last modified by $Author: ermo $
 * @version $Id: //depot/branches/personal/mogsie/esi-testcase/src/main/java/com/escenic/people/mogsie/esi/SurrogateFilter.java#4 $
 */
public class SurrogateFilter extends AbstractEsiFilter {

  public void doFilterImpl(final HttpServletRequest pRequest, final HttpServletResponse pResponse, final FilterChain pChain, final PathElement pPath, Representation pRepresentation) throws IOException, ServletException {
    // Surrogate-Capability: foo="bar/1.0 baz/1.1", beer="bar/1.1"
    String header = pRequest.getHeader("Surrogate-Capability");
    if (header == null) {
      header = "";
    }
    for (String capabilityset : header.split(",")) {
      String[] capabilitysetsplit = capabilityset.split("=");  
      if (capabilitysetsplit.length == 2) {
        // String devicetoken = capabilitysetsplit[0];  // "foo"
        String capability = capabilitysetsplit[1];
        if (capability.matches(".*\\bESI/1\\.0.*")) {
          // we found a device token (foo) that can handle us!
          // add control header to indicate we're using all of its capabilities.
          pResponse.setHeader("Surrogate-Control", "content=" + capability);
          break;
        }
      }
    }
    pChain.doFilter(pRequest, pResponse);
  }
}
