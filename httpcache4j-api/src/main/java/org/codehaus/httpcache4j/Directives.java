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
import org.codehaus.httpcache4j.util.DirectivesParser;

import java.io.Serializable;
import java.util.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class Directives implements Iterable<Directive>, Serializable {
    private final Map<String, Directive> directives = new LinkedHashMap<String, Directive>();

    public Directives() {
        this("");
    }

    public Directives(Iterable<Directive> directives) {
        for (Directive directive : directives) {
            this.directives.put(directive.getName(), directive);
        }
    }

    public Directives(String value) {
        this(DirectivesParser.DEFAULT.parse(value));
    }

    public boolean hasDirective(String key) {
        return directives.containsKey(key);
    }

    public String get(String key) {
        Directive directive = directives.get(key);
        if (directive == null) {
            return "";
        }
        return directive.getValue();
    }

    public int size() {
        return directives.size();
    }

    /**
     * @return a new Immutable iterator
     */
    public Iterator<Directive> iterator() {
        return ImmutableList.copyOf(directives.values()).iterator();
    }
    
    @Override
    public String toString() {
        return Iterables.toString(this);
    }
}
