package cz.incad.kramerius.client.socialauth;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.brickred.socialauth.Profile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import antlr.RecognitionException;
import antlr.TokenStreamException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.client.AuthenticationServlet;
import cz.incad.kramerius.client.kapi.auth.AdminUser;
import cz.incad.kramerius.client.kapi.auth.CallUserController;
import cz.incad.kramerius.client.socialauth.OpenIDSupport.OpenIdUserWrapper;
import cz.incad.kramerius.client.tools.BasicAuthenticationFilter;
import cz.incad.kramerius.client.tools.GeneratePasswordUtils;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.impl.http.shibrules.ShibRuleLexer;
import cz.incad.kramerius.security.impl.http.shibrules.ShibRuleParser;
import cz.incad.kramerius.security.impl.http.shibrules.shibs.ShibRules;
import cz.incad.kramerius.security.impl.http.shibrules.shibs.ShibbolethContext;
import cz.incad.kramerius.security.impl.http.shibrules.shibs.ShibbolethContextImpl;
import cz.incad.kramerius.shib.utils.ShibbolethUtils;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.utils.StringUtils;

public class ShibbolethSupport {

    public static final String SHIBB_KEY="shibboleth";
    
    public static final Map<String, String> CREDENTIALS = new HashMap<String, String>();

    public static Logger LOGGER = Logger.getLogger(ShibbolethSupport.class.getName());

    public static class ShibbolethUserWrapper implements UsersWrapper {
        private String calculatedName;
        private String firstName;
        private String lastName;

        private List<String> roles = new ArrayList<String>();
        
        public ShibbolethUserWrapper(String calculatedName) {
            super();
            this.calculatedName = calculatedName;
        }

        @Override
        public String getCalculatedName() {
            return this.calculatedName;
        }

        public void setFirstName(String fname) {
            this.firstName = fname;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public void setRoles(List<String> rls) {
            this.roles = rls;
        }
        
        public List<String> getRoles() {
            return roles;
        }
        
        @Override
        public String getProperty(String key) {
            if (key.equals(UsersWrapper.FIRST_NAME_KEY)) {
                return this.firstName;
            } else if (key.equals(UsersWrapper.LAST_NAME_KEY)) {
                return this.lastName;
            } else  return null;
        }
    }


    public static class ClientShibbolethContext implements ShibbolethContext {

        private HttpServletRequest request;
        
        
        private ShibbolethUserWrapper user;
        private List<String> roles = new ArrayList<String>();
        
        public ClientShibbolethContext(HttpServletRequest request, ShibbolethUserWrapper uwrap) {
            super();
            this.request = request;
            this.user = uwrap;
        }

        @Override
        public void associateFirstName(String firstName) {
            this.user.setFirstName(firstName);
            
        }

        @Override
        public void associateLastName(String lastName) {
            this.user.setLastName(lastName);
        }

        @Override
        public void associateRole(String rname) {
            this.roles.add(rname);
        }

        @Override
        public boolean isRoleAssociated(String rname) {
            return false;
        }

        @Override
        public HttpServletRequest getHttpServletRequest() {
            return this.request;
        }
    }
    
    
    public void provideRedirection(HttpServletRequest req,
            HttpServletResponse resp) throws Exception {

        if (ShibbolethUtils.isUnderShibbolethSession(req)) {
            String calculated = calculateUserName(req);
            if (calculated != null) {
                AuthenticationServlet.createCaller(req, null, null, null);
                ShibbolethUserWrapper wrp = new ShibbolethUserWrapper(calculated);
                evaluateShibRules(req,wrp);

                String generatedPassword = generatePswd();

                JSONArray users = getUser(req, wrp);
                if (users.length() > 0) {
                    JSONObject jsonObject = users.getJSONObject(0);
                    savePassword(req, "" + jsonObject.getInt("id"), generatedPassword);
                    //ProviderUsersUtils.newPasswordUser(req, "" + jsonObject.getInt("id"), generatedPassword);
                } else {
                    //ProviderUsersUtils.createUser(req, wrp, generatedPassword);
                    createNewUser(req, wrp, generatedPassword);
                    users = getUser(req, wrp);
                }

                // associate roles
                CallUserController caller = AuthenticationServlet.createCaller(req,
                        wrp.getCalculatedName(), generatedPassword, users.getJSONObject(0).toString());
                caller.getClientCaller().updateInformation(wrp.getProperty(UsersWrapper.FIRST_NAME_KEY), wrp.getProperty(UsersWrapper.LAST_NAME_KEY));

                resp.sendRedirect("index.vm");
            } else {
                LOGGER.warning("No remote user or user principal defined");
                resp.sendRedirect("index.vm");
            }
        }
    }


    public void providerLogin(HttpServletRequest req,
            HttpServletResponse resp) throws Exception {
        String loginUrl = KConfiguration.getInstance().getConfiguration().getString("shibboleth.loginpage");
        if (loginUrl != null) {
            resp.sendRedirect(loginUrl);
        } else {
            LOGGER.warning("no loginurl defined");
        }
        
    }

    //JSONArray users = ProviderUsersUtils.getUser(req, wrp);
    public JSONArray getUser(HttpServletRequest req, ShibbolethUserWrapper wrp) throws ConfigurationException, JSONException {
        return ProviderUsersUtils.getUser(req, wrp);
    }

    public String generatePswd() {
        return   GeneratePasswordUtils.generatePswd();
    }
    
    public void savePassword(HttpServletRequest req, String userId, String pswd) {
        ProviderUsersUtils.newPasswordUser(req, userId, pswd);
    }

    public void createNewUser(HttpServletRequest req, ShibbolethUserWrapper wrp, String generatedPassword) throws ConfigurationException, JSONException {
        ProviderUsersUtils.createUser(req, wrp, generatedPassword);
    }
    
    public void evaluateShibRules(HttpServletRequest req, ShibbolethUserWrapper wrapper) throws IOException, FileNotFoundException, RecognitionException, TokenStreamException {
        //ShibContext ctx = new ShibContext(this.provider.get(), user, this.userManager);
        ClientShibbolethContext ctx = new ClientShibbolethContext(req,wrapper);

        String readAsString = readShibbolethConfigFile();

        ShibRuleLexer shibRuleLexer = new ShibRuleLexer(new StringReader(readAsString));
        ShibRuleParser shibRuleParser = new ShibRuleParser(shibRuleLexer);

        ShibRules shibRules = shibRuleParser.shibRules();
        LOGGER.fine("shib rules parsed and trying to evaluate");

        shibRules.evaluate(ctx);
        LOGGER.fine("shib rules evaluated");
    }

    String readShibbolethConfigFile() throws IOException,
            FileNotFoundException {
        String shibRulesPath = KConfiguration.getInstance().getShibAssocRules();
        LOGGER.fine("reading rules file :"+shibRulesPath);
        String readAsString = IOUtils.readAsString(new FileInputStream(shibRulesPath), Charset.forName("UTF-8"), true);
        return readAsString;
    }

    public void login(HttpServletRequest request, HttpServletResponse resp) {
        try {
            providerLogin(request, resp);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
        }
    }

    private static String calculateUserName(HttpServletRequest request) {
        String uname = null;
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal != null) {
            uname = userPrincipal.getName();
        } else {
            uname = request.getRemoteUser();
        }
        if (uname != null) {
            return SHIBB_KEY+"_"+uname;
        } else return null;
    }
}
