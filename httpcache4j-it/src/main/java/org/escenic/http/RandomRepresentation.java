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

import java.util.Random;
import java.io.InputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:mogsie@escenic.com">Erik Mogensen</a>
 * @author last modified by $Author: ermo $
 * @version $Id: //depot/branches/personal/mogsie/esi-testcase/src/main/java/com/escenic/people/mogsie/esi/RandomRepresentation.java#1 $
 */
public class RandomRepresentation implements Representation {
  private final Random mRandom = new Random();
  public static final Representation sInstance = new RandomRepresentation();
  private int mHitCount;

  public long getLastModified() {
    return System.currentTimeMillis();
  }

  public byte[] toByteArray() {
    return Long.toHexString(mRandom.nextLong()).getBytes();
  }

  public void accept(final InputStream pInputStream) throws IOException {
    // Don't do anything...
  }

  public int getVersion() {
    return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
  }

  public String getVersionString() {
    return new String(toByteArray());    
  }

  public int getHitCount() {
    return mHitCount;
  }

  public synchronized void hit() {
    mHitCount++;
  }

  public String getContentType() {
    return "text/plain";
  }

  public void setContentType(final String pContentType) {
    // ignore incoming content types...
  }
}
