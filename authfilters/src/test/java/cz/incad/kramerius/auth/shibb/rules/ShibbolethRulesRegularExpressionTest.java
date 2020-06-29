package cz.incad.kramerius.auth.shibb.rules;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.kramerius.auth.shibb.RequestSupportForTests;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.ShibRuleLexer;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.ShibRuleParser;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.ShibRules;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.ClientShibbolethContext;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.ShibbolethUserWrapper;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.StringReader;
import java.util.Enumeration;

public class ShibbolethRulesRegularExpressionTest {

    static String shibRules1 = "match(/.*staff.*/,header(\"affilation\")) {\n" +
            "       user(\"firstname\", header(\"remote_user\"))  \n" +
            "       user(\"edupersonuniqueid\", header(\"edupersonuniqueid\"))  \n" +
            "       role(\"k4_admins\")\n" +
            "}";

    static String shibRules2 = "match(header(\"affilation\"),/.*staff.*/) {\n" +
            "       user(\"firstname\", header(\"remote_user\"))  \n" +
            "       user(\"edupersonuniqueid\", header(\"edupersonuniqueid\"))  \n" +
            "       role(\"k4_admins\")\n" +
            "}";

    @Test
    public void testShibRules1() throws TokenStreamException, RecognitionException {
        testShibRules(shibRules1);
    }

    @Test
    public void testShibRules2() throws TokenStreamException, RecognitionException {
        testShibRules(shibRules2);
    }

    private void testShibRules(String rules) throws RecognitionException, TokenStreamException {
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeaderNames()).andAnswer(new IAnswer<Enumeration>() {
            @Override
            public Enumeration answer() throws Throwable {
                return RequestSupportForTests.getLoggedShibLowerCaseTable().keys();
            }
        });

        EasyMock.expect(req.getHeader("affilation")).andReturn("staff@mzk.cz;member@mzk.cz;employee@mzk.cz").anyTimes();
        EasyMock.expect(req.getHeader("remote_user")).andReturn("user@mzk.cz").anyTimes();
        EasyMock.expect(req.getHeader("edupersonuniqueid")).andReturn("edupersonAtt").anyTimes();

        RequestSupportForTests.callExpectation(req, RequestSupportForTests.getLoggedShibLowerCaseTable().keys(), RequestSupportForTests.getLoggedShibLowerCaseTable());
        EasyMock.replay(req);

        String userName = "user@mzk.cz";
        ShibbolethUserWrapper wrapper = new ShibbolethUserWrapper(userName);
        ClientShibbolethContext ctx = new ClientShibbolethContext(req, wrapper);

        ShibRuleLexer shibRuleLexer = new ShibRuleLexer(new StringReader(rules));
        ShibRuleParser shibRuleParser = new ShibRuleParser(shibRuleLexer);

        ShibRules shibRules = shibRuleParser.shibRules();
        shibRules.evaluate(ctx);

        Assert.assertTrue(wrapper.getRoles().size() == 1);
        Assert.assertTrue(wrapper.getRoles().get(0).equals("k4_admins"));

        Assert.assertTrue(wrapper.getProperty("edupersonuniqueid") != null );
        Assert.assertTrue(wrapper.getProperty("edupersonuniqueid").equals("edupersonAtt"));
    }
}
