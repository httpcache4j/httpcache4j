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

package org.codehaus.httpcache4j.payload;


import com.google.common.base.Preconditions;

import java.io.InputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:erlend@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class DelegatingInputStream extends InputStream {
    private final InputStream delegate;


    public DelegatingInputStream(InputStream delegate) {
        this.delegate = Preconditions.checkNotNull(delegate, "Delegate may not be null");
    }

    public int read() throws IOException {
        return delegate.read();
    }

    public int read(byte[] b) throws IOException {
        return delegate.read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return delegate.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        return delegate.skip(n);
    }

    public int available() throws IOException {
        return delegate.available();
    }

    public void close() throws IOException {
        delegate.close();
    }

    public void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    public void reset() throws IOException {
        delegate.reset();
    }

    public boolean markSupported() {
        return delegate.markSupported();
    }
}
