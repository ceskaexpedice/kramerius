package cz.incad.kramerius.rights.server.utils;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.jaas.K4UserPrincipal;

public class GetCurrentLoggedUser {

	public static User getCurrentLoggedUser(HttpServletRequest request) {
		if (Boolean.getBoolean(DebugLoggedUser.DEBUG_KEY)) {
			return DebugLoggedUser.getCurrentLoggedUser();
 		} else {
 			Principal principal = request.getUserPrincipal();
 			if (principal != null) {
 			    K4UserPrincipal k4principal = (K4UserPrincipal) principal;
 			    User user = k4principal.getUser();
 			    return user;
 			} else return null;		
 		}
	}
	

}
