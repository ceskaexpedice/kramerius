package cz.incad.kramerius.security;

/**
 * Test wheather actual user has defined given role
 * @author pavels
 */
public interface IsUserInRoleDecision {
	
	public boolean isUserInRole(String roleName);
}
