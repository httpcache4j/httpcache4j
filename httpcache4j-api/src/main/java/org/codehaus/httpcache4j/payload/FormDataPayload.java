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

import org.codehaus.httpcache4j.MIMEType;
import org.codehaus.httpcache4j.Parameter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.net.URLEncoder;

import com.google.common.base.Joiner;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class FormDataPayload implements Payload {
    private final MIMEType mimeType = new MIMEType("application/x-www-form-urlencoded");
    private final String values;

    public FormDataPayload(Map<String, List<String>> parameters) {
        this(toIterable(parameters));
    }

    private static Iterable<FormParameter> toIterable(Map<String, List<String>> parameters) {
        Validate.notNull(parameters, "Parameters map may not be null");
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
        values = Joiner.on("&").skipNulls().join(parameters);
    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        return IOUtils.toInputStream(values);
    }

    public String getValues() {
        return values;
    }

    public boolean isAvailable() {
        return true;
    }

    public static class FormParameter extends Parameter {
        private static final long serialVersionUID = -174492565886663398L;

        public FormParameter(String key, String from) {
            super(key, from);
        }

        private String encode(String value) {
            try {
                return URLEncoder.encode(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new Error(e);
            }
        }
        
        @Override
        public String toString() {
            return encode(name) + "=" + encode(value);
        }
    }
}