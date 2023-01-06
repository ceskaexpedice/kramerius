package cz.incad.kramerius.auth.thirdparty.keycloack.cdk;

import java.util.logging.Level;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;


import com.google.inject.Inject;
import com.google.inject.Injector;

import cz.incad.kramerius.auth.thirdparty.ExtAuthFilter;
import cz.incad.kramerius.auth.thirdparty.ThirdPartyUsersSupport;
import cz.incad.kramerius.auth.thirdparty.keycloack.KeycloakUserSupport;
import cz.incad.kramerius.processes.cdk.KeycloakCDKCache;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.users.LoggedUsersSingleton;

public class CDKKeycloakFilter extends ExtAuthFilter {

    @Inject
    UserManager userManager;

    @Inject
    LoggedUsersSingleton loggedUsersSingleton;

    @Inject
    KeycloakCDKCache groupCache;
    
    private CDKUserSupport cdkUserSupport;

    @Override
    protected ThirdPartyUsersSupport getThirdPartyUsersSupport() {
        return this.cdkUserSupport;
    }

    @Override
    protected boolean userStoreIsNeeded(HttpServletRequest httpReq) {
        try {
            String header = httpReq.getHeader("CDK_TOKEN_PARAMETERS");
            return header != null;
        }catch (Throwable th){
            LOGGER.log(Level.SEVERE,"Error retrieving KeycloakAccount", th);
        }
        return false;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Injector injector = getInjector(filterConfig);
        injector.injectMembers(this);
        this.cdkUserSupport = new CDKUserSupport();
        this.cdkUserSupport.setCache(this.groupCache);
        this.cdkUserSupport.setUserManager(this.userManager);
        this.cdkUserSupport.setLoggedUsersSingleton(this.loggedUsersSingleton);

    }

    protected Injector getInjector(FilterConfig config) {
        return (Injector) config.getServletContext().getAttribute(Injector.class.getName());
    }

}
