package org.codehaus.httpcache4j.resolver;

import org.apache.commons.lang.Validate;

/**
 * Implementors should implement this instead of using the ResponseResolver interface directly.
 * 
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public abstract class AbstractResponseResolver implements ResponseResolver {
    private PayloadCreator payloadCreator;

    public AbstractResponseResolver(PayloadCreator payloadCreator) {
        Validate.notNull(payloadCreator, "You may not add a null Payload creator");
        this.payloadCreator = payloadCreator;
    }

    protected PayloadCreator getPayloadCreator() {
        return payloadCreator;
    }
}
