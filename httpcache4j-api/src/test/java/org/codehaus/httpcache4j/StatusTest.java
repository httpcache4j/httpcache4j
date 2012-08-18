package org.codehaus.httpcache4j;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
 */
public class StatusTest {
    @Test
    public void checkStatusSuccessCategory() {
        Assert.assertEquals(Status.Category.SUCCESS, Status.OK.getCategory());
        Assert.assertEquals(Status.Category.SUCCESS, Status.NO_CONTENT.getCategory());
    }

    @Test
    public void checkStatusRedirectionCategory() {
        Assert.assertEquals(Status.Category.REDIRECTION, Status.FOUND.getCategory());
    }

    @Test
    public void checkStatusClientErrorCategory() {
        Assert.assertEquals(Status.Category.CLIENT_ERROR, Status.NOT_FOUND.getCategory());
        Assert.assertTrue(Status.BAD_REQUEST.isClientError());
        Assert.assertFalse(Status.BAD_REQUEST.isServerError());
    }

    @Test
    public void checkStatusServerErrorCategory() {
        Assert.assertEquals(Status.Category.SERVER_ERROR, Status.INTERNAL_SERVER_ERROR.getCategory());
        Assert.assertTrue(Status.INTERNAL_SERVER_ERROR.isServerError());
        Assert.assertFalse(Status.INTERNAL_SERVER_ERROR.isClientError());
    }
}
