package cz.inovatika.kramerius.auth.jwt;

import com.google.inject.Inject;
import com.google.inject.Injector;
import cz.incad.kramerius.auth.thirdparty.ExtAuthFilter;
import cz.incad.kramerius.auth.thirdparty.ThirdPartyUsersSupport;
import cz.incad.kramerius.security.UserManager;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.logging.Level;

/**
 * JwtUserMappingFilter
 *
 * @author ppodsednik
 */
public class JwtUserMappingFilter extends ExtAuthFilter {

    @Inject
    private UserManager userManager;

    private JwtUserSupport jwtUserSupport;

    @Override
    protected ThirdPartyUsersSupport getThirdPartyUsersSupport() {
        return jwtUserSupport;
    }

    @Override
    protected boolean userStoreIsNeeded(HttpServletRequest httpReq) {
        JwtAccount principal = (JwtAccount) httpReq.getAttribute(JwtAccount.class.getName());
        if (principal != null) {
            LOGGER.log(Level.FINE, "JWT principal {0} roles={1}", new Object[]{principal.getUsername(), principal.getRoles()});
        }
        return principal != null;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Injector injector = getInjector(filterConfig);
        injector.injectMembers(this);

        this.jwtUserSupport = new JwtUserSupport();
        this.jwtUserSupport.setUserManager(this.userManager);
    }

    private Injector getInjector(FilterConfig config) {
        return (Injector) config.getServletContext().getAttribute(Injector.class.getName());
    }
}