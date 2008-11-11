package org.codehaus.httpcache4j.preference;

import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.HeaderConstants;
import org.codehaus.httpcache4j.HTTPUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Collections;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
//TODO: add encoding preference.
public final class Preferences {
    private List<Preference<Locale>> acceptLocales = new ArrayList<Preference<Locale>>();
    private List<Preference<MIMEType>> acceptMIMETypes = new ArrayList<Preference<MIMEType>>();
    private List<Preference<String>> acceptCharset = new ArrayList<Preference<String>>();

    public void addLocale(Locale locale) {
        LocalePreference preference = new LocalePreference(locale);
        if (!acceptLocales.contains(preference)) {
            acceptLocales.add(preference);
        }
    }

    public void addMIMEType(MIMEType mimeType) {
        MIMETypePreference mime = new MIMETypePreference(mimeType);
        if (!acceptMIMETypes.contains(mime)) {
            acceptMIMETypes.add(mime);
        }
    }

    public void addCharset(String charset) {
        CharsetPreference preference = new CharsetPreference(charset);
        if (!acceptCharset.contains(preference)) {
            acceptCharset.add(preference);
        }
    }

    public List<Preference<Locale>> getAcceptLocales() {
        return acceptLocales;
    }

    public List<Preference<MIMEType>> getAcceptMIMETypes() {
        return acceptMIMETypes;
    }

    public List<Preference<String>> getAcceptCharset() {
        return acceptCharset;
    }

    public Headers toHeaders() {
        Headers headers = new Headers();
        if (!getAcceptMIMETypes().isEmpty()) {
            headers.add(HTTPUtils.toHeader(HeaderConstants.ACCEPT, getAcceptMIMETypes()));

        }
        else if (!getAcceptLocales().isEmpty()) {
            headers.add(HTTPUtils.toHeader(HeaderConstants.ACCEPT_LANGUAGE, getAcceptLocales()));

        }
        else if (!getAcceptCharset().isEmpty()) {
            headers.add(HTTPUtils.toHeader(HeaderConstants.ACCEPT_CHARSET, getAcceptCharset()));
        }
        return headers;
    }
}
