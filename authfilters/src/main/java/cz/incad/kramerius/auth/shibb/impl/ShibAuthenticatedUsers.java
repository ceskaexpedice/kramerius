package cz.incad.kramerius.auth.shibb.impl;

import java.io.FileInputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.auth.AuthenticatedUsers;
import cz.incad.kramerius.auth.shibb.rules.ShibRuleLexer;
import cz.incad.kramerius.auth.shibb.rules.ShibRuleParser;
import cz.incad.kramerius.auth.shibb.rules.objects.ShibRules;
import cz.incad.kramerius.auth.shibb.utils.ClientShibbolethContext;
import cz.incad.kramerius.auth.shibb.utils.ShibbolethUserWrapper;
import cz.incad.kramerius.auth.shibb.utils.ShibbolethUtils;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public abstract class ShibAuthenticatedUsers implements AuthenticatedUsers {

    public static final Logger LOGGER = Logger.getLogger(ShibAuthenticatedUsers.class.getName());

    protected Map<String, String> credentials = new HashMap<String, String>();
    
    @Inject
    protected Provider<HttpServletRequest> requestProvider;

    protected abstract String updateUser(String userName, ShibbolethUserWrapper wrapper) throws Exception;

    protected abstract boolean userExists(String userName) throws Exception;

    protected abstract String createUser(String user, ShibbolethUserWrapper w) throws Exception;

    protected String readShibbolethConfigFile() throws Exception {
        String shibRulesPath = KConfiguration.getInstance().getShibAssocRules();
        LOGGER.fine("reading rules file :" + shibRulesPath);
        String readAsString = IOUtils.readAsString(new FileInputStream(shibRulesPath), Charset.forName("UTF-8"), true);
        return readAsString;
    }

    protected ShibbolethUserWrapper evaluateShibRules(HttpServletRequest req, String userName) throws Exception {
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
    public synchronized void disconnectUser(String userName) {
        this.credentials.remove(userName);
    }

    @Override
    public synchronized String getUserPassword(String userName) {
        return this.credentials.get(userName);
    }

    public synchronized HttpServletRequest updateRequest(final HttpServletRequest req) {
        final Object userName = req.getSession().getAttribute(UserUtils.USER_NAME_PARAM);
        final Object password = req.getSession().getAttribute(UserUtils.PSWD_PARAM);
        if (userName != null && password != null) {
            return new HttpServletRequestWrapper(req) {

                @Override
                public String getRemoteUser() {
                  return userName.toString();
                }
                
                @Override
                public Principal getUserPrincipal() {
                    Principal userPrincipal = super.getUserPrincipal();
                    if (userPrincipal == null) {
                        userPrincipal = new Principal() {
                            
                            @Override
                            public String getName() {
                                return userName.toString();
                            }
                        };
                    }
                    return userPrincipal;
                }

                @Override
                public String getHeader(String name) {
                    HttpSession session = req.getSession();
                    Object value = session.getAttribute(name);
                    if (value != null) {
                        return value.toString();
                    } else {
                        return super.getHeader(name);
                    }
                }
            };
        } else return req;
    }
    
    public synchronized String updateUser(HttpServletRequest req, String userName) throws Exception {
        String password = null;
        ShibbolethUserWrapper wrapper = evaluateShibRules(req, userName);

        if (userExists(userName)) {
            password = updateUser(userName, wrapper);
        } else {
            password = createUser(userName, wrapper);
        }

        ShibbolethUtils.storeShibbolethSession(req);
        this.credentials.put(userName, password);
        req.getSession().setAttribute(UserUtils.USER_NAME_PARAM, userName);
        req.getSession().setAttribute(UserUtils.PSWD_PARAM, password);

        req.getSession().setAttribute(UserUtils.FIRST_NAME_KEY, wrapper.getProperty(UserUtils.FIRST_NAME_KEY));
        req.getSession().setAttribute(UserUtils.LAST_NAME_KEY, wrapper.getProperty(UserUtils.LAST_NAME_KEY));
        
        return password;
    }

}
