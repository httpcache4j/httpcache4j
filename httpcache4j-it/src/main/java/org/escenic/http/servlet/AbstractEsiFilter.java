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
import org.escenic.http.SimpleRepresentation;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mogsie@escenic.com">Erik Mogensen</a>
 * @author last modified by $Author: ermo $
 * @version $Id: //depot/branches/personal/mogsie/esi-testcase/src/main/java/com/escenic/people/mogsie/esi/ErrorFilter.java#4 $
 */
public abstract class AbstractEsiFilter implements Filter {
  protected FilterConfig mFilterConfig;
  private String mPath;

  public void init(FilterConfig pFilterConfig) throws ServletException {
    mFilterConfig = pFilterConfig;
    mPath = mFilterConfig.getInitParameter("path");
  }

  public void doFilter(ServletRequest pRequest, ServletResponse pResponse, FilterChain pChain) throws IOException, ServletException {
    if (!(pRequest instanceof HttpServletRequest)) {
      throw new ServletException("Not a HTTP container");
    }
    HttpServletRequest request = (HttpServletRequest) pRequest;
    HttpServletResponse response = (HttpServletResponse) pResponse;
    Map<String, PathElement> paths = getPaths(request);
    PathElement path = paths.get(mPath);
    if (mPath != null && path == null) {
      pChain.doFilter(request, response);
      return;
    }
    Representation representation = getRepresentation(request);
    mFilterConfig.getServletContext().log("In Filter: " + mFilterConfig.getFilterName());
    doFilterImpl(request, response, pChain, path, representation);
  }

  protected Representation getRepresentation(final HttpServletRequest pRequest) {
    return SimpleRepresentation.get(getFileName(pRequest));
  }

  public abstract void doFilterImpl(final HttpServletRequest pRequest, final HttpServletResponse pResponse, final FilterChain pChain, final PathElement pPath, Representation pRepresentation) throws IOException, ServletException;

  public void destroy() {
  }

  @SuppressWarnings("unchecked")
  public static Map<String, PathElement> getPaths(final HttpServletRequest pRequest) {
    Map<String, PathElement> paths = (Map<String, PathElement>) pRequest.getAttribute(AbstractEsiFilter.class.getName() + ".PATH");
    if (paths == null) {
      paths = new HashMap<String, PathElement>();
      String path = calculatePath(pRequest);
      for (String p : path.split("/")) {
        PathElement element = new PathElement(p);
        if (element.getName() != null && !element.getName().equals("")) {
          paths.put(element.getName(), element);
        }
      }
    }
    return paths;
  }

  private static String calculatePath(final HttpServletRequest pRequest) {
    String path = pRequest.getServletPath();
    if (path == null) {
      path = "";
    }
    String pathInfo = pRequest.getPathInfo();
    if (pathInfo != null) {
      path = path + pathInfo;
    }
    return path;
  }

  /**
   * Return the file name of the request or null if no file name was specified.  The file name is the part of the path after the
   * last forward slash (/)
   *
   * @param pRequest The incoming request
   *
   * @return The file name of null if the request was for no specific file name
   */
  public static String getFileName(final HttpServletRequest pRequest) {
    String path = calculatePath(pRequest);
    if (path == null) {
      return null;
    }
    if (!path.contains("/")) {
      return null;
    }
    return path.substring(path.lastIndexOf("/") + 1);
  }

  public String getPath() {
    return mPath;
  }

  /** An encapsulation of part of a path with the syntax: <tt>pathname[,paramname[=paramvalue]]</tt> */
  public static class PathElement {
    private String mName;
    private final Map<String, String> mParameters;

    /**
     * Create a PathElement based on the specified directory name.  The directory name may have the following forms: <ul>
     * <li>something</li> <li>foo,param=value</li> <li>bar,otherparam</li> <li>baz,178</li> </ul>
     *
     * <ul> <li>Something is just there and has no parameters</li> <li>foo has a named parameter</li> <li>bar has a named parameter
     * with an empty string as the value</li> <li>baz has an anonymous parameter with the empty string as the key</li> </ul> Empty
     * named parameters and anonymous parameters are basically the same thing.  A PathElement can only handle one of them.
     *
     * @param pDirectoryName The part of the path between two forward slashes; typically corresponding to a directory.
     */
    public PathElement(final String pDirectoryName) {
      Map<String, String> set = new HashMap<String, String>();
      for (String part : pDirectoryName.split(",")) {
        if (mName == null) {
          mName = part;
          continue;
        }
        if (!part.contains("=")) {
          if (set.containsKey("")) {
            throw new RuntimeException("Max 1 anonymous parameter");
          }
          set.put("", part);
          set.put(part, "");
          continue;
        }
        String key = part.substring(0, part.indexOf("="));
        if (key.equals("")) {
          // ignoring malformed anonymous parameter...
          continue;
        }
        String value = part.substring(part.indexOf("=") + 1);
        set.put(key, value);
      }
      mParameters = Collections.unmodifiableMap(set);
    }

    public String getName() {
      return mName;
    }

    public void setName(final String pName) {
      mName = pName;
    }

    public Map<String, String> getParameters() {
      return mParameters;
    }

    @Override
    public String toString() {
      return getName() + "," + mParameters;
    }
  }
}