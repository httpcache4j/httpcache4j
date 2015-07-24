package org.codehaus.httpcache4j;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Erlend Hamnaberg<erlend.hamnaberg@arktekk.no>
 */
public class StatusTest {
    @Test
    public void checkStatusSuccessCategory() {
        assertEquals(Status.Category.SUCCESS, Status.OK.getCategory());
        assertEquals(Status.Category.SUCCESS, Status.NO_CONTENT.getCategory());
    }

    @Test
    public void checkStatusRedirectionCategory() {
        assertEquals(Status.Category.REDIRECTION, Status.FOUND.getCategory());
    }

    @Test
    public void checkStatusClientErrorCategory() {
        assertEquals(Status.Category.CLIENT_ERROR, Status.NOT_FOUND.getCategory());
        assertTrue(Status.BAD_REQUEST.isClientError());
        assertFalse(Status.BAD_REQUEST.isServerError());
    }

    @Test
    public void checkStatusServerErrorCategory() {
        assertEquals(Status.Category.SERVER_ERROR, Status.INTERNAL_SERVER_ERROR.getCategory());
        assertTrue(Status.INTERNAL_SERVER_ERROR.isServerError());
        assertFalse(Status.INTERNAL_SERVER_ERROR.isClientError());
    }

    @Test
    public void checkStatusFromCodeIsTheSameAsCachedInMap() {
        assertEquals(Status.FORBIDDEN, Status.valueOf(403));
        assertSame(Status.FORBIDDEN, Status.valueOf(403));
    }

    @Test
    public void checkThatAllStatusesAreInTheMap() {
        assertEquals(40, Status.STATUSES.size());
    }
}
