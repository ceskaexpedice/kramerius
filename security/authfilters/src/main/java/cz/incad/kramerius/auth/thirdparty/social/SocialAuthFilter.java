package cz.incad.kramerius.auth.thirdparty.social;

import java.util.logging.Logger;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import cz.incad.kramerius.auth.thirdparty.ThirdPartyUsersSupport;
import org.brickred.socialauth.SocialAuthManager;

import cz.incad.kramerius.auth.thirdparty.ExtAuthFilter;
import cz.incad.kramerius.auth.thirdparty.social.utils.OpenIDFlag;

public class SocialAuthFilter extends ExtAuthFilter {

    public static Logger LOGGER = Logger.getLogger(SocialAuthFilter.class.getName());

    public static final String PROVIDER_KEY = "provider";
    
    private ThirdPartyUsersSupport thirdPartyUsersSupport;
    
    public static void loginReqests(HttpServletRequest req, HttpServletResponse resp, String provider,
            String successUrl) throws Exception {
        OpenIDFlag lState = OpenIDFlag.flagFromRequest(req).next(req);
        SocialAuthManager authManager = lState.authManager(req);
        String redirectingUrl = authManager.getAuthenticationUrl(provider, successUrl);
        resp.sendRedirect(redirectingUrl);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        this.thirdPartyUsersSupport = new OpenIDThirdPartyUsersSupport();
    }


    @Override
    protected ThirdPartyUsersSupport getThirdPartyUsersSupport() {
        return this.thirdPartyUsersSupport;
    }


    @Override
    protected boolean userStoreIsNeeded(HttpServletRequest httpReq) {
        OpenIDFlag flag = OpenIDFlag.flagFromRequest(httpReq);
        return OpenIDFlag.LOGIN_INITIALIZED.equals(flag);
    }
}
