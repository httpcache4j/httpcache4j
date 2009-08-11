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

import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.Headers;
import org.codehaus.httpcache4j.HeaderConstants;
import org.codehaus.httpcache4j.HeaderUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Collections for preferences.
 * 
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
//TODO: add encoding preference. (handle decoding in client).
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
            headers.add(HeaderUtils.toHeader(HeaderConstants.ACCEPT, getAcceptMIMETypes()));
        }
        if (!getAcceptLocales().isEmpty()) {
            headers.add(HeaderUtils.toHeader(HeaderConstants.ACCEPT_LANGUAGE, getAcceptLocales()));
        }
        if (!getAcceptCharset().isEmpty()) {
            headers.add(HeaderUtils.toHeader(HeaderConstants.ACCEPT_CHARSET, getAcceptCharset()));
        }
        return headers;
    }
}
