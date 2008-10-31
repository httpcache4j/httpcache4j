package org.httpcache4j;

import org.apache.commons.lang.Validate;
import static org.httpcache4j.HeaderConstants.*;
import org.httpcache4j.payload.Payload;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public final class HTTPResponse implements Serializable {
    private final Status status;
    private final Payload payload;
    private final Headers headers;
    private Tag ETag;
    private DateTime lastModified;
    private String charset;
    private List<HTTPMethod> allowedMethods;

    public HTTPResponse(Payload payload, Status status, Headers headers) {
        Validate.notNull(status, "You must supply a Status");
        Validate.notNull(headers, "You must supply some Headers");
        this.status = status;
        this.payload = payload;
        this.headers = headers;
        MIMEType mimeType = null;
        if (headers.hasHeader(ETAG)) {
            ETag = Tag.parse(headers.getFirstHeader(ETAG).getValue());
        }
        if (headers.hasHeader(CONTENT_TYPE)) {
            mimeType = new MIMEType(headers.getFirstHeader(CONTENT_TYPE).getValue());
        }
        if (headers.hasHeader(CONTENT_TYPE)) {
            lastModified = HTTPUtils.fromHttpDate(headers.getFirstHeader(CONTENT_TYPE));
        }
        if (headers.hasHeader(ALLOW)) {
            String value = headers.getFirstHeader(ALLOW).getValue();
            String[] parts = value.split(",");
            List<HTTPMethod> allowedMethods = new ArrayList<HTTPMethod>();
            for (String part : parts) {
                allowedMethods.add(HTTPMethod.valueOf(part.trim()));
            }
            this.allowedMethods = Collections.unmodifiableList(allowedMethods);
        }
        if (mimeType != null) {
            for (Parameter parameter : mimeType.getParameters()) {
                if ("charset".equals(parameter.getName())) {
                    charset = parameter.getValue();
                }
            }
        }
    }

    public boolean hasPayload() {
        return payload != null;
    }

    public Status getStatus() {
        return status;
    }

    public Payload getPayload() {
        return payload;
    }

    public Headers getHeaders() {
        return headers;
    }

    public Tag getETag() {
        return ETag;
    }

    public DateTime getLastModified() {
        return lastModified;
    }

    public String getCharset() {
        return charset;
    }

    public List<HTTPMethod> getAllowedMethods() {
        return allowedMethods;
    }
}