package org.httpcache4j.preference;

import org.httpcache4j.MIMEType;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 */
//TODO: add encoding preference.
public final class Preferences {
    private List<LocalePreference> acceptLocales = new ArrayList<LocalePreference>();
    private List<MIMETypePreference> acceptMIMETypes = new ArrayList<MIMETypePreference>();
    private List<CharsetPreference> acceptCharset = new ArrayList<CharsetPreference>();
    

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


}
