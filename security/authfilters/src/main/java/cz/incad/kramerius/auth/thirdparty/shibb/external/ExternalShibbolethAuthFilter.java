package cz.incad.kramerius.auth.thirdparty.shibb.external;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import com.google.inject.Inject;
import cz.incad.kramerius.auth.thirdparty.AuthenticatedUsers;
import cz.incad.kramerius.auth.thirdparty.impl.AbstractAuthenticatedUsers;
import cz.incad.kramerius.auth.thirdparty.shibb.ShibbolethAuthFilter;
import cz.incad.kramerius.security.UserManager;

public class ExternalShibbolethAuthFilter extends ShibbolethAuthFilter {

    @Inject
    UserManager userManager;

    private AbstractAuthenticatedUsers authenticatedSources;
    
    @Override
    public void init(FilterConfig fConfig) throws ServletException {
        this.authenticatedSources = new ExternalAuthenticatedUsersImpl();
        this.authenticatedSources.setUserManager(this.userManager);

    }

    @Override
    protected AuthenticatedUsers getExternalAuthenticatedUsers() {
        return this.authenticatedSources;
    }

}
