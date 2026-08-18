/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package cz.incad.kramerius.utils;

import junit.framework.Assert;

import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * @author pavels
 *
 */
public class ApplicationURLTest {

    @Test
    public void testCreateURL() {
        String created = ApplicationURL.createURL("krameriusdemo.mzk.cz", "http", "/search/");
        Assert.assertEquals("http://krameriusdemo.mzk.cz/search/", created);
    }

    @Test
    public void applicationURLUsesForwardedProto() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost:8080/search/api/client/v7.0/ui-config/general")).times(2);
        expect(request.getHeader("x-forwarded-host")).andReturn("k7.inovatika.dev");
        expect(request.getRequestURI()).andReturn("/search/api/client/v7.0/ui-config/general");
        expect(request.getHeader("x-forwarded-proto")).andReturn("https");
        expect(request.getHeader("x-forwarded-port")).andReturn(null);
        replay(request);

        String applicationURL = ApplicationURL.applicationURL(request);

        Assert.assertEquals("https://k7.inovatika.dev/search", applicationURL);
    }

    @Test
    public void applicationURLUsesForwardedPortWhenNotDefault() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost:8080/search/api/client/v7.0/ui-config/general")).times(2);
        expect(request.getHeader("x-forwarded-host")).andReturn("k7.inovatika.dev");
        expect(request.getRequestURI()).andReturn("/search/api/client/v7.0/ui-config/general");
        expect(request.getHeader("x-forwarded-proto")).andReturn("https");
        expect(request.getHeader("x-forwarded-port")).andReturn("8443");
        replay(request);

        String applicationURL = ApplicationURL.applicationURL(request);

        Assert.assertEquals("https://k7.inovatika.dev:8443/search", applicationURL);
    }

    @Test
    public void applicationURLSkipsDefaultForwardedPort() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(request.getRequestURL()).andReturn(new StringBuffer("http://localhost:8080/search/api/client/v7.0/ui-config/general")).times(2);
        expect(request.getHeader("x-forwarded-host")).andReturn("k7.inovatika.dev");
        expect(request.getRequestURI()).andReturn("/search/api/client/v7.0/ui-config/general");
        expect(request.getHeader("x-forwarded-proto")).andReturn("https");
        expect(request.getHeader("x-forwarded-port")).andReturn("443");
        replay(request);

        String applicationURL = ApplicationURL.applicationURL(request);

        Assert.assertEquals("https://k7.inovatika.dev/search", applicationURL);
    }
}
