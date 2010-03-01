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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
class AuthenticatorBase {
    private final List<AuthenticatorStrategy> strategies = Lists.newArrayList();
    protected final SchemeRegistry registry = new SchemeRegistry();

    public AuthenticatorBase() {
        this(defaultStrategies());
    }

    public AuthenticatorBase(List<AuthenticatorStrategy> strategies) {
        this.strategies.addAll(strategies);
    }


    protected static List<AuthenticatorStrategy> defaultStrategies() {
        return ImmutableList.of(new DigestAuthenticatorStrategy(), new BasicAuthenticatorStrategy());
    }

    protected AuthenticatorStrategy select(AuthScheme authScheme) {
        AuthenticatorStrategy selected = null;
        for (AuthenticatorStrategy strategy : strategies) {
            if (strategy.supports(authScheme)) {
                selected = strategy;
            }
        }
        if (selected == null) {
            selected = new NullAuthenticatorStrategy();
        }
        return selected;
    }
}
