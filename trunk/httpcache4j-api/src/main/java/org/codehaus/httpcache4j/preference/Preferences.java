/*
 * Copyright (c) 2008, The Codehaus. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.codehaus.httpcache4j.preference;

import org.codehaus.httpcache4j.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Collections;

/**
 * Collections for preferences.
 * 
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
//TODO: add encoding preference. (handle decoding in client).
public final class Preferences {
    private final List<Preference<Locale>> locales;
    private final List<Preference<MIMEType>> MIMETypes;
    private final List<Preference<String>> charsets;
    private static final double IGNORED_QUALITY = 1.0;

    public Preferences() {
        this(Collections.<Preference<Locale>>emptyList(), Collections.<Preference<MIMEType>>emptyList(), Collections.<Preference<String>>emptyList());
    }

    private Preferences(List<Preference<Locale>> locales, List<Preference<MIMEType>> MIMETypes, List<Preference<String>> charsets) {
        this.locales = locales;
        this.MIMETypes = MIMETypes;
        this.charsets = charsets;
    }

    public Preferences addLocale(Locale locale) {
        return addLocale(locale, IGNORED_QUALITY);
    }
    
    public Preferences addLocale(Locale locale, double quality) {
        List<Preference<Locale>> locales = new ArrayList<Preference<Locale>>(this.locales);
        LocalePreference preference = new LocalePreference(locale, quality);
        if (!locales.contains(preference)) {
            locales.add(preference);
        }
        return new Preferences(Collections.unmodifiableList(locales), MIMETypes, charsets);
    }

    public Preferences addMIMEType(MIMEType mimeType, double quality) {
        List<Preference<MIMEType>> MIMETypes = new ArrayList<Preference<MIMEType>>(this.MIMETypes);
        Preference<MIMEType> mime = new Preference<MIMEType>(mimeType, quality);
        if (!MIMETypes.contains(mime)) {
            MIMETypes.add(mime);
        }
        return new Preferences(locales, Collections.unmodifiableList(MIMETypes), charsets);
    }

    public Preferences addMIMEType(MIMEType mimeType) {
        return addMIMEType(mimeType, IGNORED_QUALITY);
    }

    public Preferences addCharset(String charset) {
        return addCharset(charset, IGNORED_QUALITY);
    }

    public Preferences addCharset(String charset, double quality) {
        List<Preference<String>> charsets = new ArrayList<Preference<String>>();
        Preference<String> preference = new Preference<String>(charset, quality);
        if (!charsets.contains(preference)) {
            charsets.add(preference);
        }
        return new Preferences(locales, MIMETypes, Collections.unmodifiableList(charsets));
    }

    public List<Preference<Locale>> getAcceptLocales() {
        return locales;
    }

    public List<Preference<MIMEType>> getAcceptMIMETypes() {
        return MIMETypes;
    }

    public List<Preference<String>> getAcceptCharset() {
        return charsets;
    }

    public Headers toHeaders() {
        Headers headers = new Headers();
        if (!getAcceptMIMETypes().isEmpty()) {
            headers = headers.add(toHeader(HeaderConstants.ACCEPT, getAcceptMIMETypes()));
        }
        if (!getAcceptLocales().isEmpty()) {
            headers = headers.add(toHeader(HeaderConstants.ACCEPT_LANGUAGE, getAcceptLocales()));
        }
        if (!getAcceptCharset().isEmpty()) {
            headers = headers.add(toHeader(HeaderConstants.ACCEPT_CHARSET, getAcceptCharset()));
        }
        return headers;
    }

    static Header toHeader(String headerName, List<? extends Preference<?>> preferences) {
        StringBuilder builder = new StringBuilder();
        for (Preference<?> preference : preferences) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(preference.toString());
        }
        return new Header(headerName, builder.toString());
    }
}
