/*
 * Copyright (c) 2009. The Codehaus. All Rights Reserved.
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

package org.codehaus.httpcache4j.preference;


import java.util.Locale;

/**
 * Represents a Language preference.
 * <p/>
 * Described in http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html section: 14.2 and 14.4
 */
public class LocalePreference extends Preference<Locale> {
    public static Locale ALL = new Locale("*");

    public LocalePreference(Locale us) {
        super(us);
    }

    public LocalePreference(Locale preference, double quality) {
        super(preference, quality);
    }

    @Override
    protected String getStringValue() {
        return getPreference().getLanguage();
    }
}
