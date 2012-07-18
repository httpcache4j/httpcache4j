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

package org.codehaus.httpcache4j.auth.digest;

import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.auth.AuthScheme;
import org.codehaus.httpcache4j.util.AuthDirectivesParser;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class RequestDigestTest {

    @Test
    public void testCalculateHashA1() {
        AuthScheme scheme = new AuthScheme(AuthDirectivesParser.parse("Digest realm=\"realm1\", nonce=\"f2a3f18799759d4f1a1c068b92b573cb\"").iterator().next());
        Digest digest = new Digest(new HTTPHost("http", "localhost", -1), scheme);
        assertEquals("f2a3f18799759d4f1a1c068b92b573cb", digest.getNonce());
        RequestDigest requestDigest = new RequestDigest(
                new UsernamePasswordChallenge("username", "password"),
                HTTPMethod.GET,
                URI.create("/"),
                digest
        );
        assertEquals("37ddcec9c022371a3dd7d500dfc81297", requestDigest.calculateHashA1());
    }

    @Test
    public void testCalculateHashA2() {
        AuthScheme scheme = new AuthScheme(AuthDirectivesParser.parse("Digest realm=\"realm1\", nonce=\"f2a3f18799759d4f1a1c068b92b573cb\"").iterator().next());
        Digest digest = new Digest(new HTTPHost("http", "localhost", -1), scheme);
        assertEquals("f2a3f18799759d4f1a1c068b92b573cb", digest.getNonce());
        RequestDigest requestDigest = new RequestDigest(
                new UsernamePasswordChallenge("username", "password"),
                HTTPMethod.GET,
                URI.create("/"),
                digest
        );
        assertEquals("71998c64aea37ae77020c49c00f73fa8", requestDigest.calculateHashA2());
    }


    @Test
    public void testDigestAuthenticationFromRFC() {
        AuthScheme scheme = new AuthScheme(AuthDirectivesParser.parse("Digest realm=\"testrealm@host.com\", qop=\"auth\", nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\", opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"").iterator().next());
        Digest digest = new Digest(new HTTPHost("http", "localhost", -1), scheme);
        assertEquals("dcd98b7102dd2f0e8b11d0f600bfb0c093", digest.getNonce());
        assertEquals("5ccc069c403ebaf9f0171e9517f40e41", digest.getOpaque());
        RequestDigest requestDigest = new RequestDigest(
                new UsernamePasswordChallenge("Mufasa", "Circle Of Life"),
                HTTPMethod.GET,
                URI.create("/dir/index.html"),
                digest
        ) {
            @Override
            String calculateCNonce() {
                return "0a4f113b";
            }
        };
        assertEquals("939e7578ed9e3c518a452acee763bce9", requestDigest.calculateHashA1());
        assertEquals("39aff3a2bab6126f332b942af96d3366", requestDigest.calculateHashA2());
        assertEquals("0a4f113b", requestDigest.calculateCNonce());
        assertEquals("6629fae49393a05397450978507c4ef1", requestDigest.calculateResponse());
        assertEquals("Digest username=\"Mufasa\"," +
                " realm=\"testrealm@host.com\"," +
                " nonce=\"dcd98b7102dd2f0e8b11d0f600bfb0c093\"," +
                " uri=\"/dir/index.html\"," +
                " qop=auth," +
                " nc=00000001," +
                " cnonce=\"0a4f113b\"," +
                " response=\"6629fae49393a05397450978507c4ef1\"," +
                " opaque=\"5ccc069c403ebaf9f0171e9517f40e41\"",  requestDigest.toHeaderValue());


        /**
         * Authorization: Digest username="Mufasa", realm="testrealm@host.com", nonce="dcd98b7102dd2f0e8b11d0f600bfb0c093", uri="/dir/index.html", qop=auth, nc=00000001, cnonce="0a4f113b", response="6629fae49393a05397450978507c4ef1", opaque="5ccc069c403ebaf9f0171e9517f40e41"
         */
    }
    
    @Test
    public void testDigestAuthenticationFromLocalApache() {
        AuthScheme scheme = new AuthScheme(AuthDirectivesParser.parse("Digest realm=\"The Shit\", nonce=\"74x09bV+BAA=00c61ef4d62ad0b8616a45d0714b47a39f833e91\", algorithm=MD5, qop=\"auth\"").iterator().next());
        Digest digest = new Digest(new HTTPHost("http", "localhost", -1), scheme);
        assertEquals("74x09bV+BAA=00c61ef4d62ad0b8616a45d0714b47a39f833e91", digest.getNonce());
        RequestDigest requestDigest = new RequestDigest(
                new UsernamePasswordChallenge("username", "password"),
                HTTPMethod.GET,
                URI.create("/private/"),
                digest
        ) {
          @Override
          String calculateCNonce() {
            return "MDA4MDQ1";
          }
        };
        assertEquals("cca57e64ed1bbf0056100e2662326d85", requestDigest.calculateResponse());
    }
}
