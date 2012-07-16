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

import com.google.common.base.Strings;
import org.codehaus.httpcache4j.HTTPHost;

import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class ProxyConfiguration {
    private HTTPHost host;
    private final Set<String> ignoredHosts = new CopyOnWriteArraySet<String>();
    private final ChallengeProvider provider;

    public ProxyConfiguration() {
        this(null, null, null);
    }

    public ProxyConfiguration(HTTPHost host, String ignoredHosts, ChallengeProvider provider) {
        this.host = host;
        if (provider != null) {
            this.provider = provider;
        }
        else {
            this.provider = new ChallengeProvider.Stub();
        }
        this.ignoredHosts.addAll(parseIgnoreableHosts(ignoredHosts));
    }

    private Collection<String> parseIgnoreableHosts(String ignoredHosts) {
        if (!Strings.isNullOrEmpty(ignoredHosts)) {
            if (ignoredHosts.contains("|")) {
                return Arrays.asList(ignoredHosts.split("\\|"));
            }
            else if (ignoredHosts.contains(",")) {
                return Arrays.asList(ignoredHosts.split(","));
            }
        }
        return Collections.emptySet();
    }

    public boolean isHostIgnored(String host) {
        for (String candidate : ignoredHosts) {
            candidate = candidate.trim();
            if (candidate.startsWith("*") && host.contains(candidate.substring(1))) {
                return true;
            }
            else if (candidate.endsWith("*") && host.contains(candidate.substring(0, candidate.length() - 1))) {
                return true;
            }
            else if (host.equals(candidate)) {
                return true;
            }
        }
        return false;
    }


    public HTTPHost getHost() {
        return host;
    }

    public Set<String> getIgnoredHosts() {
        return ignoredHosts;
    }

    public ChallengeProvider getProvider() {
        return provider;
    }
}
