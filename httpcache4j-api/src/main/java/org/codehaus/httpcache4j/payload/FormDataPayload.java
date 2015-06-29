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
import org.codehaus.httpcache4j.uri.QueryParam;
import org.codehaus.httpcache4j.uri.QueryParams;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public final class FormDataPayload implements Payload {
    private final MIMEType mimeType = new MIMEType("application/x-www-form-urlencoded");
    private final String value;

    public FormDataPayload(Map<String, List<String>> parameters) {
        this(new QueryParams(parameters).toQuery(false));
    }

    public FormDataPayload(List<QueryParam> parameters) {
        this(new QueryParams(parameters).toQuery(false));
    }

    public FormDataPayload(String formatted) {
        this.value = Objects.requireNonNull(formatted, "form data may not be null");
    }

    public MIMEType getMimeType() {
        return mimeType;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
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
}
