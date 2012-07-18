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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.codehaus.httpcache4j.util.NumberUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class Directive extends NameValue {
    private final List<Parameter> parameters;
    private Map<String, Parameter> parameterMap;
    
    public Directive(final String name, String value) {
        this(name, HeaderUtils.removeQuotes(value), Collections.<Parameter>emptyList());
    }

    public Directive(final String name, String value, List<Parameter> parameters) {
        super(name, HeaderUtils.removeQuotes(value));
        Preconditions.checkNotNull(parameters, "Parameters may not be null");
        this.parameters = ImmutableList.copyOf(parameters);
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public int getValueAsInteger() {
        return NumberUtils.toInt(getValue(), -1);
    }

    public Parameter getParameter(String name) {
        if (parameterMap == null) {
            synchronized (this) {
                if (parameterMap == null) {
                    ImmutableMap.Builder<String, Parameter> builder = ImmutableMap.builder();
                    for (Parameter parameter : parameters) {
                        builder.put(parameter.getName(), parameter);
                    }
                    parameterMap = builder.build();
                }
            }
        }
        return parameterMap.get(name);
    }

    public String getParameterValue(String name) {
        Parameter param = getParameter(name);
        if (param != null) {
            return param.getValue();
        }
        return null;
    }

    @Override
    public String toString() {
        String output = name;
        if (value != null && !value.isEmpty()) {
            output += "=" + value;
        }
        if (!parameters.isEmpty()) {
            output = output + "; " + Joiner.on("; ").join(parameters);
        }
        return output;
    }
}
