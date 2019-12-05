package cz.incad.kramerius.auth.thirdparty;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


/**
 * Basic filter reponsible for third party authentication
 * @author pavels
 *
 */
public abstract class ExtAuthFilter implements Filter {

    public static final Logger LOGGER = Logger.getLogger(ExtAuthFilter.class.getName());
    
    /**
     * Flag stored in session says, that user is already autheanticated
     */
    public static final String THIRD_PARTY_AUTHENTICATED_USER_KEY = "third_party_user";

    /**
     * Return AuthenticatedUser instance
     * @return
     */
    protected abstract AuthenticatedUsers getExternalAuthenticatedUsers();

    /**
     * Returns true if filter needs to store user got from http response
     * @param httpReq
     * @return
     */
    protected abstract boolean userStoreIsNeeded(HttpServletRequest httpReq);

    @Override
    public void destroy() {
        
    }

    
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) req;
        try {
            Object value = httpReq.getSession().getAttribute(THIRD_PARTY_AUTHENTICATED_USER_KEY);
            if (value == null || (!value.equals("true"))) {
                if (userStoreIsNeeded(httpReq)) {
                    String calculated = getExternalAuthenticatedUsers().calculateUserName(httpReq);
                    if (calculated != null) {
                        getExternalAuthenticatedUsers().storeUserPropertiesToSession(httpReq, calculated);
                        httpReq.getSession().setAttribute(THIRD_PARTY_AUTHENTICATED_USER_KEY, "true");
                    }
                }
            }
            chain.doFilter(getExternalAuthenticatedUsers().updateRequest((HttpServletRequest) req), resp);
        } catch (IOException e) {
            throw e;
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ServletException(e);
        }
    }
}
