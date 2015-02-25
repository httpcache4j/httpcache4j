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

package org.codehaus.httpcache4j.mutable;

import net.hamnaberg.funclite.Preconditions;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.preference.Preference;
import org.joda.time.DateTime;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class MutableHeaders implements Iterable<Header> {
    private Headers headers;

    public MutableHeaders() {
        this(new Headers());
    }

    MutableHeaders(Headers headers) {
        this.headers = Preconditions.checkNotNull(headers, "Headers may not be null");
    }

    public void add(Header header) {
        headers = headers.add(header);
    }

    public void add(String key, String value) {
        headers = headers.add(key, value);
    }

    public void add(String key, Iterable<String> values) {
        headers = headers.add(key, values);
    }

    public void add(Iterable<Header> headers) {
        this.headers = this.headers.add(headers);
    }

    public void set(Header header) {
        headers = headers.set(header);
    }

    public void set(String name, String value) {
        headers = headers.set(name, value);
    }

    public void set(Iterable<Header> headers) {
        this.headers = this.headers.set(headers);
    }

    public List<Header> getHeaders(String headerKey) {
        return headers.getHeaders(headerKey);
    }

    public Header getFirstHeader(String headerKey) {
        return headers.getFirstHeader(headerKey);
    }

    public String getFirstHeaderValue(String headerKey) {
        return headers.getFirstHeaderValue(headerKey);
    }

    public void addAcceptLanguage(Preference... accept) {
        headers = headers.addAcceptLanguage(accept);
    }

    public List<Preference> getAcceptLanguage() {
        return headers.getAcceptLanguage();
    }

    public void setAcceptCharset(List<Preference> charsets) {
        headers = headers.withAcceptCharset(charsets);
    }

    public List<Preference> getAcceptCharset() {
        return headers.getAcceptCharset();
    }

    public void setExpires(DateTime expires) {
        headers = headers.withExpires(expires);
    }

    public void addAccept(Preference... accept) {
        headers = headers.addAccept(accept);
    }

    public DateTime getLastModified() {
        return headers.getLastModified();
    }

    public void addAcceptCharset(Preference... accept) {
        headers = headers.addAcceptCharset(accept);
    }

    public void setAccept(List<Preference> charsets) {
        headers = headers.withAccept(charsets);
    }

    public List<Preference> getAccept() {
        return headers.getAccept();
    }

    public void setAcceptLanguage(List<Preference> acceptLanguage) {
        headers = headers.withAcceptLanguage(acceptLanguage);
    }

    public void setLastModified(DateTime lm) {
        headers = headers.withLastModified(lm);
    }

    public DateTime getExpires() {
        return headers.getExpires();
    }

    public DateTime getDate() {
        return headers.getDate();
    }

    public void setDate(DateTime dt) {
        headers = headers.withDate(dt);
    }

    public Set<HTTPMethod> getAllow() {
        return headers.getAllow();
    }

    public void setAllow(Set<HTTPMethod> allow) {
        headers = headers.withAllow(allow);
    }

    public CacheControl getCacheControl() {
        return headers.getCacheControl();
    }

    public void setCacheControl(CacheControl cc) {
        headers = headers.withCacheControl(cc);
    }

    public Tag getETag() {
        return headers.getETag();
    }

    public void setETag(Tag tag) {
        headers = headers.withETag(tag);
    }

    public boolean contains(Header header) {
        return headers.contains(header);
    }

    public boolean contains(String headerName) {
        return headers.contains(headerName);
    }

    @Deprecated
    public boolean hasHeader(String headerName) {
        return headers.contains(headerName);
    }

    public int size() {
        return headers.size();
    }

    public boolean isEmpty() {
        return headers.isEmpty();
    }

    @Override
    public String toString() {
        return headers.toString();
    }

    public Headers toHeaders() {
        return headers;
    }

    public Set<String> keySet() {
        return headers.keySet();
    }

    public Iterator<Header> iterator() {
        return headers.iterator();
    }

}
