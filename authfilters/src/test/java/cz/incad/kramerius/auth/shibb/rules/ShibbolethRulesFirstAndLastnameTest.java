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

public class ShibbolethRulesFirstAndLastnameTest  {

    public static final String shibRules1 = "match(header(\"AJP_uid\"),\"happy\") {\n" +
            "        user(\"firstname\",header(\"AJP_uid\"))\n" +
            "        user(\"surname\", header(\"AJP_uid\"))\n" +
            "\n" +
            "        role(\"k4_admins\")\n" +
            "\n" +
            "}\n";

    @Test
    public void testFirstAndLastName() throws TokenStreamException, RecognitionException {
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeaderNames()).andAnswer(new IAnswer<Enumeration>() {
            @Override
            public Enumeration answer() throws Throwable {
                return RequestSupportForTests.getLoggedShibLowerCaseTable().keys();
            }
        });

        EasyMock.expect(req.getHeader("AJP_uid")).andReturn("happy").anyTimes();

        RequestSupportForTests.callExpectation(req, RequestSupportForTests.getLoggedShibLowerCaseTable().keys(), RequestSupportForTests.getLoggedShibLowerCaseTable());
        EasyMock.replay(req);

        String userName = "user@mzk.cz";
        ShibbolethUserWrapper wrapper = new ShibbolethUserWrapper(userName);
        ClientShibbolethContext ctx = new ClientShibbolethContext(req, wrapper);

        System.out.println(shibRules1);
        ShibRuleLexer shibRuleLexer = new ShibRuleLexer(new StringReader(shibRules1));
        ShibRuleParser shibRuleParser = new ShibRuleParser(shibRuleLexer);

        ShibRules shibRules = shibRuleParser.shibRules();
        shibRules.evaluate(ctx);

        Assert.assertTrue(wrapper.getProperty("firstname").equals("happy"));
        Assert.assertTrue(wrapper.getProperty("surname").equals("happy"));

    }
}
