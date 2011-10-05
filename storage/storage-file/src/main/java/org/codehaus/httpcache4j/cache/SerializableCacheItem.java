/*
 * Copyright (c) 2010. The Codehaus. All Rights Reserved.
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

package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.util.ToJSON;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class SerializableCacheItem implements Serializable, ToJSON, CacheItem {
    private static final long serialVersionUID = 7170431954380145524L;

    private transient CacheItem item;

    public SerializableCacheItem(CacheItem item) {
        this.item = item;
    }

    public int getTTL() {
        return item.getTTL();
    }

    public boolean isStale(HTTPRequest request) {
        return item.isStale(request);
    }

    public int getAge(HTTPRequest request) {
        return item.getAge(request);
    }

    public DateTime getCachedTime() {
        return item.getCachedTime();
    }

    public HTTPResponse getResponse() {
        return item.getResponse();
    }

    public String toJSON() {

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> object = new LinkedHashMap<String, Object>();
        object.put("cache-time", HeaderUtils.toHttpDate("cache-time", item.getCachedTime()).getValue());
        HTTPResponse response = item.getResponse();
        object.put("status", response.getStatus().getCode());
        if (response.hasPayload()) {
            CleanableFilePayload payload = (CleanableFilePayload) response.getPayload();
            Map<String, String> payloadItem = new LinkedHashMap<String, String>();
            payloadItem.put("file", payload.getFile().getAbsolutePath());
            payloadItem.put("mime-type", payload.getMimeType().toString());
            object.put("payload", payloadItem);
        } else {
            object.put("payload", null);
        }
        object.put("headers", response.getHeaders().toJSON());
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private CacheItem fromJSON(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(json);
            DateTime time = HeaderUtils.fromHttpDate(new Header("cache-time", node.path("cache-time").getValueAsText()));
            Status status = Status.valueOf(node.path("status").getIntValue());
            Headers headers = Headers.fromJSON(node.path("headers").getValueAsText());
            CleanableFilePayload p = null;
            if (node.path("payload") != null) {
                JsonNode payload = node.path("payload");
                if (!payload.isNull()) {
                    p = new CleanableFilePayload(new File(payload.path("file").getValueAsText()), MIMEType.valueOf(payload.path("mime-type").getValueAsText()));
                }
            }
            return new DefaultCacheItem(new HTTPResponse(p, status, headers), time);

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(toJSON());
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        String jsonValue = (String) in.readObject();
        item = fromJSON(jsonValue);
    }
}
