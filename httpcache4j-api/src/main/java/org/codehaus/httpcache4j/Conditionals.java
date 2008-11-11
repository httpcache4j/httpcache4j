package org.codehaus.httpcache4j;

import org.apache.commons.lang.Validate;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a> */
public final class Conditionals {
    private List<Tag> match;
    private List<Tag> nonMatch;
    private DateTime modifiedSince;
    private DateTime unModifiedSince;
    private static final String ERROR_MESSAGE = "The combination of %s and %s is undefined by the HTTP specification";

    public void addIfMatch(Tag tag) {
        Validate.isTrue(modifiedSince == null, String.format(ERROR_MESSAGE, HeaderConstants.IF_MATCH, HeaderConstants.IF_MODIFIED_SINCE));
        Validate.isTrue(nonMatch == null || nonMatch.isEmpty(), String.format(ERROR_MESSAGE, HeaderConstants.IF_MATCH, HeaderConstants.IF_NON_MATCH));
        if (tag != null) {
            if (match == null || Tag.ALL.equals(tag)) {
                match = new ArrayList<Tag>();
            }
            if (!match.contains(Tag.ALL)) {
                match.add(tag);
            }
            else {
                throw new IllegalArgumentException("Tag ALL already in the list");
            }
        }
    }

    public void addIfNoneMatch(Tag tag) {
        Validate.isTrue(unModifiedSince == null, String.format(ERROR_MESSAGE, HeaderConstants.IF_NON_MATCH, HeaderConstants.IF_UNMODIFIED_SINCE));
        Validate.isTrue(match == null || match.isEmpty(), String.format(ERROR_MESSAGE, HeaderConstants.IF_NON_MATCH, HeaderConstants.IF_MATCH));
        if (tag != null) {
            if (nonMatch == null || Tag.ALL.equals(tag)) {
                nonMatch = new ArrayList<Tag>();
            }
            if (!nonMatch.contains(Tag.ALL)) {
                nonMatch.add(tag);
            }
            else {
                throw new IllegalArgumentException("Tag ALL already in the list");
            }
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
        return match == null ? Collections.<Tag>emptyList() : Collections.unmodifiableList(match);
    }

    public List<Tag> getNonMatch() {
        return nonMatch == null ? Collections.<Tag>emptyList() : Collections.unmodifiableList(nonMatch);
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