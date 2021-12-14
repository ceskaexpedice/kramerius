package cz.incad.kramerius.auth.thirdparty.keycloack;

import com.google.inject.Inject;
import com.google.inject.Injector;
import cz.incad.kramerius.auth.thirdparty.ThirdPartyUsersSupport;
import cz.incad.kramerius.auth.thirdparty.ExtAuthFilter;
import cz.incad.kramerius.auth.thirdparty.shibb.internal.InternalThirdPartyUsersSupportImpl;
import cz.incad.kramerius.security.UserManager;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

public class KeycloackFilter extends ExtAuthFilter {

    @Inject
    UserManager userManager;

    private KeycloackUserSupport authenticatedSources;

    @Override
    protected ThirdPartyUsersSupport getThirdPartyUsersSupport() {
        return null;
    }

    @Override
    protected boolean userStoreIsNeeded(HttpServletRequest httpReq) {
        return true;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Injector injector = getInjector(filterConfig);
        injector.injectMembers(this);
        KeycloackUserSupport internalAuthUsers = new KeycloackUserSupport();
        internalAuthUsers.setUserManager(this.userManager);
        this.authenticatedSources = internalAuthUsers;

    }

    protected Injector getInjector(FilterConfig config) {
        return (Injector) config.getServletContext().getAttribute(Injector.class.getName());
    }

}
