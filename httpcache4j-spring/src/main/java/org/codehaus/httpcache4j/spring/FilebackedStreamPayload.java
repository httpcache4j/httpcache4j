package org.codehaus.httpcache4j.spring;

import com.google.common.io.FileBackedOutputStream;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.payload.Payload;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.io.InputStream;

/**
* @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
*/
class FilebackedStreamPayload implements Payload {
    private final HttpHeaders httpHeaders;
    private final FileBackedOutputStream fileBackedStream;

    public FilebackedStreamPayload(HttpHeaders httpHeaders, FileBackedOutputStream fileBackedStream) {
        this.httpHeaders = httpHeaders;
        this.fileBackedStream = fileBackedStream;
    }

    @Override
    public MIMEType getMimeType() {
        return MIMEType.valueOf(httpHeaders.getContentType().toString());
    }

    @Override
    public InputStream getInputStream() {
        try {
            return fileBackedStream.getSupplier().getInput();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public long length() {
        return -1;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
