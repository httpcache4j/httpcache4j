package org.codehaus.httpcache4j.payload;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.codehaus.httpcache4j.MIMEType;

import java.io.InputStream;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class StringPayload implements Payload {
    private String value;
    private MIMEType mimeType;

    public StringPayload(String value, MIMEType mimeType) {
        Validate.notNull(value, "String value may not be null");
        Validate.notNull(mimeType, "MIMEType may not be null");

        this.value = value;
        this.mimeType = mimeType;
    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        return IOUtils.toInputStream(value);
    }

    public boolean isAvailable() {
        return true;
    }
}
