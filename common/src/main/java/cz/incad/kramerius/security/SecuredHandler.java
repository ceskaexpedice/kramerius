package cz.incad.kramerius.security;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;


public class SecuredHandler implements MethodInterceptor {

	IsUserInRoleDecision userInRoleDecision;
	
	public Object invoke(MethodInvocation invocation) throws Throwable {
		Secured secured = invocation.getMethod().getAnnotation(Secured.class);
		String[] methodRoles = secured.roles();	
		for (String role : methodRoles) {
			if (userInRoleDecision.isUserInRole(role)) {
				return invocation.proceed();
			}
		}
		throw new SecurityException("cannot perform method, user is not defined ");
	}

}
