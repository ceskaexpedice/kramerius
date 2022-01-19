package cz.incad.kramerius.auth.thirdparty.shibb.external;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import com.google.inject.Inject;
import cz.incad.kramerius.auth.thirdparty.ThirdPartyUsersSupport;
import cz.incad.kramerius.auth.thirdparty.impl.AbstractThirdPartyUsersSupport;
import cz.incad.kramerius.auth.thirdparty.shibb.ShibbolethAuthFilter;
import cz.incad.kramerius.security.UserManager;

public class ExternalShibbolethAuthFilter extends ShibbolethAuthFilter {

    @Inject
    UserManager userManager;

    private AbstractThirdPartyUsersSupport authenticatedSources;
    
    @Override
    public void init(FilterConfig fConfig) throws ServletException {
        this.authenticatedSources = new ExternalThirdPartyUsersSupportImpl();
        this.authenticatedSources.setUserManager(this.userManager);

    }

    @Override
    protected ThirdPartyUsersSupport getThirdPartyUsersSupport() {
        return this.authenticatedSources;
    }

}
