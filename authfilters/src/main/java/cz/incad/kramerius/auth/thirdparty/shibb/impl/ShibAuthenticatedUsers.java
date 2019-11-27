package cz.incad.kramerius.auth.thirdparty.shibb.impl;

import java.io.FileInputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.auth.thirdparty.impl.AbstractAuthenticatedUsers;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.ShibRuleLexer;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.ShibRuleParser;
import cz.incad.kramerius.auth.thirdparty.shibb.rules.objects.ShibRules;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.ClientShibbolethContext;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.ShibbolethUserWrapper;
import cz.incad.kramerius.auth.thirdparty.shibb.utils.ShibbolethUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public abstract class ShibAuthenticatedUsers extends AbstractAuthenticatedUsers<ShibbolethUserWrapper> {

    public static final Logger LOGGER = Logger.getLogger(ShibAuthenticatedUsers.class.getName());

    public static final String SHIBBOLETH_USER_PREFIX = "_shibboleth_";

    @Inject
    protected Provider<HttpServletRequest> requestProvider;

    protected String readShibbolethConfigFile() throws Exception {
        String shibRulesPath = KConfiguration.getInstance().getShibAssocRules();
        LOGGER.fine("reading rules file :" + shibRulesPath);
        String readAsString = IOUtils.readAsString(new FileInputStream(shibRulesPath), Charset.forName("UTF-8"), true);
        return readAsString;
    }

    public ShibbolethUserWrapper createUserWrapper(HttpServletRequest req, String userName) throws Exception {
        ShibbolethUserWrapper wrap = new ShibbolethUserWrapper(userName);
        ClientShibbolethContext ctx = new ClientShibbolethContext(req, wrap);

        String readAsString = readShibbolethConfigFile();

        ShibRuleLexer shibRuleLexer = new ShibRuleLexer(new StringReader(readAsString));
        ShibRuleParser shibRuleParser = new ShibRuleParser(shibRuleLexer);

        ShibRules shibRules = shibRuleParser.shibRules();
        LOGGER.fine("shib rules parsed and trying to evaluate");

        shibRules.evaluate(ctx);
        LOGGER.fine("shib rules evaluated");
        return wrap;
    }


    @Override
    public String storeUserPropertiesToSession(HttpServletRequest req, String userName) throws Exception {
        String pass = super.storeUserPropertiesToSession(req, userName);
        ShibbolethUtils.storeShibbolethSession(req);
        return pass;
    }

    @Override
    public String calculateUserName(HttpServletRequest request) {
        String uname = null;
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null) {
            uname = userPrincipal.getName();
        } else if (request.getRemoteUser() != null){
            uname = request.getRemoteUser();
        } else {
            uname = request.getHeader("REMOTE_USER");
        }
        if (uname != null) {
            return SHIBBOLETH_USER_PREFIX+"_"+uname;
        } else return null;
    }
    
}
