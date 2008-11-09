package org.codehaus.httpcache4j;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @author last modified by $Author: $
 * @version $Id: $
 */
public class HTTPException extends RuntimeException {

    public HTTPException(String message) {
        super(message);
    }

    public HTTPException(String message, Throwable cause) {
        super(message, cause);
    }
}