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

package org.codehaus.httpcache4j.mutable;

import net.hamnaberg.funclite.Preconditions;
import org.codehaus.httpcache4j.Conditionals;
import org.codehaus.httpcache4j.Tag;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class MutableConditionals {
    private Conditionals conditionals;

    public MutableConditionals() {
        this(new Conditionals());
    }

    MutableConditionals(Conditionals conditionals) {
        this.conditionals = Preconditions.checkNotNull(conditionals, "Conditionals may not be null");
    }

    public void addIfMatch(Tag tag) {
        conditionals = conditionals.addIfMatch(tag);
    }

    public void addIfNoneMatch(Tag tag) {
        conditionals = conditionals.addIfNoneMatch(tag);
    }

    public void ifModifiedSince(LocalDateTime time) {
        conditionals = conditionals.ifModifiedSince(time);
    }

    public void ifUnModifiedSince(LocalDateTime time) {
        conditionals = conditionals.ifUnModifiedSince(time);
    }

    public List<Tag> getMatch() {
        return conditionals.getMatch();
    }

    public List<Tag> getNoneMatch() {
        return conditionals.getNoneMatch();
    }

    public LocalDateTime getModifiedSince() {
        return conditionals.getModifiedSince().orNull();
    }

    public LocalDateTime getUnModifiedSince() {
        return conditionals.getUnModifiedSince().orNull();
    }

    public boolean isUnconditional() {
        return conditionals.isUnconditional();
    }

    public Conditionals toConditionals() {
        return conditionals;
    }
}
