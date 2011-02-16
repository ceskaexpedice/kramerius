package cz.incad.kramerius.rights.server.utils;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.GroupImpl;
import cz.incad.kramerius.security.impl.UserImpl;
import cz.incad.kramerius.security.jaas.K4UserPrincipal;

public class GetCurrentLoggedUser {

	public static User getCurrentLoggedUser(HttpServletRequest request) {
		Principal principal = request.getUserPrincipal();
		if (principal != null) {
		    K4UserPrincipal k4principal = (K4UserPrincipal) principal;
		    User user = k4principal.getUser();
		    return user;
		} else return null;		
	}

}
