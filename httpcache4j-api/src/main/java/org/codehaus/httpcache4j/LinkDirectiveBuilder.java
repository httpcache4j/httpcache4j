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
import java.util.LinkedHashMap;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class LinkDirectiveBuilder {
    private final LinkMap parameters = new LinkMap();
    private final URI uri;

    private LinkDirectiveBuilder(URI uri, LinkMap parameters) {
        this.uri = uri;
        this.parameters.putAll(parameters);
    }

    public LinkDirectiveBuilder uri(URI uri) {
        return new LinkDirectiveBuilder(uri, parameters);
    }

    public LinkDirectiveBuilder rel(String rel) {
        return parameter(new QuotedParameter("rel", rel));
    }

    public LinkDirectiveBuilder rev(String rev) {
        return parameter(new QuotedParameter("rev", rev));
    }

    public LinkDirectiveBuilder title(String title) {
        return parameter(new QuotedParameter("title", title));
    }

    public LinkDirectiveBuilder anchor(URI anchor) {
        return parameter(new QuotedParameter("anchor", anchor.toString()));
    }

    public LinkDirectiveBuilder parameter(Parameter parameter) {
        return new LinkDirectiveBuilder(uri, new LinkMap(parameters, parameter));
    }

    public LinkDirective build() {
        return new LinkDirective(uri, parameters.values());
    }

    public static LinkDirectiveBuilder create(URI uri) {
        return new LinkDirectiveBuilder(uri, new LinkMap());
    }

    private static class LinkMap extends LinkedHashMap<String, Parameter> {
        public LinkMap() {
            super();
        }

        public LinkMap(LinkMap m, Parameter directive) {
            super(m);
            put(directive.getName(), directive);
        }
    }
}
