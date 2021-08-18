package cz.incad.kramerius.auth.shibb.rules;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.kramerius.auth.shibb.RequestSupportForTests;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.ShibRuleLexer;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.ShibRuleParser;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.ShibRules;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.ClientShibbolethContext;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.ShibbolethUserWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Enumeration;

public class ShibbolethTest {

    public static final String CONST = "match(header(\"AJP_uid\"),\"happy\") {\n" +
            "        user(\"firstname\",header(\"AJP_uid\"))\n" +
            "        user(\"surname\", header(\"AJP_uid\"))\n" +
            "\n" +
            "        role(\"k4_admins\")\n" +
            "\n" +
            "}\n";

    @Test
    public void testParse() throws IOException, TokenStreamException, RecognitionException {
        HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(req.getHeaderNames()).andAnswer(new IAnswer<Enumeration>() {
            @Override
            public Enumeration answer() {
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

        InputStream resourceAsStream = ShibbolethTest.class.getResourceAsStream("/shibrules.txt");
        String rules = IOUtils.toString(resourceAsStream, "UTF-8");

        ShibRuleLexer shibRuleLexer = new ShibRuleLexer(new StringReader(rules));
        ShibRuleParser shibRuleParser = new ShibRuleParser(shibRuleLexer);

        ShibRules shibRules = shibRuleParser.shibRules();
        shibRules.evaluate(ctx);

        Assert.assertTrue(wrapper.getRoles().size() == 2);
        Assert.assertTrue(wrapper.getRoles().get(0).equals("k4_admins"));
        Assert.assertTrue(wrapper.getRoles().get(1).equals("dalsi_role"));

        Assert.assertTrue(wrapper.getProperty("organization") != null );
        Assert.assertTrue(wrapper.getProperty("organization").equals("MZK"));

    }
}
