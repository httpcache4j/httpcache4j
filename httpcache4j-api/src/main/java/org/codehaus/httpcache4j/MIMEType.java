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

import javax.activation.MimeType;
import javax.activation.MimeTypeParameterList;
import javax.activation.MimeTypeParseException;
import java.util.*;

/**
 * Media type used in representations and preferences.
 *
 * @see <a href="http://en.wikipedia.org/wiki/MIME">MIME types on Wikipedia</a>
 */
public final class MIMEType {
    public static final MIMEType ALL = new MIMEType("*", "*");
    public static final MIMEType APPLICATION_OCTET_STREAM = new MIMEType("application", "octet-stream");

    private final MimeType mimeType;

    public MIMEType(String MIMEType) {
        MimeType mimeType;
        try {
            mimeType = new MimeType(MIMEType);
        } catch (MimeTypeParseException e) {
            throw new IllegalArgumentException(e);
        }
        this.mimeType = mimeType;
    }

    public MIMEType(String primaryType, String subType) {
        MimeType mimeType;
        try {
            mimeType = new MimeType(primaryType, subType);
        } catch (MimeTypeParseException e) {
            throw new IllegalArgumentException(e);
        }
        this.mimeType = mimeType;
    }

    /**
     * Adds a parameter to the MIMEType.
     *
     * @param name name of parameter
     * @param value value of parameter
     * @return returns a new instance with the parameter set
     */
    public MIMEType addParameter(String name, String value) {
        MIMEType mt = new MIMEType(toString());
        mt.mimeType.setParameter(name, value);
        return mt;
    }

    public String getSubType() {
        return mimeType.getSubType();
    }

    public String getPrimaryType() {
        return mimeType.getPrimaryType();
    }

    @Override
    public int hashCode() {
        return 31 * (getPrimaryType().hashCode() + getSubType().hashCode());
    }

    @Override
    public boolean equals(final Object object) {
        return equals(object, true);
    }

    public boolean equalsWithoutParameters(final Object o) {
        return equals(o, false);
    }

    /**
     * This will be removed in the next release.
     * @deprecated Use {@link #equals(Object)} or {@link #equalsWithoutParameters(Object)} instead.
     */
    @Deprecated
    public boolean equals(final Object o, final boolean includeParameters) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MIMEType other = (MIMEType) o;
        if (!(getPrimaryType().equals(other.getPrimaryType()) && getSubType().equals(other.getSubType()))) {
            return false;
        }
        if (includeParameters && !parametersEquals(other)) {
            return false;
        }

        return true;
    }

    private boolean parametersEquals(MIMEType other) {
        Map<String, String> otherParameterList = convertParams(other.mimeType.getParameters());
        Map<String, String> parameterList = convertParams(mimeType.getParameters());
        return parameterList.equals(otherParameterList);
    }

    public boolean includes(MIMEType mimeType) {
        boolean includes = mimeType == null || equalsWithoutParameters(ALL) || equalsWithoutParameters(mimeType);
        if (!includes) {
            includes = getPrimaryType().equals(mimeType.getPrimaryType())
                    && (getSubType().equals(mimeType.getSubType()) || "*".equals(getSubType()));
        }
        return includes;
    }

    public List<Parameter> getParameters() {
        List<Parameter> list = new ArrayList<Parameter>();
        Map<String, String> map = convertParams(mimeType.getParameters());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            list.add(new Parameter(entry.getKey(), entry.getValue()));
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public String toString() {
        return mimeType.toString();
    }

    private Map<String, String> convertParams(MimeTypeParameterList list) {
        Map<String, String> map = new HashMap<String, String>();
        Enumeration names = list.getNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            map.put(name, list.get(name));
        }
        return map;
    }


    public static MIMEType valueOf(final String MIMEType) {
        return new MIMEType(MIMEType);
    }

    public static MIMEType valueOf(final String primaryType, final String subType) {
        return new MIMEType(primaryType, subType);
    }
}
