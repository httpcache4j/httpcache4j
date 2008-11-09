package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.payload.Payload;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @author last modified by $Author: $
 * @version $Id: $
 */
public interface CleanablePayload extends Payload {
    void clean();
}