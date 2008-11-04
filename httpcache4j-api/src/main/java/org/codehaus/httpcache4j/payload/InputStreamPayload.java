package org.codehaus.httpcache4j.payload;

import org.codehaus.httpcache4j.MIMEType;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamPayload implements Payload {
    private final InputStream stream;
    private MIMEType mimeType;
    private boolean available = true;

    public InputStreamPayload(InputStream stream, MIMEType mimeType) {
        this.mimeType = mimeType;
        if (stream != null) {
            this.stream = new WrappedInputStream(stream);
        } else {
            this.stream = null;
            available = false;
        }
    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isTransient() {
        return !stream.markSupported();
    }

    public InputStream getInputStream() {
        if (available) {
            return stream;
        }
        return null;
    }

    private class WrappedInputStream extends InputStream {
        private InputStream delegate;

        public WrappedInputStream(InputStream delegate) {
            this.delegate = delegate;
        }

        public int read() throws IOException {
            if (available) {
                available = false;
            }
            return delegate.read();
        }

        public int read(byte[] b) throws IOException {
            if (available) {
                available = false;
            }

            return delegate.read(b);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (available) {
                available = false;
            }
            return delegate.read(b, off, len);
        }

        public long skip(long n) throws IOException {
            if (available) {
                available = false;
            }
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
}
