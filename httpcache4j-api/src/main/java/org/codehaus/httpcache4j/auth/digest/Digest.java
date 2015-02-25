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

import org.codehaus.httpcache4j.Directive;
import org.codehaus.httpcache4j.HTTPHost;
import org.codehaus.httpcache4j.auth.AuthScheme;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class Digest {

    private final HTTPHost host;
    private final String nonce;
    private final List<URI> domain;
    private final String opaque;
    private final boolean stale;
    private final Algorithm algorithm;
    private final String qop;
    private final AuthScheme scheme;


    public Digest(HTTPHost host, AuthScheme scheme) {
        this.scheme = scheme;
        Directive directives = scheme.getDirective();

        this.host = host;
        this.nonce = directives.getParameterValue("nonce");
        this.domain = parseDomain(directives.getParameterValue("domain"));
        this.opaque = directives.getParameterValue("opaque");
        this.stale = Boolean.parseBoolean(directives.getParameterValue("stale"));
        this.algorithm = Algorithm.getAlgorithm(directives.getParameterValue("algorithm"));
        this.qop = directives.getParameterValue("qop");
    }

    private List<URI> parseDomain(String domain) {
        if (!Objects.toString(domain, "").isEmpty() && !"*".equals(domain)) {
            String[] strings = domain.split(" ");
            List<URI> uris = Arrays.asList(strings).stream().map(this::parseURI).collect(Collectors.toList());
            return Collections.unmodifiableList(uris);
        }
        return Collections.emptyList();
    }

    private URI parseURI(String string) {
        if (string.startsWith("/")) {
            string = host.toURI().resolve(string).toString();
        }
        URI uri = URI.create(string);
        if (!uri.isAbsolute() && uri.getHost() == null) {
            uri = host.toURI().resolve(uri);
        }
        return uri;
    }

    public String getNonce() {
        return nonce;
    }

    public List<URI> getDomain() {
        return domain;
    }

    public String getOpaque() {
        return opaque;
    }

    public boolean isStale() {
        return stale;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public String getQop() {
        return qop;
    }

    public AuthScheme getScheme() {
        return scheme;
    }

    public HTTPHost getHost() {
        return host;
    }

    @Override
    public String toString() {
        return "Digest{" +
                "host=" + host +
                ", nonce='" + nonce + '\'' +
                ", domain=" + domain +
                ", opaque='" + opaque + '\'' +
                ", stale=" + stale +
                ", algorithm=" + algorithm +
                ", qop='" + qop + '\'' +
                '}';
    }
}
