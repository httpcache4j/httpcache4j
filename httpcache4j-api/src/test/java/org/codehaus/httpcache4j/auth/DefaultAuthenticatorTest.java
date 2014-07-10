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

package org.codehaus.httpcache4j.auth;

import org.codehaus.httpcache4j.*;
import org.junit.Test;
import org.junit.Assert;

import java.net.URI;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DefaultAuthenticatorTest {

    private HTTPRequest defaultRequest = new HTTPRequest(URI.create("http://foo"));   


    @Test
    public void testNoAuthExpectTheSameRequest() {
        final HTTPRequest req = new DefaultAuthenticator().prepareAuthentication(defaultRequest, new HTTPResponse(null, Status.CREATED, new Headers()));
        Assert.assertSame(req, defaultRequest);
    }

    @Test
    public void testAuthBasic() {
        final HTTPRequest request = defaultRequest.challenge(new UsernamePasswordChallenge("foo", "bar"));
        final HTTPRequest req = new DefaultAuthenticator().prepareAuthentication(request,
                                                                                 new HTTPResponse(null, Status.UNAUTHORIZED,
                                                                                                  new Headers().add(HeaderConstants.WWW_AUTHENTICATE, "Basic realm=\"foo\"")));
        Assert.assertNotSame(req, defaultRequest);
        Assert.assertTrue("No auth header", req.getHeaders().contains("Authorization"));
    }

    @Test
    public void testAuthAlmostBasic() {
        final HTTPRequest request = defaultRequest.challenge(new UsernamePasswordChallenge("foo", "bar"));
        final HTTPRequest req = new DefaultAuthenticator().prepareAuthentication(request,
                                                                                 new HTTPResponse(null, Status.UNAUTHORIZED,
                                                                                                  new Headers().add(HeaderConstants.WWW_AUTHENTICATE, "Basicssss raealm=\"foo\"")));
        Assert.assertSame(req, request);
        Assert.assertFalse("Auth header was present", req.getHeaders().contains("Authorization"));
    }
}
