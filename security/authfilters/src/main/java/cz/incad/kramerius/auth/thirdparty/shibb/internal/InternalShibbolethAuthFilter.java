package cz.incad.kramerius.auth.thirdparty.shibb.internal;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import com.google.inject.Inject;
import com.google.inject.Injector;

import cz.incad.kramerius.auth.thirdparty.AuthenticatedUsers;
import cz.incad.kramerius.auth.thirdparty.shibb.ShibbolethAuthFilter;
import cz.incad.kramerius.security.UserManager;

public class InternalShibbolethAuthFilter extends ShibbolethAuthFilter {

    
    @Inject
    UserManager userManager;

    AuthenticatedUsers authenticatedSources;
    
    @Override
    protected AuthenticatedUsers getExternalAuthenticatedUsers() {
        return this.authenticatedSources;
    }

    
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        Injector injector = getInjector(arg0);
        injector.injectMembers(this);
        InternalAuthenticatedUsersImpl internalAuthUsers = new InternalAuthenticatedUsersImpl();
        internalAuthUsers.setUserManager(this.userManager);
        this.authenticatedSources = internalAuthUsers;
    }

    
    protected Injector getInjector(FilterConfig config) {
        return (Injector) config.getServletContext().getAttribute(Injector.class.getName());
    }

    
    
}
