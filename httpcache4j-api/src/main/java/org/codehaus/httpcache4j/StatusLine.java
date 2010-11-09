package org.codehaus.httpcache4j;

import org.apache.commons.lang.Validate;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public final class StatusLine {
    private final HTTPVersion version;
    private final String message;
    private final Status status;

    public StatusLine(Status status, String message) {
        this(HTTPVersion.HTTP_1_1, status, message);
    }

    public StatusLine(Status status) {
        this(HTTPVersion.HTTP_1_1, status, null);
    }

    public StatusLine(HTTPVersion version, Status status, String message) {
        Validate.notNull(version, "Version may not be null");
        Validate.notNull(status, "Status may not be null");
        this.version = version;
        this.status = status;
        this.message = message == null ? "" : message;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StatusLine that = (StatusLine) o;

        if (status != null ? !status.equals(that.status) : that.status != null) {
            return false;
        }
        if (version != that.version) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (status != null ? status.hashCode() : 0);
        return result;
    }
}
