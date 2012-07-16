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
import org.codehaus.httpcache4j.payload.FilePayload;
import org.codehaus.httpcache4j.util.NumberUtils;
import org.joda.time.DateTime;

import java.io.*;
import java.util.Properties;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class SerializableCacheItem implements Serializable, CacheItem {
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

    public Properties toProperties() {
        Properties object = new Properties();
        object.setProperty("cache-time", HeaderUtils.toHttpDate("cache-time", item.getCachedTime()).getValue());
        HTTPResponse response = item.getResponse();
        object.setProperty("status", String.valueOf(response.getStatus().getCode()));
        if (response.hasPayload()) {
            FilePayload payload = (FilePayload) response.getPayload();
            object.setProperty("file", payload.getFile().getAbsolutePath());
        }
        object.setProperty("headers", response.getHeaders().toString());
        return object;
    }

    public static CacheItem parse(Properties object) {
        DateTime time = HeaderUtils.fromHttpDate(new Header("cache-time", object.getProperty("cache-time")));
        Status status = Status.valueOf(NumberUtils.toInt(object.getProperty("status"), 200));
        Headers headers = Headers.parse(object.getProperty("headers"));
        FilePayload p = null;
        if (object.containsKey("file")) {
            p = new FilePayload(new File(object.getProperty("file")), MIMEType.valueOf(headers.getFirstHeaderValue("Content-Type")));
        }
        return new DefaultCacheItem(new HTTPResponse(p, status, headers), time);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(toProperties());
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        Properties jsonValue = (Properties) in.readObject();
        item = parse(jsonValue);
    }
}
