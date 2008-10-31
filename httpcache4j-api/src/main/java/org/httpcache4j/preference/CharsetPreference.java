package org.httpcache4j.preference;

import org.httpcache4j.HeaderConstants;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
public class CharsetPreference extends Preference<String> {
    public CharsetPreference(String preference) {
        super(preference);
    }

    protected String getHeaderName() {
        return HeaderConstants.ACCEPT_CHARSET;
    }
}
