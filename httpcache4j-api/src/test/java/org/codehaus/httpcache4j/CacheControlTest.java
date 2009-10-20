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

package org.codehaus.httpcache4j;

import org.junit.Test;
import org.junit.Assert;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: $
 */
public class CacheControlTest {

    @Test
    public void testParsePrivateMaxAge10() {
        String cc = "private, max-age=10";
        CacheControl control = CacheControl.valueOf(cc);
        Assert.assertTrue("Cache-Control was not private", control.isPrivate());
        Assert.assertEquals("Cache-Control had the wrong max-age", 10, control.getMaxAge());
        Assert.assertEquals(-1, control.getMaxStale());
        Assert.assertEquals(-1, control.getSMaxAge());
        Assert.assertEquals(-1, control.getMinFresh());
    }

    @Test
    public void testParseAllPrivate() {
        String cc = "private, max-age=10, s-maxage=10, max-stale=10, min-fresh=10";
        CacheControl control = CacheControl.valueOf(cc);
        Assert.assertTrue("Cache-Control was not private", control.isPrivate());
        Assert.assertEquals("Cache-Control had the wrong max-age", 10, control.getMaxAge());
        Assert.assertEquals("Cache-Control had the wrong max-stale", 10, control.getMaxStale());
        Assert.assertEquals("Cache-Control had the wrong s-max-age", 10, control.getSMaxAge());
        Assert.assertEquals("Cache-Control had the wrong min-fresh", 10, control.getMinFresh());
    }

    @Test
    public void testParseAllPublic() {
        String cc = "public, max-age=10, s-maxage=10, max-stale=10, min-fresh=10";
        CacheControl control = CacheControl.valueOf(cc);
        Assert.assertFalse("Cache-Control was private", control.isPrivate());
        Assert.assertEquals("Cache-Control had the wrong max-age", 10, control.getMaxAge());
        Assert.assertEquals("Cache-Control had the wrong max-stale", 10, control.getMaxStale());
        Assert.assertEquals("Cache-Control had the wrong s-max-age", 10, control.getSMaxAge());
        Assert.assertEquals("Cache-Control had the wrong min-fresh", 10, control.getMinFresh());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseWrongHeader() {
        String cc = "pubic, amax-age=10, flosh, max-stale=10, min-fresh=10";
        CacheControl.valueOf(cc);
    }
}
