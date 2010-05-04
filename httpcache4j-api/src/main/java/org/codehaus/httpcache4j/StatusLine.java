package org.codehaus.httpcache4j;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class StatusLine {
    private final HTTPVersion version;
    private final String message;
    private final Status status;

    public StatusLine(HTTPVersion version, Status status, String message) {
        this.version = version;
        this.status = status;
        this.message = message == null ? "" : message;
    }

    public StatusLine(Status status, String message) {
        this(HTTPVersion.HTTP_1_1, status, message);
    }

    public StatusLine(Status status) {
        this(HTTPVersion.HTTP_1_1, status, null);
    }

    public HTTPVersion getVersion() {
        return version;
    }

    public String getMessage() {
        return message;
    }

    public Status getStatus() {
        return status;
    }
}
