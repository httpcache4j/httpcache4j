package org.codehaus.httpcache4j.preference;


import org.codehaus.httpcache4j.HeaderConstants;

import java.util.Locale;

/**
 * Represents an Accept-Language and Accept-Charset preference.
 * <p/>
 * Described in http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html section: 14.2 and 14.4
 */
public class LocalePreference extends Preference<Locale> {
    public static Locale ALL = new Locale("*");

    public LocalePreference(Locale preference) {
        super(preference);
    }

    protected String getHeaderName() {
        return HeaderConstants.ACCEPT_LANGUAGE;
    }

    @Override
    protected String getStringValue() {
        return getPreference().getLanguage();
    }
}
