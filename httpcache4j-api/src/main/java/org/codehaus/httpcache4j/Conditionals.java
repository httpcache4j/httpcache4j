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


import org.codehaus.httpcache4j.util.OptionalUtils;
import org.codehaus.httpcache4j.util.Preconditions;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the different conditional types that an HTTP request may have.
 * This are basically 4 things:
 * <ul>
 *   <li>If-Match</li>
 *   <li>If-None-Match</li>
 *   <li>If-Unmodified-Since</li>
 *   <li>If-Modified-Since</li>
 * </ul>
 *
 * <p> Combinations of these conditionals are possible with the following exceptions </p>
 * 
 * <table summary="Usage">
 *   <thead>
 *       <tr><th>Conditional</th><th>Can be combined with</th><th>Unspecified</th></tr>
 *   </thead>
 *   <tbody>
 *   <tr>
 *     <td>If-Match</td><td>If-Unmodified-Since</td><td>If-None-Match, If-Modified-Since</td>
 *   </tr>
 *   <tr>
 *     <td>If-None-Match</td><td>If-Modified-Since</td><td>If-Match, If-Unmodified-Since</td>
 *   </tr>
 *   <tr>
 *     <td>If-Unmodified-Since</td><td>If-Match</td><td>If-None-Match, If-Modified-Since</td>
 *   </tr>
 *   <tr>
 *     <td>If-Modified-Since</td><td>If-None-Match</td><td>If-Match, If-Unmodified-Since</td>
 *   </tr>
 *   </tbody>
 * </table>
 *
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 */
public final class Conditionals {
    private final List<Tag> match;
    private final List<Tag> noneMatch;
    private final Optional<LocalDateTime> modifiedSince;
    private final Optional<LocalDateTime> unModifiedSince;
    private static final String ERROR_MESSAGE = "The combination of %s and %s is undefined by the HTTP specification";

    public Conditionals() {
        this(empty(), empty(), Optional.empty(), Optional.empty());
    }

    private static List<Tag> empty() {
        return Collections.emptyList();
    }

    public Conditionals(List<Tag> match, List<Tag> noneMatch, Optional<LocalDateTime> modifiedSince, Optional<LocalDateTime> unModifiedSince) {
        this.match = match;
        this.noneMatch = noneMatch;
        this.modifiedSince = modifiedSince;
        this.unModifiedSince = unModifiedSince;
    }

    /**
     * Adds tags to the If-Match header.
     *
     * @param tag the tag to add, may be null. This means the same as adding {@link Tag#ALL}
     * @throws IllegalArgumentException if ALL is supplied more than once, or you add a null tag more than once.
     * @return a new Conditionals object with the If-Match tag added.
     */
    public Conditionals addIfMatch(Tag tag) {
        Preconditions.checkArgument(!modifiedSince.isPresent(), String.format(ERROR_MESSAGE, HeaderConstants.IF_MATCH, HeaderConstants.IF_MODIFIED_SINCE));
        Preconditions.checkArgument(noneMatch.isEmpty(), String.format(ERROR_MESSAGE, HeaderConstants.IF_MATCH, HeaderConstants.IF_NONE_MATCH));
        List<Tag> match = new ArrayList<>(this.match);

        if (tag == null) {
            tag = Tag.ALL;
        }
        if (Tag.ALL.equals(tag)) {
            match.clear();
        }
        if (!match.contains(Tag.ALL)) {
            if (!match.contains(tag)) {
                match.add(tag);
            }
        }
        else {
            throw new IllegalArgumentException("Tag ALL already in the list");
        }
        return new Conditionals(Collections.unmodifiableList(match), empty(), Optional.empty(), unModifiedSince);
    }

    /**
     * Adds tags to the If-None-Match header.
     *
     * The meaning of "If-None-Match: *" is that the method MUST NOT be performed if the representation selected by
     * the origin server (or by a cache, possibly using the Vary mechanism, see section 14.44) exists,
     * and SHOULD be performed if the representation does not exist.
     * This feature is intended to be useful in preventing races between PUT operations. 
     *
     * @param tag the tag to add, may be null. This means the same as adding {@link Tag#ALL}
     * @throws IllegalArgumentException if ALL is supplied more than once, or you add a null tag more than once.
     * @return a new Conditionals object with the If-None-Match tag added.
     */
    public Conditionals addIfNoneMatch(Tag tag) {
        Preconditions.checkArgument(!unModifiedSince.isPresent(), String.format(ERROR_MESSAGE, HeaderConstants.IF_NONE_MATCH, HeaderConstants.IF_UNMODIFIED_SINCE));
        Preconditions.checkArgument(match.isEmpty(), String.format(ERROR_MESSAGE, HeaderConstants.IF_NONE_MATCH, HeaderConstants.IF_MATCH));
        List<Tag> noneMatch = new ArrayList<>(this.noneMatch);
        if (tag == null) {
            tag = Tag.ALL;
        }
        if (Tag.ALL.equals(tag)) {
            noneMatch.clear();
        }
        if (!noneMatch.contains(Tag.ALL)) {
            if (!noneMatch.contains(tag)) {
                noneMatch.add(tag);
            }
        }
        else {
            throw new IllegalArgumentException("Tag ALL already in the list");
        }
        return new Conditionals(empty(), Collections.unmodifiableList(noneMatch), modifiedSince, Optional.empty());
    }

    /**
     * You should use the server's time here. Otherwise you might get unexpected results.
     *
     * The typical use case is:
     *
     *
     * <pre>
     *   HTTPResponse response = ....
     *   HTTPRequest request = createRequest();
     *   request = request.conditionals(new Conditionals().ifModifiedSince(response.getLastModified());
     * </pre>
     *
     * @param time the time to check.
     * @return the conditionals with the If-Modified-Since date set.
     */
    public Conditionals ifModifiedSince(LocalDateTime time) {
        Preconditions.checkArgument(match.isEmpty(), String.format(ERROR_MESSAGE, HeaderConstants.IF_MODIFIED_SINCE, HeaderConstants.IF_MATCH));
        Preconditions.checkArgument(!unModifiedSince.isPresent(), String.format(ERROR_MESSAGE, HeaderConstants.IF_MODIFIED_SINCE, HeaderConstants.IF_UNMODIFIED_SINCE));
        time = time.withNano(0);
        return new Conditionals(empty(), noneMatch, Optional.of(time), Optional.empty());
    }

    /**
     * You should use the server's time here. Otherwise you might get unexpected results.
     * The typical use case is:
     *
     *
     * <pre>
     *   HTTPResponse response = ....
     *   HTTPRequest request = createRequest();
     *   request = request.conditionals(new Conditionals().ifUnModifiedSince(response.getLastModified());
     * </pre>
     *
     * @param time the time to check.
     * @return the conditionals with the If-Unmodified-Since date set.
     */
    public Conditionals ifUnModifiedSince(LocalDateTime time) {
        Preconditions.checkArgument(noneMatch.isEmpty(), String.format(ERROR_MESSAGE, HeaderConstants.IF_UNMODIFIED_SINCE, HeaderConstants.IF_NONE_MATCH));
        Preconditions.checkArgument(!modifiedSince.isPresent(), String.format(ERROR_MESSAGE, HeaderConstants.IF_UNMODIFIED_SINCE, HeaderConstants.IF_MODIFIED_SINCE));
        time = time.withNano(0);
        return new Conditionals(match, empty(), Optional.empty(), Optional.of(time));
    }

    public List<Tag> getMatch() {
        return Collections.unmodifiableList(match);
    }

    public List<Tag> getNoneMatch() {
        return Collections.unmodifiableList(noneMatch);
    }

    public Optional<LocalDateTime> getModifiedSince() {
        return modifiedSince;
    }

    public Optional<LocalDateTime> getUnModifiedSince() {
        return unModifiedSince;
    }

    /**
     * 
     * @return {@code true} if the Conditionals represents a unconditional request. 
     */
    public boolean isUnconditional() {
      return noneMatch.contains(Tag.ALL) || match.contains(Tag.ALL) || (match.isEmpty() && !unModifiedSince.isPresent()) || (noneMatch.isEmpty() && !modifiedSince.isPresent()) ;
    }

    /**
     * Converts the Conditionals into real headers.
     * @return real headers.
     */
    public Headers toHeaders() {
        Headers headers = new Headers();
        if (!getMatch().isEmpty()) {
            headers = headers.add(new Header(HeaderConstants.IF_MATCH, buildTagHeaderValue(getMatch())));
        }
        if (!getNoneMatch().isEmpty()) {
            headers = headers.add(new Header(HeaderConstants.IF_NONE_MATCH, buildTagHeaderValue(getNoneMatch())));
        }
        if (modifiedSince.isPresent()) {
            headers = headers.set(HeaderUtils.toHttpDate(HeaderConstants.IF_MODIFIED_SINCE, modifiedSince.get()));
        }
        if (unModifiedSince.isPresent()) {
            headers = headers.set(HeaderUtils.toHttpDate(HeaderConstants.IF_UNMODIFIED_SINCE, unModifiedSince.get()));
        }

        return headers;
    }

    public static Conditionals valueOf(Headers headers) {
        List<Tag> ifMatch = makeTags(headers.getFirstHeaderValue(HeaderConstants.IF_MATCH).orElse(null));
        List<Tag> ifNoneMatch = makeTags(headers.getFirstHeaderValue(HeaderConstants.IF_NONE_MATCH).orElse(null));
        Optional<LocalDateTime> modifiedSince = headers.getFirstHeader(HeaderConstants.IF_MODIFIED_SINCE).flatMap(HeaderUtils::fromHttpDate);
        Optional<LocalDateTime> unModifiedSince = headers.getFirstHeader(HeaderConstants.IF_UNMODIFIED_SINCE).flatMap(HeaderUtils::fromHttpDate);
        return new Conditionals(ifMatch, ifNoneMatch, modifiedSince, unModifiedSince);
    }

    private static List<Tag> makeTags(String ifMatch) {
        if (ifMatch == null) {
            return Arrays.asList();
        }
        return Collections.unmodifiableList(Arrays.asList(ifMatch.split(",")).stream().
                filter(m -> !Objects.toString(m, "").isEmpty()).
                map(String::trim).
                flatMap(t -> OptionalUtils.stream(Tag.parse(t))).collect(Collectors.toList()));
    }

    private String buildTagHeaderValue(List<Tag> match) {
        return match.stream().map(Tag::format).collect(Collectors.joining(","));
    }
}
