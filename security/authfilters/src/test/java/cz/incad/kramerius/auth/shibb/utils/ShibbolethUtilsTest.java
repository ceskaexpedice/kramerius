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
package cz.incad.kramerius.auth.shibb.utils;


import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Test;

import cz.incad.kramerius.auth.thirdparty.shibb.utils.ShibbolethUtils;

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

    @Test
    public void testShibbolethSessionId() {
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeaderNames()).andAnswer(new IAnswer<Enumeration>() {

            @Override
            public Enumeration answer() throws Throwable {
                return getLoggedShibTable().keys();
            }
        });

        callExpectation(req, getLoggedShibTable().keys(), getLoggedShibTable());

        EasyMock.replay(req);

        String shibbolethSessionId = ShibbolethUtils.getShibbolethSessionId(req);
        Assert.assertTrue("_8b58b975229f61df5d9389b8f2d0d8d8".equals(shibbolethSessionId));
    }

    @Test
    public void testValidateSessionId() {
        final HttpSession session  =EasyMock.createMock(HttpSession.class);
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeaderNames()).andAnswer(new IAnswer<Enumeration>() {
            @Override
            public Enumeration answer() throws Throwable {
                return getLoggedShibTable().keys();
            }
        }).anyTimes();

        EasyMock.expect(req.getSession(true)).andAnswer(new IAnswer<HttpSession>() {
            @Override
            public HttpSession answer() throws Throwable {
                return session;
            }
        }).anyTimes();
        
        EasyMock.expect(session.getAttribute("Shib-Session-ID")).andAnswer(new IAnswer<String>() {
            @Override
            public String answer() throws Throwable {
                return "_8b58b975229f61df5d9389b8f2d0d8d8";
            }
        }).anyTimes();
        
        callExpectation(req, getLoggedShibTable().keys(), getLoggedShibTable());

        EasyMock.replay(req,session);
        Assert.assertTrue(ShibbolethUtils.validateShibbolethSessionId(req));
    }

    @Test
    public void testValidateSessionId2() {
        final HttpSession session  =EasyMock.createMock(HttpSession.class);
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeaderNames()).andAnswer(new IAnswer<Enumeration>() {
            @Override
            public Enumeration answer() throws Throwable {
                return getLoggedShibTable().keys();
            }
        }).anyTimes();

        EasyMock.expect(req.getSession(true)).andAnswer(new IAnswer<HttpSession>() {
            @Override
            public HttpSession answer() throws Throwable {
                return session;
            }
        }).anyTimes();
        
        EasyMock.expect(session.getAttribute("Shib-Session-ID")).andAnswer(new IAnswer<String>() {
            @Override
            public String answer() throws Throwable {
                return "_abe8b975229f61df5d9389b8f2d0d8d8";
            }
        }).anyTimes();
        
        callExpectation(req, getLoggedShibTable().keys(), getLoggedShibTable());

        EasyMock.replay(req,session);
        Assert.assertFalse(ShibbolethUtils.validateShibbolethSessionId(req));
    }

    public static void callExpectation(HttpServletRequest req, Enumeration<String> keys, final Hashtable<String, String> table) {
        while(keys.hasMoreElements()) {
            final String k = keys.nextElement();
            EasyMock.expect(req.getHeader(k)).andAnswer(new IAnswer<String>() {

                @Override
                public String answer() throws Throwable {
                    return table.get(k);
                }
            }).anyTimes();
        }
    }
}
