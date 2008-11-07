/*
 * Copyright 2005-2007 Noelios Consulting.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.codehaus.httpcache4j;

import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Media type used in representations and preferences.
 *
 * @see <a href="http://en.wikipedia.org/wiki/MIME">MIME types on Wikipedia</a>
 */
public final class MIMEType {
    public static final MIMEType ALL = new MIMEType("*", "*");
    public static final MIMEType APPLICATION_OCTET_STREAM = new MIMEType("application", "octet-stream");

    private final MimeType mimeType;
    private final List<Parameter> parameters = new ArrayList<Parameter>();

    public MIMEType(String MIMEType) {
        MimeType mimeType;
        try {
            mimeType = new MimeType(MIMEType);
        }
        catch (MimeTypeParseException e) {
            throw new IllegalArgumentException(e);
        }
        this.mimeType = mimeType;
        convertParamerters(mimeType);
    }

    public MIMEType(String primaryType, String subType) {
        MimeType mimeType;
        try {
            mimeType = new MimeType(primaryType, subType);
        }
        catch (MimeTypeParseException e) {
            throw new IllegalArgumentException(e);
        }
        this.mimeType = mimeType;
    }

    public void addParameter(String name, String value) {
        Parameter parameter = new Parameter(name, value);
        if (parameters.contains(parameter)) {
            mimeType.setParameter(name, value);
            parameters.add(parameter);
        }
    }

    private void convertParamerters(MimeType mimeType) {
        MimeTypeParameterList list = mimeType.getParameters();
        Enumeration names = list.getNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            parameters.add(new Parameter(name, list.get(name)));
        }
    }

    public String getSubType() {
        return mimeType.getSubType();
    }

    public String getPrimaryType() {
        return mimeType.getPrimaryType();
    }

    public boolean matches(String MIMEType) {
        try {
            return this.mimeType.match(MIMEType);
        }
        catch (MimeTypeParseException e) {
            throw new IllegalArgumentException("Argument is not a mime type", e);
        }
    }

    public boolean matches(MIMEType MIMEType) {
        return this.mimeType.match(MIMEType.mimeType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MIMEType other = (MIMEType) o;

        if (mimeType != null ? !mimeType.match(other.mimeType) : other.mimeType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = mimeType != null ? mimeType.hashCode() : 0;
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }

    public boolean includes(String mimeType) {
        throw new UnsupportedOperationException("Implement");
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return mimeType.toString();
    }
}