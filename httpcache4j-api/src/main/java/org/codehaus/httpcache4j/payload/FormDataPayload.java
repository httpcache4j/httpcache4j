/*
 * Copyright (c) 2009. The Codehaus. All Rights Reserved.
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

package org.codehaus.httpcache4j.payload;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.Parameter;
import org.codehaus.httpcache4j.util.URIEncoder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class FormDataPayload implements Payload {
    private final MIMEType mimeType = new MIMEType("application/x-www-form-urlencoded");
    private final String value;

    public FormDataPayload(Map<String, List<String>> parameters) {
        this(toIterable(parameters));
    }

    private static Iterable<FormParameter> toIterable(Map<String, List<String>> parameters) {
        Preconditions.checkNotNull(parameters, "Parameters map may not be null");
        List<FormParameter> params = new ArrayList<FormParameter>();
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            params.addAll(convert(entry.getKey(), entry.getValue()));
        }
        return params;
    }

    private static List<FormParameter> convert(final String key, List<String> values) {
        return Lists.transform(values, new Function<String, FormParameter>() {
            public FormParameter apply(String from) {
                return new FormParameter(key, from);
            }
        });
    }

    public FormDataPayload(Iterable<FormParameter> parameters) {
        value = Joiner.on("&").skipNulls().join(parameters);
    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(value.getBytes(Charsets.UTF_8));
    }

    @Deprecated
    public String getValues() {
        return value;
    }

    public String getValue() {
        return value;
    }

    public boolean isAvailable() {
        return true;
    }

    public long length() {
        return value.length();
    }

    /**
     * Represents a Form Url-encoded data parameter.
     * http://www.w3.org/TR/html401/interact/forms.html#h-17.13.3.4
     *
     * new line character MUST be {@code \r\n}.
     */
    public static class FormParameter extends Parameter {
        private static final long serialVersionUID = -174492565886663398L;

        public FormParameter(String key, String value) {
            super(key, value);
        }

        private String encode(String value) {
            return URIEncoder.encodeUTF8(value);
        }
        
        @Override
        public String toString() {
            return encode(name) + "=" + encode(value);
        }
    }
}
