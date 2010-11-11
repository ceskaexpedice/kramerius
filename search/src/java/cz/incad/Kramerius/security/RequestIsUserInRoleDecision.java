package cz.incad.Kramerius.security;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.security.IsUserInRoleDecision;

public class RequestIsUserInRoleDecision implements IsUserInRoleDecision {

	private Logger logger;
	private Provider<HttpServletRequest> provider;

	
	@Inject
	public RequestIsUserInRoleDecision(Logger logger,
			Provider<HttpServletRequest> provider) {
		super();
		this.logger = logger;
		this.provider = provider;
	}

	
	@Override
	public boolean isUserInRole(String roleName) {
		boolean flag = this.provider.get().isUserInRole(roleName);
		if ((!flag) && (!roleName.equals(KrameriusRoles.KRAMERIUS_ADMIN.getRoleName()))) {
			if (this.provider.get().isUserInRole(KrameriusRoles.KRAMERIUS_ADMIN.getRoleName())) return true;
		}
		return flag;
	}
}
