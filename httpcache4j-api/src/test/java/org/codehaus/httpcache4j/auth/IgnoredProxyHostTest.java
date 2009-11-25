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

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Set;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class IgnoredProxyHostTest {
    private static final String IGNORED_HOSTS = "www*|localhost*|db.no|ze.bitch|*.se";

    @Test
    public void testParseIgnoredHostList() {
        ProxyConfiguration configuration = new ProxyConfiguration(null, IGNORED_HOSTS, null);
        Set<String> hosts = configuration.getIgnoredHosts();
        assertEquals("Wrong number of hosts", 5, hosts.size());
    }

    @Test
    public void testMatchesIgnoredHostList() {
        ProxyConfiguration configuration = new ProxyConfiguration(null, IGNORED_HOSTS, null);
        assertTrue("wrongly accepted", configuration.isHostIgnored("www.vg.no"));
        assertTrue("wrongly accepted", configuration.isHostIgnored("localhost.localdomain"));
        assertTrue("wrongly accepted", configuration.isHostIgnored("hd.se"));
        assertTrue("wrongly accepted", configuration.isHostIgnored("db.no"));
        assertTrue("wrongly accepted ",configuration.isHostIgnored("aftenposten.se"));
        assertFalse("wrongly ignored", configuration.isHostIgnored("db.dk"));
        assertFalse("wrongly ignored",configuration.isHostIgnored("annonser.db.no"));
    }
}
