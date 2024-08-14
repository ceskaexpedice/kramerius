package cz.incad.kramerius.auth.thirdparty.shibb.internal;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import com.google.inject.Inject;
import com.google.inject.Injector;

import cz.incad.kramerius.auth.thirdparty.ThirdPartyUsersSupport;
import cz.incad.kramerius.auth.thirdparty.shibb.ShibbolethAuthFilter;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.users.LoggedUsersSingleton;

public class InternalShibbolethAuthFilter extends ShibbolethAuthFilter {

    
    @Inject
    UserManager userManager;
//    @Inject
//    LoggedUsersSingleton loggedUsersSingleton;


    ThirdPartyUsersSupport authenticatedSources;
    
    @Override
    protected ThirdPartyUsersSupport getThirdPartyUsersSupport() {
        return this.authenticatedSources;
    }

    
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        Injector injector = getInjector(arg0);
        injector.injectMembers(this);
        InternalThirdPartyUsersSupportImpl internalAuthUsers = new InternalThirdPartyUsersSupportImpl();
        internalAuthUsers.setUserManager(this.userManager);
//        internalAuthUsers.setLoggedUsersSingleton(this.loggedUsersSingleton);
        this.authenticatedSources = internalAuthUsers;
    }

    
    protected Injector getInjector(FilterConfig config) {
        return (Injector) config.getServletContext().getAttribute(Injector.class.getName());
    }

    
    
}
