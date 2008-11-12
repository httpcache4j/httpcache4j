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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Validation tag equivalent to the HTTP entity tag. "A strong entity tag may be
 * shared by two entities of a resource only if they are equivalent by octet
 * equality.<br/> A weak entity tag may be shared by two entities of a resource
 * only if the entities are equivalent and could be substituted for each other
 * with no significant change in semantics."
 *
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP
 *      Entity Tags</a>
 * @see <a
 *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.3.2">HTTP
 *      Entity Tag Cache Validators</a>
 */
public final class Tag extends Metadata {
    /** Tag matching any other tag, used in call's condition data. */
    public static final Tag ALL = Tag.parse("*");

    /** The tag weakness. */
    private boolean weak;

    /**
     * Parses a tag formatted as defined by the HTTP standard.
     *
     * @param httpTag The HTTP tag string; if it starts with 'W/' the tag will be
     *                marked as weak and the data following the 'W/' used as the tag;
     *                otherwise it should be surrounded with quotes (e.g.,
     *                "sometag").
     *
     * @return A new tag instance.
     *
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP
     *      Entity Tags</a>
     */
    public static Tag parse(final String httpTag) {
        Tag result = null;
        boolean weak = false;
        String httpTagCopy = httpTag;

        if (httpTagCopy.startsWith("W/")) {
            weak = true;
            httpTagCopy = httpTagCopy.substring(2);
        }

        if (httpTagCopy.startsWith("\"") && httpTagCopy.endsWith("\"")) {
            result = new Tag(
                    httpTagCopy.substring(1, httpTagCopy.length() - 1), weak);
        }
        else if (httpTagCopy.equals("*")) {
            result = new Tag("*", weak);
        }
        else {
            Logger.getLogger(Tag.class.getCanonicalName()).log(Level.WARNING,
                    "Invalid tag format detected: " + httpTagCopy);
        }

        return result;
    }

    /**
     * Constructor.
     *
     * @param opaqueTag The tag value.
     * @param weak      The weakness indicator.
     */
    private Tag(final String opaqueTag, boolean weak) {
        super(opaqueTag);
        this.weak = weak;
    }

    /**
     * Indicates if both tags are equal.
     *
     * @param object The object to compare to.
     *
     * @return True if both tags are equal.
     */
    @Override
    public boolean equals(final Object object) {
        return equals(object, true);
    }

    /**
     * Indicates if both tags are equal.
     *
     * @param object        The object to compare to.
     * @param checkWeakness the equality test takes care or not of the weakness.
     *
     * @return True if both tags are equal.
     */
    public boolean equals(final Object object, boolean checkWeakness) {
        boolean result = (object != null) && (object instanceof Tag);

        if (result) {
            Tag that = (Tag) object;

            if (checkWeakness) {
                result = (that.isWeak() == isWeak());
            }

            if (result) {
                if (getName() == null) {
                    result = (that.getName() == null);
                }
                else {
                    result = getName().equals(that.getName());
                }
            }
        }

        return result;
    }

    /**
     * Returns tag formatted as an HTTP tag string.
     *
     * @return The formatted HTTP tag string.
     *
     * @see <a
     *      href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP
     *      Entity Tags</a>
     */
    public String format() {
        if (getName().equals("*")) {
            return "*";
        }
        else {
            StringBuilder sb = new StringBuilder();
            if (isWeak()) {
                sb.append("W/");
            }
            return sb.append('"').append(getName()).append('"').toString();
        }
    }

    /**
     * Returns the description.
     *
     * @return The description.
     */
    public String getDescription() {
        return "HTTP entity tag";
    }

    /**
     * Returns the name, corresponding to an HTTP opaque tag value.
     *
     * @return The name, corresponding to an HTTP opaque tag value.
     */
    public String getName() {
        return super.getName();
    }

    @Override
    public String toString() {
        return "opaque tag: " + getName() + " is weak: " + isWeak();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return format().hashCode();
    }

    /**
     * Indicates if the tag is weak.
     *
     * @return True if the tag is weak, false if the tag is strong.
     */
    public boolean isWeak() {
        return this.weak;
    }
}