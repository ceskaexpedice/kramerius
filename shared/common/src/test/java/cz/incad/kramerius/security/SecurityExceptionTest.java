package cz.incad.kramerius.security;

import junit.framework.Assert;
import junit.framework.TestCase;

public class SecurityExceptionTest extends TestCase {

    public void testSecurityMessage() {
        SecurityException excp = new SecurityException( new SecurityException.SecurityExceptionInfo(SecuredActions.ADMINISTRATE));
        Assert.assertEquals("action ADMINISTRATE is not allowed", excp.getMessage());

        excp = new SecurityException( new SecurityException.SecurityExceptionInfo(SecuredActions.READ,"uuid:xxxx"));
        Assert.assertEquals("action READ is not allowed, object is uuid:xxxx", excp.getMessage());

        excp = new SecurityException( new SecurityException.SecurityExceptionInfo(SecuredActions.READ,"uuid:xxxx", "IMG_FULL"));
        Assert.assertEquals("action READ is not allowed, object is uuid:xxxx, datastream is IMG_FULL", excp.getMessage());
    }
}
