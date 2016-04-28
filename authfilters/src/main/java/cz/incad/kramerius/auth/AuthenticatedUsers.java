package cz.incad.kramerius.auth;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONException;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.kramerius.auth.shibb.rules.ShibRuleLexer;
import cz.incad.kramerius.auth.shibb.rules.ShibRuleParser;
import cz.incad.kramerius.auth.shibb.rules.objects.ShibRules;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Implementation of this interface is dedicated to store external authenticated users and 
 * their passwords. Passwords are removed after closing user's session.
 * 
 * @author pavels
 *
 */
public interface AuthenticatedUsers {

    public HttpServletRequest updateRequest(HttpServletRequest req);
    
    public String updateUser(HttpServletRequest req, String userName) throws Exception;
    
    public void disconnectUser(String userName);

    public  String getUserPassword(String userName);
}

