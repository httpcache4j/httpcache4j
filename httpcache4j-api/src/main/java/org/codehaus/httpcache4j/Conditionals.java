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
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public final class Conditionals {
    private final List<Tag> match = new ArrayList<Tag>();
    private final List<Tag> nonMatch = new ArrayList<Tag>();
    private DateTime modifiedSince;
    private DateTime unModifiedSince;
    private static final String ERROR_MESSAGE = "The combination of %s and %s is undefined by the HTTP specification";

    /**
     * Adds tags to the If-Match header.
     *
     * @param tag the tag to add, may be null. This means the same as adding {@link Tag#ALL}
     * @throws IllegalArgumentException if ALL is supplied more than once, or you add a null tag more than once.
     */
    public void addIfMatch(Tag tag) {
        Validate.isTrue(modifiedSince == null, String.format(ERROR_MESSAGE, HeaderConstants.IF_MATCH, HeaderConstants.IF_MODIFIED_SINCE));
        Validate.isTrue(nonMatch == null || nonMatch.isEmpty(), String.format(ERROR_MESSAGE, HeaderConstants.IF_MATCH, HeaderConstants.IF_NON_MATCH));
        if (tag == null) {
            tag = Tag.ALL;
        }
        if (Tag.ALL.equals(tag)) {
            match.clear();
        }
        if (!match.contains(Tag.ALL)) {
            match.add(tag);
        }
        else {
            throw new IllegalArgumentException("Tag ALL already in the list");
        }
    }

    /**
     * Adds tags to the If-None-Match header.
     *
     * The meaning of "If-None-Match: *" is that the method MUST NOT be performed if the representation selected by
     * the origin server (or by a cache, possibly using the Vary mechanism, see section 14.44) exists,
     * and SHOULD be performed if the representation does not exist.
     * FThis feature is intended to be useful in preventing races between PUT operations. 
     *
     * @param tag the tag to add, may be null. This means the same as adding {@link Tag#ALL}
     * @throws IllegalArgumentException if ALL is supplied more than once, or you add a null tag more than once.
     */
    public void addIfNoneMatch(Tag tag) {
        Validate.isTrue(unModifiedSince == null, String.format(ERROR_MESSAGE, HeaderConstants.IF_NON_MATCH, HeaderConstants.IF_UNMODIFIED_SINCE));
        Validate.isTrue(match == null || match.isEmpty(), String.format(ERROR_MESSAGE, HeaderConstants.IF_NON_MATCH, HeaderConstants.IF_MATCH));
        if (tag == null) {
            tag = Tag.ALL;
        }
        if (Tag.ALL.equals(tag)) {
            nonMatch.clear();
        }
        if (!nonMatch.contains(Tag.ALL)) {
            nonMatch.add(tag);
        }
        else {
            throw new IllegalArgumentException("Tag ALL already in the list");
        }
    }

    /**
     * You should use the server's time here. Otherwise you might get unexpected results.
     *
     * @param time the time to check.
     */
    public void setIfModifiedSince(DateTime time) {
        Validate.isTrue(match == null || match.isEmpty(), String.format(ERROR_MESSAGE, HeaderConstants.IF_MODIFIED_SINCE, HeaderConstants.IF_MATCH));
        Validate.isTrue(unModifiedSince == null, String.format(ERROR_MESSAGE, HeaderConstants.IF_MODIFIED_SINCE, HeaderConstants.IF_UNMODIFIED_SINCE));
        modifiedSince = time;
    }

    /**
     * You should use the server's time here. Otherwise you might get unexpected results.
     *
     * @param time the time to check.
     */
    public void setIfUnModifiedSince(DateTime time) {
        Validate.isTrue(nonMatch == null || nonMatch.isEmpty(), String.format(ERROR_MESSAGE, HeaderConstants.IF_UNMODIFIED_SINCE, HeaderConstants.IF_NON_MATCH));
        Validate.isTrue(modifiedSince == null, String.format(ERROR_MESSAGE, HeaderConstants.IF_UNMODIFIED_SINCE, HeaderConstants.IF_MODIFIED_SINCE));
        unModifiedSince = time;
    }

    public List<Tag> getMatch() {
        return Collections.unmodifiableList(match);
    }

    public List<Tag> getNonMatch() {
        return Collections.unmodifiableList(nonMatch);
    }

    public DateTime getModifiedSince() {
        return modifiedSince;
    }

    public DateTime getUnModifiedSince() {
        return unModifiedSince;
    }

    public Headers toHeaders() {
        Headers headers = new Headers();
        if (!getMatch().isEmpty()) {
            headers.add(new Header(HeaderConstants.IF_MATCH, buildTagHeaderValue(getMatch())));
        }
        if (!getNonMatch().isEmpty()) {
            headers.add(new Header(HeaderConstants.IF_NON_MATCH, buildTagHeaderValue(getNonMatch())));
        }
        if (modifiedSince != null) {
            headers.add(HTTPUtils.toHttpDate(HeaderConstants.IF_MODIFIED_SINCE, modifiedSince));
        }
        if (unModifiedSince != null) {
            headers.add(HTTPUtils.toHttpDate(HeaderConstants.IF_UNMODIFIED_SINCE, unModifiedSince));
        }

        return headers;
    }

    private String buildTagHeaderValue(List<Tag> match) {
        StringBuilder builder = new StringBuilder();
        for (Tag tag : match) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(tag.format());
        }
        return builder.toString();
    }
}