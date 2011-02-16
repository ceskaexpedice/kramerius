package cz.incad.kramerius.rights.server.utils;

import cz.incad.kramerius.security.Group;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.impl.UserImpl;

public class DebugLoggedUser {

	public static final String DEBUG_KEY="rightsedit.debug.key";
	
	
	public static User getCurrentLoggedUser() {
		return getPavelStastny();
	}
	
	public static User getJosefVomacka() {
		UserImpl userImpl = new UserImpl(1, "josef", "vomacka", "subadmin", 0);	
		Group knav_subadmins = new cz.incad.kramerius.security.impl.GroupImpl(4,"knav_subadmins",3);
		userImpl.setGroups(new Group[] {knav_subadmins});
		return userImpl;
	}

	public static User getPavelStastny() {
		UserImpl userImpl = new UserImpl(2, "Pavel", "Stastny", "krameriusAdmin", 0);	
		Group k4_admins = new cz.incad.kramerius.security.impl.GroupImpl(3,"k4_admins",0);
		Group knav_users = new cz.incad.kramerius.security.impl.GroupImpl(2,"knav_users",4);
		userImpl.setGroups(new Group[] {k4_admins, knav_users});
		return userImpl;
	}

	public static User getKarelPoslusny() {
		UserImpl userImpl = new UserImpl(3, "Karel", "Poslusny", "karels@poslusny.cz", 0);	
		Group knav_users = new cz.incad.kramerius.security.impl.GroupImpl(2,"knav_users",4);
		userImpl.setGroups(new Group[] { knav_users});
		return userImpl;
	}

}
