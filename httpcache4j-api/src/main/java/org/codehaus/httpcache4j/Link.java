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

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * http://www.w3.org/Protocols/9707-link-header.html
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class Link {
    private final Map<String, Parameter> parameters;
    private final URI uri;

    public Link(Directive directive) {
        parameters = toMap(directive.getParameters());
        uri = URI.create(directive.getValue());
    }

    public Link(URI uri, Iterable<Parameter> parameters) {
        this.uri = uri;
        this.parameters = toMap(parameters); 
    }

    private Map<String, Parameter> toMap(Iterable<Parameter> parameters) {
        Map<String, Parameter> map = new LinkedHashMap<String, Parameter>();
        for (Parameter parameter : parameters) {
            map.put(parameter.getName(), parameter);
        }
        return map;
    }
    
    public URI getURI() {
        return uri;
    }

    public String getRel() {
        return getParameterValue("rel");
    }

    public String getParameterValue(final String key) {
        if (parameters.containsKey(key)) {
            return parameters.get(key).getValue();
        }
        return null;
    }

    public String getRev() {
        return getParameterValue("rev");
    }

    public String getTitle() {
        return getParameterValue("title");
    }

    public String getAnchor() {
        return getParameterValue("anchor");
    }

    public Map<String, Parameter> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public String toString() {
        StringBuilder value = new StringBuilder();
        value.append(String.format("<%s>", uri));
        for (Parameter parameter : parameters.values()) {
            if (value.length() > 0) {
                value.append("; ");
            }
            value.append(parameter);
        }
        return value.toString();
    }
}
