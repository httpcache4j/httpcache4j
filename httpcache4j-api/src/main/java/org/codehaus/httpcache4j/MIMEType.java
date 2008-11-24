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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.io.Serializable;

/**
 * Media type used in representations and preferences.
 *
 * @see <a href="http://en.wikipedia.org/wiki/MIME">MIME types on Wikipedia</a>
 */
public final class MIMEType implements Serializable {
    public static final MIMEType ALL = new MIMEType("*", "*");
    public static final MIMEType APPLICATION_OCTET_STREAM = new MIMEType("application", "octet-stream");

    private final MimeType mimeType;
    private final List<Parameter> parameters = new ArrayList<Parameter>();
    private static final long serialVersionUID = 1937511950666426391L;

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
        if (!parameters.contains(parameter)) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MIMEType other = (MIMEType) o;
        if (!new EqualsBuilder().append(getPrimaryType(), other.getPrimaryType()).append(getSubType(), other.getSubType()).isEquals()) {
            return false;
        }
        if (parameters != null ? !parameters.equals(other.parameters) : other.parameters != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(0, 31).append(getPrimaryType()).append(getSubType()).toHashCode();
    }

    public boolean includes(MIMEType mimeType) {
        boolean includes = mimeType == null || equals(ALL) || equals(mimeType);
        if (!includes) {
            includes = getPrimaryType().equals(mimeType.getPrimaryType())
                    && (getSubType().equals(mimeType.getSubType()) || getSubType()
                    .equals("*"));
        }
        return includes;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return mimeType.toString();
    }
    
    public static MIMEType valueOf(final String MIMEType) {
        return new MIMEType(MIMEType);
    }
    public static MIMEType valueOf(final String primaryType, final String subType) {
        return new MIMEType(primaryType, subType);
    }
}