package org.codehaus.httpcache4j.resolver;

import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

import static org.junit.Assert.*;

public class ResolverConfigurationTest {

    @Test
    public void checkDefaultValues() {
        ResolverConfiguration config = new ResolverConfiguration();
        assertFalse(config.isUseChunked());
        assertThat(config.getUserAgent(), JUnitMatchers.containsString("HTTPCache4j"));
    }

    @Test
    public void checkUserAgentSetToSomethingElse() {
        ResolverConfiguration config = new ResolverConfiguration("agent", true);
        assertTrue(config.isUseChunked());
        assertEquals("agent", config.getUserAgent());
    }
}
