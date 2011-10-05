package org.codehaus.httpcache4j;

import org.junit.Assert;
import org.junit.Test;

public class HeaderTest {

    @Test
    public void valueOfMustAllowDateValuesToBeSet() {
        Header header = Header.valueOf("Expires:Wed, 05 Oct 2011 22:00:00 GMT");
        Assert.assertEquals("Expires", header.getName());
        Assert.assertEquals("Wed, 05 Oct 2011 22:00:00 GMT", header.getValue());
    }
}
