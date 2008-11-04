package org.codehaus.httpcache4j.preference;

import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.HeaderConstants;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class MIMETypePreference extends Preference<MIMEType> {
    public MIMETypePreference(MIMEType preference) {
        super(preference);
    }

    protected String getHeaderName() {
        return HeaderConstants.ACCEPT;
    }
}
