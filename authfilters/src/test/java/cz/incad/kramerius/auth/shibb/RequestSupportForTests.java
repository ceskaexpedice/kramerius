package cz.incad.kramerius.auth.shibb;

import org.easymock.EasyMock;
import org.easymock.IAnswer;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Hashtable;

public class RequestSupportForTests {

    public static Hashtable<String, String> getLoggedShibLowerCaseTable() {
        Hashtable<String, String> table = new Hashtable<String, String>();

        table.put("shib-session-id", "_dd68cbd66641c9b647b05509ac0241f7");
        table.put("shib-session-index", "_36e3755e67acdeaf1b8b6f7ebebecdeb3abd6ddc98");
        table.put("shib-session-expires", "1592847906");
        table.put("shib-identity-provider", "https://shibboleth.mzk.cz/simplesaml/metadata.xml");
        table.put("shib-authentication-method", "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");
        table.put("shib-handler", "https://dnnt.mzk.cz/Shibboleth.sso");
        table.put("affiliation","staff@mzk.cz;member@mzk.cz;employee@mzk.cz");

        return table;
    }

    public static Hashtable<String, String> getLoggedShibTable() {
        Hashtable<String, String> table = new Hashtable<String, String>();
        table.put("AJP_Shib-Session-ID", "_8b58b975229f61df5d9389b8f2d0d8d8");
        table.put("AJP_Shib-Identity-Provider", "https://shibboleth.mzk.cz/simplesaml/metadata.xml");
        table.put("AJP_Shib-Authentication-Method", "urn:oasis:names:tc:SAML:2.0:ac:classes:Password");
        table.put("AJP_Shib-Authentication-Instant", "2011-10-25T17:14:49Z");
        table.put("AJP_Shib-AuthnContext-Class", "urn:oasis:names:tc:SAML:2.0:ac:classes:Password");
        table.put("AJP_Shib-Assertion-Count", "");
        table.put("AJP_Shib-AuthnContext-Decl", "");
        table.put("AJP_uid", "happy");
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

    public static void callExpectation(HttpServletRequest req, Enumeration<String> keys, final Hashtable<String, String> table) {
        while(keys.hasMoreElements()) {
            final String k = keys.nextElement();
            EasyMock.expect(req.getHeader(k)).andAnswer(new IAnswer<String>() {

                @Override
                public String answer() {
                    return table.get(k);
                }
            }).anyTimes();
        }
    }
}
