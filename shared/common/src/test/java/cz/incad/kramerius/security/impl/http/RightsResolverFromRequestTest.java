package cz.incad.kramerius.security.impl.http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.security.RightsResolver;
import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.Provider;

import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class RightsResolverFromRequestTest {

    @Test
    public void testForwardAddress() {
        final HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        Provider<HttpServletRequest> reqProvider = new Provider<HttpServletRequest>() {
            @Override
            public HttpServletRequest get() {
                return req;
            }
        };
        
        EasyMock.expect(req.getHeader("X-Forwarded-For")).andReturn("192.167.1.2").anyTimes();
        EasyMock.expect(req.getRemoteAddr()).andReturn("127.0.0.1").anyTimes();
        EasyMock.expect(req.getHeaderNames()).andReturn(Collections.enumeration( Arrays.asList("X-Forwarded-For"))).anyTimes();
        EasyMock.replay(req);
        
        Configuration conf = KConfiguration.getInstance().getConfiguration();
        conf.setProperty("x_ip_forwared_enabled_for", Arrays.asList(IPAddressUtils.LOCALHOSTS));

        RightsResolver righsResolver = new RightsResolverFromRequest(null, reqProvider, null, null, null);
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

        EasyMock.expect(req.getHeader("x-forwarded-For")).andReturn("192.167.1.2").anyTimes();
        EasyMock.expect(req.getHeaderNames()).andReturn(Collections.enumeration( Arrays.asList("x-forwarded-For"))).anyTimes();
        EasyMock.expect(req.getRemoteAddr()).andReturn("127.0.0.1").anyTimes();
        EasyMock.replay(req);
        
        Configuration conf = KConfiguration.getInstance().getConfiguration();
        conf.setProperty("x_ip_forwared_enabled_for", Arrays.asList());

        RightsResolver righsResolver = new RightsResolverFromRequest(null, reqProvider, null, null, null);
        String rAddres = IPAddressUtils.getRemoteAddress(req, conf);
        Assert.assertTrue("192.167.1.2".equals(rAddres));
        Assert.assertFalse("127.0.0.1".equals(rAddres));
    }

}
