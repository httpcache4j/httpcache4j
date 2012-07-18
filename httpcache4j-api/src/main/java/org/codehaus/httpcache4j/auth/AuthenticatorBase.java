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

package org.codehaus.httpcache4j.auth;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.codehaus.httpcache4j.Directive;
import org.codehaus.httpcache4j.HTTPHost;
import org.codehaus.httpcache4j.HTTPResponse;
import org.codehaus.httpcache4j.Header;
import org.codehaus.httpcache4j.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
//TODO: Find an algorithm for selecting the most secure supported authentication method.
//I imagine the Set of ("Digest", "Basic"); and anything else is more secure than those.
class AuthenticatorBase {
    private static final Function<Directive,AuthScheme> AuthSchemeF = new Function<Directive, AuthScheme>() {
        @Override
        public AuthScheme apply(Directive input) {
            return new AuthScheme(input);
        }
    };
    private final List<AuthenticatorStrategy> strategies = Lists.newArrayList();
    protected final SchemeRegistry registry = new DefaultSchemeRegistry();

    public AuthenticatorBase() {
        this(defaultStrategies());
    }

    public AuthenticatorBase(List<AuthenticatorStrategy> strategies) {
        this.strategies.addAll(strategies);
    }


    protected static List<AuthenticatorStrategy> defaultStrategies() {
        return ImmutableList.of(new DigestAuthenticatorStrategy(), new BasicAuthenticatorStrategy());
    }

    protected Pair<AuthenticatorStrategy, AuthScheme> select(List<AuthScheme> authScheme) {
        AuthenticatorStrategy selected = null;
        AuthScheme selectedScheme = null;
        for (AuthenticatorStrategy strategy : strategies) {
            for (AuthScheme scheme : authScheme) {
                if (strategy.supports(scheme)) {
                    selected = strategy;
                    selectedScheme = scheme;
                    break;
                }
            }
        }
        if (selected == null) {
            selected = new NullAuthenticatorStrategy();
        }
        return Pair.of(selected, selectedScheme);
    }

    public boolean canAuthenticatePreemptively(HTTPHost host) {       
        return registry.matches(host);
    }

    protected List<AuthScheme> toAuthSchemes(HTTPResponse response, String name) {
        List<Header> authenticateHeader = response.getHeaders().getHeaders(name);
        List<Directive> directives = new ArrayList<Directive>();
        for (Header header : authenticateHeader) {
            Iterables.addAll(directives, header.getDirectives());
        }
        return Lists.transform(directives, AuthSchemeF);
    }
}
