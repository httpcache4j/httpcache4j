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

import org.codehaus.httpcache4j.util.Streamable;

import java.io.Serializable;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public final class Directives implements Streamable<Directive>, Serializable {
    private final Map<String, Directive> directives;

    public Directives(Iterable<Directive> directives) {
        Map<String, Directive> m = StreamSupport.stream(directives.spliterator(), false).
                collect(Collectors.toMap(Directive::getName, Function.<Directive>identity(), throwingMerger(), LinkedHashMap::new));
        this.directives = Collections.unmodifiableMap(m);
    }

    public Directives() {
        directives = Collections.emptyMap();
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

    public Directive getAsDirective(String key) {
        return directives.get(key);
    }

    public int size() {
        return directives.size();
    }

    public Iterator<Directive> iterator() {
        return new ArrayList<>(directives.values()).iterator();
    }
    
    @Override
    public String toString() {
        return stream().map(Directive::toString).collect(Collectors.joining(", "));
    }

    public Directives add(Directive directive) {
        List<Directive> list = Stream.concat(stream(), Arrays.asList(directive).stream()).collect(Collectors.toList());
        return new Directives(list);
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); };
    }
}
