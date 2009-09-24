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

package org.codehaus.httpcache4j.cache;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assert;
import org.codehaus.httpcache4j.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class HTTPCacheHelperTest {
    private final HTTPCacheHelper helper = new HTTPCacheHelper();

    @Test
    public void testRemoveUnmodifiableHeadersLeavesLinkHeader() {
        Headers headers = new Headers().add("LINK", "<foo>");
        Headers washedHeaders = helper.removeUnmodifiableHeaders(headers);
        Assert.assertTrue("Link header was removed, it should remain", washedHeaders.keySet().contains("link"));
    }

    @Test @Ignore /* Test fails in 2.1 */
    public void testRemoveUnmodifiableHeadersRemovesConnectionHeader() {
        Headers headers = new Headers().add("CONNECTION", "close");
        Headers washedHeaders = helper.removeUnmodifiableHeaders(headers);
        Assert.assertFalse("Connection header was not removed, it should be.", washedHeaders.keySet().contains("connection"));
    }

    @Test
    public void testAgeCalculation() {
        Headers headers = new Headers().add(HeaderUtils.toHttpDate("Date", createDateTime(0)));
        DateTimeUtils.setCurrentMillisFixed(createDateTime(10).getMillis());
        HTTPResponse cachedResponse = createResponse(headers);
        HTTPResponse responseWithCalculatedAge = helper.calculateAge(cachedResponse, new CacheItem(cachedResponse, createDateTime(0)));
        Assert.assertEquals("10", responseWithCalculatedAge.getHeaders().getFirstHeaderValue("Age"));
    }
    
    @Test
    public void testCacheableResponses() {
        Headers headers = new Headers();
        headers = headers.add(HeaderConstants.CACHE_CONTROL, "private, max-age=39");
        headers = headers.add(HeaderConstants.ETAG, "\"123\"");
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, createDateTime(10)));
        assertCacheableHeaders(headers);

        headers = new Headers();
        headers = headers.add(HeaderConstants.CACHE_CONTROL, "private, max-age=39");
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, createDateTime(40)));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, createDateTime(0)));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.LAST_MODIFIED, createDateTime(0)));
        assertCacheableHeaders(headers);

        headers = new Headers();
        headers = headers.add(HeaderConstants.CACHE_CONTROL, "private, max-age=39");
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.LAST_MODIFIED, createDateTime(0)));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, createDateTime(0)));
        assertCacheableHeaders(headers);
    }

    private void assertCacheableHeaders(Headers headers) {
        Assert.assertTrue("Response was not cacheable", helper.isCacheableResponse(new HTTPResponse(null, Status.OK, headers)));
        Assert.assertTrue("Response was not cacheable", helper.isCacheableResponse(new HTTPResponse(null, Status.NON_AUTHORITATIVE_INFORMATION, headers)));
        Assert.assertTrue("Response was not cacheable", helper.isCacheableResponse(new HTTPResponse(null, Status.MULTIPLE_CHOICES, headers)));
        Assert.assertTrue("Response was not cacheable", helper.isCacheableResponse(new HTTPResponse(null, Status.MOVED_PERMANENTLY, headers)));
        Assert.assertTrue("Response was not cacheable", helper.isCacheableResponse(new HTTPResponse(null, Status.GONE, headers)));
    }

    @Test
    public void testNotCacheableResponses() {
        Headers headers = new Headers();
        headers = headers.add(HeaderUtils.toHttpDate("date", createDateTime(10)));
        headers = headers.add(HeaderConstants.CACHE_CONTROL, "private, max-age=39");
        Assert.assertFalse("Response was cacheable", helper.isCacheableResponse(new HTTPResponse(null, Status.OK, headers)));
        Assert.assertFalse("Response was cacheable", helper.isCacheableResponse(new HTTPResponse(null, Status.NON_AUTHORITATIVE_INFORMATION, headers)));
        Assert.assertFalse("Response was cacheable", helper.isCacheableResponse(new HTTPResponse(null, Status.MULTIPLE_CHOICES, headers)));
        Assert.assertFalse("Response was cacheable", helper.isCacheableResponse(new HTTPResponse(null, Status.MOVED_PERMANENTLY, headers)));
        Assert.assertFalse("Response was cacheable", helper.isCacheableResponse(new HTTPResponse(null, Status.GONE, headers)));
        Assert.assertFalse("Response was cacheable", helper.isCacheableResponse(new HTTPResponse(null, Status.NOT_MODIFIED, headers)));
        Assert.assertFalse("Response was cacheable", helper.isCacheableResponse(new HTTPResponse(null, Status.ACCEPTED, headers)));
        Assert.assertFalse("Response was cacheable", helper.isCacheableResponse(new HTTPResponse(null, Status.CREATED, headers)));
    }

    private DateTime createDateTime(int seconds) {
        return new DateTime(2009, 4, 22, 10, 10, seconds, 0);
    }

    private HTTPResponse createResponse(Headers headers) {
        return new HTTPResponse(null, Status.OK, headers);
    }
}
