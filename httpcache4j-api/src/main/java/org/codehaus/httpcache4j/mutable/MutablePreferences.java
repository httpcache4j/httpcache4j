/*
 * Copyright (c) 2010. The Codehaus. All Rights Reserved.
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
 */

package org.codehaus.httpcache4j.mutable;

import com.google.common.base.Preconditions;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.preference.Preference;
import org.codehaus.httpcache4j.preference.Preferences;

import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class MutablePreferences {
    private Preferences preferences;

    public MutablePreferences() {
        this(new Preferences());
    }

    MutablePreferences(Preferences preferences) {
        this.preferences = Preconditions.checkNotNull(preferences, "Preferences may not be null");
    }

    public void addLocale(Locale locale) {
        preferences = preferences.addLocale(locale);
    }

    public void addLocale(Locale locale, double quality) {
        preferences = preferences.addLocale(locale, quality);
    }

    public void addMIMEType(MIMEType mimeType, double quality) {
        preferences = preferences.addMIMEType(mimeType, quality);
    }

    public void addMIMEType(MIMEType mimeType) {
        preferences = preferences.addMIMEType(mimeType);
    }

    public void addCharset(String charset) {
        preferences = preferences.addCharset(charset);
    }

    public void addCharset(String charset, double quality) {
        preferences = preferences.addCharset(charset, quality);
    }

    public List<Preference<Locale>> getAcceptLocales() {
        return preferences.getAcceptLocales();
    }

    public List<Preference<MIMEType>> getAcceptMIMETypes() {
        return preferences.getAcceptMIMETypes();
    }

    public List<Preference<String>> getAcceptCharset() {
        return preferences.getAcceptCharset();
    }

    public Preferences toPreferences() {
        return preferences;
    }
}
