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

import org.apache.commons.lang.Validate;

import java.util.*;

/**
 * Represents a HTTP Header.
 */
public final class Header extends NameValue {
    private static final long serialVersionUID = 3652406179988246038L;
    private Map<String, String> directives = new HashMap<String, String>();

    public Header(String name, String value) {
        super(name, value);
        Validate.notEmpty(value, "The value of a Header may not be empty");
        parseDirectives(value);
    }

    private void parseDirectives(String value) {
        List<String> directives = Arrays.asList(value.split(","));
        for (String directive : directives) {
            directive = directive.trim();
            if (directive.length() > 0) {
                String[] directiveParts = directive.split("=", 2);
                this.directives.put(directiveParts[0], directiveParts.length > 1 ? directiveParts[1] : null);
            }
        }
    }

    @Override
    public final String toString() {
        return getName() + ": " + getValue();
    }

    public Map<String, String> getDirectives() {
        return Collections.unmodifiableMap(directives);
    }
}