package cz.incad.kramerius.auth.thirdparty.shibb.external;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import cz.incad.kramerius.auth.thirdparty.AuthenticatedUsers;
import cz.incad.kramerius.auth.thirdparty.shibb.ShibbolethAuthFilter;

public class ExternalShibbolethAuthFilter extends ShibbolethAuthFilter {

    private AuthenticatedUsers authenticatedSources;
    
    @Override
    public void init(FilterConfig fConfig) throws ServletException {
        this.authenticatedSources = new ExternalAuthenticatedUsersImpl();
    }

    @Override
    protected AuthenticatedUsers getExternalAuthenticatedUsers() {
        return this.authenticatedSources;
    }

}
