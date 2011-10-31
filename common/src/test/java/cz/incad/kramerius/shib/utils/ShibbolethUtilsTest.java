/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.shib.utils;


import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.replay;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.fedora.api.ArrayOfString;
import org.fedora.api.FedoraAPIM;
import org.junit.Assert;
import org.junit.Test;

import cz.incad.kramerius.fedora.impl.DataPrepare;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ShibbolethUtilsTest {

    Hashtable<String, String> shibTable = new Hashtable<String, String>();

    public static Hashtable<String, String> getLoggedShibTable() {
        Hashtable<String, String> table = new Hashtable<String, String>();
        table.put("AJP_Shib-Session-ID", "_8b58b975229f61df5d9389b8f2d0d8d8");
        table.put("AJP_Shib-Identity-Provider", "https://shibboleth.mzk.cz/simplesaml/metadata.xml");
        table.put("AJP_Shib-Authentication-Method", "urn:oasis:names:tc:SAML:2.0:ac:classes:Password");
        table.put("AJP_Shib-Authentication-Instant", "2011-10-25T17:14:49Z");
        table.put("AJP_Shib-AuthnContext-Class", "urn:oasis:names:tc:SAML:2.0:ac:classes:Password");
        table.put("AJP_Shib-Assertion-Count", "");
        table.put("AJP_Shib-AuthnContext-Decl", "");
        table.put("AJP_Shib-Application-ID", "default");
        return table;
    }

    public static Hashtable<String, String> getNotLoggedShibTable() {
        Hashtable<String, String> table = new Hashtable<String, String>();
        table.put("AJP_Shib-Session-ID", "");
        table.put("AJP_Shib-Identity-Provider", "");
        table.put("AJP_Shib-Authentication-Method", "");
        table.put("AJP_Shib-Authentication-Instant", "");
        table.put("AJP_Shib-AuthnContext-Class", "");
        table.put("AJP_Shib-Assertion-Count", "");
        table.put("AJP_Shib-AuthnContext-Decl", "");
        table.put("AJP_Shib-Application-ID", "");
        return table;
    }

    
    @Test
    public void testIsUnderShibbolethSession_NotLogged() {
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeaderNames()).andAnswer(new IAnswer<Enumeration>() {

            @Override
            public Enumeration answer() throws Throwable {
                return getNotLoggedShibTable().keys();
            }
        });

        callExpectation(req, getNotLoggedShibTable().keys(), getNotLoggedShibTable());

        EasyMock.replay(req);

        Assert.assertFalse("expecting not logged user",ShibbolethUtils.isUnderShibbolethSession(req));

    }
    
    @Test
    public void testIsUnderShibbolethSession_Logged() {

        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeaderNames()).andAnswer(new IAnswer<Enumeration>() {

            @Override
            public Enumeration answer() throws Throwable {
                return getLoggedShibTable().keys();
            }
        });

        callExpectation(req, getLoggedShibTable().keys(), getLoggedShibTable());

        EasyMock.replay(req);

        Assert.assertTrue("expecting logged user",ShibbolethUtils.isUnderShibbolethSession(req));
    }

    public static void callExpectation(HttpServletRequest req, Enumeration<String> keys, final Hashtable<String, String> table) {
        while(keys.hasMoreElements()) {
            final String k = keys.nextElement();
            int times = "AJP_Shib-Identity-Provider".equals(k) ? 2 : 1;
            EasyMock.expect(req.getHeader(k)).andAnswer(new IAnswer<String>() {

                @Override
                public String answer() throws Throwable {
                    return table.get(k);
                }
            }).times(times);
        }
    }
}
