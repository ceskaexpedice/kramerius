package cz.incad.kramerius.auth.shibb;

import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.ConfigurationException;
import org.json.JSONException;

import com.google.inject.Inject;
import com.google.inject.Injector;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import cz.incad.kramerius.auth.ExtAuthFilter;
import cz.incad.kramerius.auth.AuthenticatedUsers;
import cz.incad.kramerius.auth.shibb.utils.ShibbolethUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public abstract class ShibbolethAuthFilter implements ExtAuthFilter {

    public static final String SHIBBOLETH_USER_PREFIX="_shibboleth_";
    public static final String SHIBBOLETH_SESSION_ID="shibboleth_session";
    
    public static Logger LOGGER = Logger.getLogger(ShibbolethAuthFilter.class.getName());
    
    


    @Override
    public void destroy() { }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        try {
            boolean shibFlag = KConfiguration.getInstance().getConfiguration().getBoolean("shibboleth", true);
            if (shibFlag) {
                Object value = httpReq.getSession().getAttribute(THIRD_PARTY_AUTHENTICATED_USER_KEY);
                if (value == null ||  (!value.equals("true"))) {
                    String calculated = calculateUserName(httpReq);
                    if (ShibbolethUtils.isUnderShibbolethSession(httpReq) && (!ShibbolethUtils.isShibbolethSessionIsStored(httpReq))) {
                        if (calculated != null) {
                            getExternalAuthenticatedUsers().updateUser(httpReq, calculated);
                            httpReq.getSession().setAttribute(THIRD_PARTY_AUTHENTICATED_USER_KEY, "true");
                        }
                    } else if (ShibbolethUtils.isUnderShibbolethSession(httpReq) && ShibbolethUtils.isShibbolethSessionIsStored(httpReq) && (!ShibbolethUtils.validateShibbolethSessionId(httpReq))) {
                        if (calculated != null) {
                            getExternalAuthenticatedUsers().updateUser(httpReq, calculated);
                            httpReq.getSession().setAttribute(THIRD_PARTY_AUTHENTICATED_USER_KEY, "true");
                        }
                    }
                }
            }
            
            chain.doFilter(getExternalAuthenticatedUsers().updateRequest((HttpServletRequest) req),resp);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            throw new ServletException(e);
        }
    }

    private static String calculateUserName(HttpServletRequest request) {
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

    
    protected abstract AuthenticatedUsers getExternalAuthenticatedUsers();
    
}
