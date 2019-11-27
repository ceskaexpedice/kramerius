package cz.incad.kramerius.security.impl.http;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Provider;

import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class IsActionAllowedFromRequestTest {

    @Test
    public void testForwardAddress() {
        final HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        Provider<HttpServletRequest> reqProvider = new Provider<HttpServletRequest>() {
            @Override
            public HttpServletRequest get() {
                return req;
            }
        };
        
        EasyMock.expect(req.getHeader("X_IP_FORWARD")).andReturn("192.167.1.2").anyTimes();
        EasyMock.expect(req.getRemoteAddr()).andReturn("127.0.0.1").anyTimes();
        EasyMock.replay(req);
        
        Configuration conf = KConfiguration.getInstance().getConfiguration();
        conf.setProperty("x_ip_forwared_enabled_for", Arrays.asList(IPAddressUtils.LOCALHOSTS));
        
        IsActionAllowedFromRequest isActionAllowed = new IsActionAllowedFromRequest(null, reqProvider, null, null, null);
        String rAddres = IPAddressUtils.getRemoteAddress(req, conf);
        Assert.assertTrue("192.167.1.2".equals(rAddres));
        Assert.assertFalse("127.0.0.1".equals(rAddres));
    }

    @Test
    public void testNotForwardAddress() {
        final HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        Provider<HttpServletRequest> reqProvider = new Provider<HttpServletRequest>() {
            @Override
            public HttpServletRequest get() {
                return req;
            }
        };
        
        EasyMock.expect(req.getHeader("X_IP_FORWARD")).andReturn("192.167.1.2").anyTimes();
        EasyMock.expect(req.getRemoteAddr()).andReturn("127.0.0.1").anyTimes();
        EasyMock.replay(req);
        
        Configuration conf = KConfiguration.getInstance().getConfiguration();
        conf.setProperty("x_ip_forwared_enabled_for", Arrays.asList());
        
        IsActionAllowedFromRequest isActionAllowed = new IsActionAllowedFromRequest(null, reqProvider, null, null, null);
        String rAddres = IPAddressUtils.getRemoteAddress(req, conf);
        Assert.assertTrue("192.167.1.2".equals(rAddres));
        Assert.assertFalse("127.0.0.1".equals(rAddres));
    }

}
