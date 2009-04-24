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

import org.junit.Test;
import org.junit.Assert;
import org.codehaus.httpcache4j.*;
import org.joda.time.DateTime;

import java.net.URI;

/**
 * @author <a href="mailto:erlend@hamnaberg.net">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class HTTPCacheHelperTest {
    private final HTTPCacheHelper helper = new HTTPCacheHelper();

    @Test
    public void testAgeCalculation() {
        Headers headers = new Headers();
        headers.add(HeaderUtils.toHttpDate("Date", createDateTime(10)));
        HTTPResponse cachedResponse = createResponse(headers);
        headers = new Headers();
        headers.add(HeaderUtils.toHttpDate("Date", createDateTime(20)));
        HTTPResponse resolvedResponse = createResponse(headers);
        Assert.assertEquals("10", helper.calculateAge(resolvedResponse, cachedResponse));
    }
    
    @Test
    public void testNoVariation() {
        HTTPRequest request = new HTTPRequest(URI.create("dummy://url"));
        HTTPResponse response = createResponse(new Headers());
        Vary vary = helper.determineVariation(response, request);
        Assert.assertEquals(0, vary.getVaryHeaderNames().size());
    }

    @Test
    public void testOneVariation() {
        HTTPRequest request = new HTTPRequest(URI.create("dummy://url"));
        Headers headers = new Headers();
        headers.add("Vary", "Accept");
        HTTPResponse response = createResponse(headers);
        Vary vary = helper.determineVariation(response, request);
        Assert.assertEquals(1, vary.getVaryHeaderNames().size());
    }

    @Test
    public void testTwoVariations() {
        HTTPRequest request = new HTTPRequest(URI.create("dummy://url"));
        Headers headers = new Headers();
        headers.add("Vary", "Accept, Accept-Language");
        HTTPResponse response = createResponse(headers);
        Vary vary = helper.determineVariation(response, request);
        Assert.assertEquals(2, vary.getVaryHeaderNames().size());
    }

    private DateTime createDateTime(int seconds) {
        return new DateTime(2009, 4, 22, 10, 10, seconds, 0);
    }

    private HTTPResponse createResponse(Headers headers) {
        return new HTTPResponse(null, Status.OK, headers);
    }
}
