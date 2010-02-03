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
import com.google.common.collect.Iterables;

import java.io.Serializable;
import java.util.*;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class Directives implements Iterable<Directive>, Serializable {
    private final Map<String, String> directives = new LinkedHashMap<String, String>();

    public Directives() {
        this("");
    }

    public Directives(String value) {
        parse(value);
    }

    public boolean hasDirective(String key) {
        return directives.containsKey(key);
    }

    public String get(String key) {
        return new Directive(key, directives.get(key)).getValue();
    }

    public int size() {
        return directives.size();
    }

    public Iterator<Directive> iterator() {
        ImmutableList.Builder<Directive> builder = ImmutableList.builder();
        for (Map.Entry<String, String> entry : directives.entrySet()) {
            builder.add(new Directive(entry.getKey(), entry.getValue()));            
        }
        return builder.build().iterator();
    }

    private void parse(String value) {
        Map<String, String> parsedDirectives = new LinkedHashMap<String, String>();
        List<String> directives = Arrays.asList(value.split(","));
        for (String directive : directives) {
            directive = directive.trim();
            if (directive.length() > 0) {
                String[] directiveParts = directive.split("=", 2);
                parsedDirectives.put(directiveParts[0], directiveParts.length > 1 ? directiveParts[1] : null);
            }
        }
        this.directives.putAll(parsedDirectives);
    }

    @Override
    public String toString() {
        return Iterables.toString(this);
    }
}
