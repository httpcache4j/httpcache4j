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
import java.util.Random;

/**
 * @author <a href="mailto:mogsie@escenic.com">Erik Mogensen</a>
 * @author last modified by $Author: ermo $
 * @version $Id: //depot/branches/personal/mogsie/esi-testcase/src/main/java/com/escenic/people/mogsie/esi/ErrorFilter.java#4 $
 */
public class ErrorFilter extends AbstractEsiFilter {

  private Random mRandom = new Random();

  public void doFilterImpl(final HttpServletRequest pRequest, final HttpServletResponse pResponse, final FilterChain pChain, final PathElement pPath, Representation pRepresentation) throws IOException, ServletException {
    if ((!pRequest.getMethod().equals("PUT")) && (!pRequest.getMethod().equals("DELETE"))) {
      int error = 500, rate = 100;
      try {
        String param = pPath.getParameters().get("rate");
        if (param != null) {
          rate = Integer.parseInt(param);
        }
      }
      catch (NumberFormatException e) {
        // nop
      }
      if (pRepresentation != null) {
        String pattern = pPath.getParameters().get("pattern");
        if (rate == 100 && pattern != null) {
          // Move first character to end.
          for (int i = 0; i < pRepresentation.getHitCount() % pattern.length(); i++) {
            pattern = pattern.substring(1) + pattern.charAt(0);
          }
          if (pattern.endsWith("1")) {
            rate = 0;
          }
        }
      }
      try {
        String param = pPath.getParameters().get("");
        if (param != null) {
          error = Integer.parseInt(param);
        }
      }
      catch (NumberFormatException e) {
        // nop
      }
      if (mRandom.nextFloat() * 99.999 < rate) {
        pResponse.sendError(error, pPath.getParameters().get("message"));
        return;
      }
    }
    pChain.doFilter(pRequest, pResponse);
  }
}
