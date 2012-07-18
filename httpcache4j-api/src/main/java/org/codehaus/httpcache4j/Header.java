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

package org.codehaus.httpcache4j;

import org.codehaus.httpcache4j.util.AuthDirectivesParser;
import org.codehaus.httpcache4j.util.DirectivesParser;

import java.util.Locale;

/**
 * Represents a HTTP Header.
 */
public final class Header extends NameValue {
    private Directives directives;

    public Header(String name, String value) {
        super(name, value);
    }

    public Header(String name, Directives directives) {
        super(name, directives.toString());
        this.directives = directives;
    }

    @Override
    public final String toString() {
        return getName() + ": " + getValue();
    }

    public Directives getDirectives() {
        if (directives == null) {
            if (HeaderConstants.AUTHENTICATION_HEADERS.contains(getName().toLowerCase(Locale.ENGLISH))) {
                directives = AuthDirectivesParser.parse(value);
            }
            else {
                directives = DirectivesParser.parse(value);
            }
        }
        return directives;
    }

    static Header valueOf(String value) {
        String[] parts = value.split(":", 2);
        if (parts != null) {
            if (parts.length == 1) {
                return new Header(parts[0].trim(), "");
            }
            else if (parts.length == 2) {
                return new Header(parts[0].trim(), parts[1].trim());
            }
        }
        throw new IllegalArgumentException("Not a valid header string");
    }
}
