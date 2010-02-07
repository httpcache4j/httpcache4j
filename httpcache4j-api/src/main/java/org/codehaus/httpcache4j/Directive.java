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

package org.codehaus.httpcache4j;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.Validate;

import java.util.Collections;
import java.util.List;


/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class Directive extends NameValue {
    private final List<Parameter> parameters;
    
    public Directive(final String name, String value) {
        this(name, fixQuotedString(value), Collections.<Parameter>emptyList());
    }

    public Directive(final String name, String value, List<Parameter> parameters) {
        super(name, fixQuotedString(value));
        Validate.notNull(parameters, "Paramaters may not be null");
        Validate.noNullElements(parameters, "Parameters may not contain any null elements");
        this.parameters = ImmutableList.copyOf(parameters);
    }

    private static String fixQuotedString(String value) {
        if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return name + "=" + value;
    }
}
