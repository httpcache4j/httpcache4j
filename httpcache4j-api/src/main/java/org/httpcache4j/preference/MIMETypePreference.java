package org.httpcache4j.preference;

import org.httpcache4j.Header;
import org.httpcache4j.MIMEType;
import org.httpcache4j.HeaderConstants;

import java.util.Arrays;

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
