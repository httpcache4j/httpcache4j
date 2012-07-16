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

import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import org.codehaus.httpcache4j.*;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
//TODO: handle auth-int
public class RequestDigest {

    private static final String CNONCE_COUNT = "00000001";
    private final Map<String, Directive> directives = new LinkedHashMap<String, Directive>();
    private final UsernamePasswordChallenge challenge;
    private final HTTPMethod method;
    private final URI requestURI;
    private final Digest serverDigest;
    private final Algorithm algorithm;

    RequestDigest(UsernamePasswordChallenge challenge, HTTPMethod method, URI requestURI, Digest serverDigest) {
        this.challenge = challenge;
        this.method = method;
        this.requestURI = requestURI;
        this.serverDigest = serverDigest;

        addDirective("username", challenge.getIdentifier(), true);
        addDirective("realm", serverDigest.getScheme().getRealm(), true);
        addDirective("nonce", serverDigest.getNonce(), true);
        addDirective("uri", requestURI.toString(), true);
        if (serverDigest.getAlgorithm() != null) {
            addDirective("algorithm", serverDigest.getAlgorithm().getValue(), true);
            algorithm = serverDigest.getAlgorithm();
        }
        else {
            algorithm = Algorithm.MD5;
        }
        if (!Strings.isNullOrEmpty(serverDigest.getQop())) {
            addDirective("qop", serverDigest.getQop(), false);
            addDirective("nc", CNONCE_COUNT, false);
            addDirective("cnonce", calculateCNonce(), true);
        }
        addDirective("response", calculateResponse(), true);
        if (!Strings.isNullOrEmpty(serverDigest.getOpaque())) {
            addDirective("opaque", serverDigest.getOpaque(), true);
        }
    }

    String calculateResponse() {
        String ha1 = calculateHashA1();
        Directive qopDirective = directives.get("qop");
        Directive nonce = directives.get("nonce");

        String response;
        String ha2 = calculateHashA2();

        if (qopDirective != null) {
            if (isAuthIntQualityOfProtection(qopDirective)) {
                throw new IllegalArgumentException("Auth-int not supported yet");
            }
            else if (isAuthQualityOfProtection(qopDirective)) {
                Directive clientNonce = directives.get("cnonce");
                StringBuilder builder = new StringBuilder();
                builder.append(ha1);
                builder.append(':');
                builder.append(nonce.getValue());
                builder.append(':');
                builder.append(CNONCE_COUNT);
                builder.append(':');
                builder.append(clientNonce.getValue());
                builder.append(':');
                builder.append(qopDirective.getValue());
                builder.append(':');
                builder.append(ha2);
                response = hash(algorithm, builder.toString(), "ISO-8859-1");
            }
            else {
                throw new IllegalArgumentException("Unknown QOP: " + qopDirective.getValue());
            }
        }
        else {
            response = hash(algorithm, String.format("%s:%s:%s", ha1, nonce.getValue(), ha2), "US-ASCII");
        }
        return response;
    }

    String calculateHashA2() {
        return hash(algorithm, String.format("%s:%s", method, requestURI), "ISO-8859-1");
    }

    String calculateHashA1() {
        StringBuilder a1 = new StringBuilder();
        a1.append(challenge.getIdentifier());
        a1.append(':');
        a1.append(serverDigest.getScheme().getRealm());
        a1.append(':');
        a1.append(challenge.getPassword());
        return hash(algorithm, a1.toString(), "US-ASCII");
    }

    private boolean isAuthQualityOfProtection(Directive directive) {
        return "auth".equals(directive.getValue());
    }

    private boolean isAuthIntQualityOfProtection(Directive directive) {
        return "auth-int".equals(directive.getValue());
    }

    private String hash(Algorithm algorithm, String input, final String charset) {
        switch (algorithm) {
            case MD5:
            case MD5_SESSION:
                try {
                    return Hashing.md5().hashBytes(input.getBytes(charset)).toString();
                } catch (UnsupportedEncodingException e) {
                    throw new Error(e);
                }
            case TOKEN:
            default:
                throw new IllegalArgumentException("No such algorithm");
        }
    }

    String calculateCNonce() {
        return hash(Algorithm.MD5, Long.toString(System.currentTimeMillis()), "US-ASCII");
    }

    public String toHeaderValue() {
        StringBuilder builder = new StringBuilder();
        builder.append("Digest ");
        for (Directive directive : directives.values()) {
            if (builder.length() > "Digest ".length()) {
                builder.append(", ");
            }
            builder.append(directive);
        }
        return builder.toString();
    }

    public static RequestDigest newInstance(UsernamePasswordChallenge challenge, HTTPRequest request, Digest digest) {
        return new RequestDigest(challenge, request.getMethod(), URI.create(request.getRequestURI().getRawPath()), digest);
    }

    private void addDirective(String name, String value, boolean quoted) {
        if (quoted) {
            directives.put(name, new QuotedDirective(name, value));
        }
        else {
            directives.put(name, new Directive(name, value));
        }
    }
}
