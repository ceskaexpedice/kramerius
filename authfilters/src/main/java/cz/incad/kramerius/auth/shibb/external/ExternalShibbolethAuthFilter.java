package cz.incad.kramerius.auth.shibb.external;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import cz.incad.kramerius.auth.AuthenticatedUsers;
import cz.incad.kramerius.auth.shibb.ShibbolethAuthFilter;

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
