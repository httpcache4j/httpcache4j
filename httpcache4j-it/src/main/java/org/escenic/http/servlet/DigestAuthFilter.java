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

package org.escenic.http.servlet;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.httpcache4j.*;
import org.codehaus.httpcache4j.auth.AuthScheme;
import org.escenic.http.Representation;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:erlend@escenic.com">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class DigestAuthFilter extends AbstractEsiFilter {
    private static final String DIGEST = "Digest ";
    private static final String NONCE = "NONCE";
    private static final String NEXT_NONCE = "NEXT_NONCE";
    private static final String REALM = "TEST";

    @Override
    public void doFilterImpl(HttpServletRequest pRequest,
                             HttpServletResponse pResponse,
                             FilterChain pChain,
                             PathElement pPath,
                             Representation pRepresentation) throws IOException, ServletException {
        PasswordDigest digest = new PasswordDigest();

        Map<String, String> parameters = pPath.getParameters();
        String user = parameters.get("u");
        String pass = parameters.get("p");
        if (StringUtils.isBlank(user) && StringUtils.isBlank(pass)) {
            sendAuthorization(pResponse);
        }
        else {
            String authorization = pRequest.getHeader(HeaderConstants.AUTHORIZATION);
            if (StringUtils.isNotBlank(authorization)) {
                AuthScheme scheme = new AuthScheme(new Header(HeaderConstants.AUTHORIZATION, authorization));
                Directives directives = scheme.getDirectives();
                String nonce = directives.get("nonce");
                String response = directives.get("response");
                Algorithm algorithm = Algorithm.valueOf(directives.get("algorithm"));
                String username = directives.get("username");
                String realm = directives.get("realm");
                String method = pRequest.getMethod();
                String uri = directives.get("uri");
                if (uri == null) {
                    uri = pRequest.getRequestURI();
                }
                if (user.equals(username)) {
                    String ha1 = digest.digest(algorithm, user, realm, pass);
                    String correctresponse = digest.calculateResponse(algorithm, uri, method, nonce, ha1);
                    if (response.equals(correctresponse)) {
                        pResponse.setHeader(HeaderConstants.AUTHENTICATION_INFO, String.format("nextnonce=\"%s\"", NEXT_NONCE));
                        pChain.doFilter(pRequest, pResponse);
                    }
                    else {
                        sendAuthorization(pResponse);
                    }
                }
                else {
                    sendAuthorization(pResponse);
                }
            }
            else {
                sendAuthorization(pResponse);
            }
        }


    }

    private void sendAuthorization(HttpServletResponse pResponse) {
        List<Directive> directives = new ArrayList<Directive>();
        directives.add(new QuotedDirective("realm", REALM));
        directives.add(new QuotedDirective("nonce", NONCE));
        directives.add(new QuotedDirective("algorithm", Algorithm.MD5.name()));
        Directives dirs = new Directives(directives);
        pResponse.addHeader(HeaderConstants.WWW_AUTHENTICATE, DIGEST + dirs.toString());
        pResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private enum Algorithm {
        MD5,
        SHA1
    }

    private static class PasswordDigest {

        public String digest(Algorithm pAlgorithm, String pUsername, String pRealm, String pPassword) {
            StringBuilder builder = new StringBuilder();
            builder.append(pUsername);
            builder.append(':');
            builder.append(pRealm);
            builder.append(':');
            builder.append(pPassword);
            return hash(pAlgorithm, builder.toString());
        }

        public String calculateResponse(Algorithm algorithm, String uri, String method, String nonce, String pHA1) {
            String HA2 = calculateHA2(algorithm, uri, method);
            return hash(algorithm, String.format("%s:%s:%s", pHA1, nonce, HA2));
        }

        private String hash(final Algorithm pAlgorithm, final String pStringToHash) {
            switch (pAlgorithm) {
                case SHA1:
                    return DigestUtils.shaHex(pStringToHash);
                default:
                case MD5:
                    return DigestUtils.md5Hex(pStringToHash);
            }
        }

        private String calculateHA2(final Algorithm pAlgorithm, final String pUri, final String pMethod) {
            return hash(pAlgorithm, String.format("%s:%s", pMethod, pUri));
        }
    }

}