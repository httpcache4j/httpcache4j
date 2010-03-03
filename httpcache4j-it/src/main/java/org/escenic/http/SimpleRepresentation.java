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

package org.escenic.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mogsie@escenic.com">Erik Mogensen</a>
 * @author last modified by $Author: ermo $
 * @version $Id: //depot/branches/personal/mogsie/esi-testcase/src/main/java/com/escenic/people/mogsie/esi/SimpleRepresentation.java#3 $
 */
public class SimpleRepresentation implements Representation {
  private static final Map<String, Representation> FILES = new HashMap<String, Representation>();

  private byte[] mBytes;
  private long mLastModified;
  private int mVersion;
  private String mContentType;
  private int mHitCount;

  public void accept(final InputStream pInputStream) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
    int b;
    while ((b = pInputStream.read()) != -1) {
      baos.write(b);
    }
    mBytes = baos.toByteArray();
    // (Round to nearest second) since HTTP has one-second granularity.
    mLastModified = System.currentTimeMillis() / 1000 * 1000;
    mVersion++;
  }

  /**
   * Return the representation for the given file or null if the file does not exist. If the file name is null or empty or
   * represents an unknown or deleted file the method returns null. If the file name is the string <tt>random</tt> then the
   * RandomRepresentation sInstance is returned.
   *
   * @param pFileName The name of the file or null for no file name
   *
   * @return The representation for that file or null if no representation exists.
   */
  public static Representation get(final String pFileName) {
    if ("random".equals(pFileName)) {
      return RandomRepresentation.sInstance;
    }
    return FILES.get(pFileName);
  }

  public static void put(final String pFileName, Representation pRepresentation) {
    FILES.put(pFileName, pRepresentation);
  }

  public static void remove(final String pFileName) {
    FILES.remove(pFileName);
  }

  public byte[] toByteArray() {
    return mBytes.clone();
  }

  public long getLastModified() {
    return mLastModified;
  }

  public int getVersion() {
    return mVersion;
  }

  @Override
  public String toString() {
    return (mBytes == null ? "no mBytes" : mBytes.length + " bytes")
        + " version#" + mVersion + " from " + new Date(mLastModified);
  }

  public String getContentType() {
    return mContentType;
  }

  public void setContentType(final String pContentType) {
    mContentType = pContentType;
  }

  public String getVersionString() {
    return Long.toHexString(new String(mBytes).hashCode());
  }

  public int getHitCount() {
    return mHitCount;
  }

  public synchronized void hit() {
    mHitCount++;
  }
}
