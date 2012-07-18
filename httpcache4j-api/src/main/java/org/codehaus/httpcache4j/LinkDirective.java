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

import com.google.common.collect.Lists;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * http://www.w3.org/Protocols/9707-link-header.html
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class LinkDirective extends Directive {
    private final URI uri;

    public LinkDirective(Directive directive) {
        this(directive.getName(), directive.getParameters());
    }

    public LinkDirective(String name, List<Parameter> parameters) {
        this(URI.create(name), parameters);
    }

    public LinkDirective(URI uri, Iterable<Parameter> parameters) {
        super(uri.toString(), null, Lists.newArrayList(parameters));
        this.uri = uri;
    }

    public URI getURI() {
        return uri;
    }

    public String getRel() {
        return getParameterValue("rel");
    }

    public String getRev() {
        return getParameterValue("rev");
    }

    public String getTitle() {
        return getParameterValue("title");
    }

    public URI getAnchor() {
        String paramValue = getParameterValue("anchor");
        if (paramValue != null) {
            return URI.create(paramValue);
        }
        return null;
    }

    public String toString() {
        StringBuilder value = new StringBuilder();
        value.append(String.format("<%s>", uri));
        for (Parameter parameter : getParameters()) {
            if (value.length() > 0) {
                value.append("; ");
            }
            value.append(parameter);
        }
        return value.toString();
    }
}
