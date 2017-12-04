package cz.incad.kramerius.auth.thirdparty.social;

import java.util.logging.Logger;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brickred.socialauth.SocialAuthManager;

import cz.incad.kramerius.auth.thirdparty.AuthenticatedUsers;
import cz.incad.kramerius.auth.thirdparty.ExtAuthFilter;
import cz.incad.kramerius.auth.thirdparty.social.utils.OpenIDFlag;

public class SocialAuthFilter extends ExtAuthFilter {

    public static Logger LOGGER = Logger.getLogger(SocialAuthFilter.class.getName());

    public static final String PROVIDER_KEY = "provider";
    
    private AuthenticatedUsers authenticatedUsers;
    
    public static void loginReqests(HttpServletRequest req, HttpServletResponse resp, String provider,
            String successUrl) throws Exception {
        OpenIDFlag lState = OpenIDFlag.flagFromRequest(req).next(req);
        SocialAuthManager authManager = lState.authManager(req);
        String redirectingUrl = authManager.getAuthenticationUrl(provider, successUrl);
        resp.sendRedirect(redirectingUrl);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        this.authenticatedUsers = new OpenIDAuthenticatedUsers();
    }


    @Override
    protected AuthenticatedUsers getExternalAuthenticatedUsers() {
        return this.authenticatedUsers;
    }


    @Override
    protected boolean userStoreIsNeeded(HttpServletRequest httpReq) {
        OpenIDFlag flag = OpenIDFlag.flagFromRequest(httpReq);
        return OpenIDFlag.LOGIN_INITIALIZED.equals(flag);
    }
}
