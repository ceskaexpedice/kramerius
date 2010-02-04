package cz.incad.utils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JNDIUtils {

	
	/**
	 * Vraci konfiguracni promennou z JNDI kontextu
	 * @param lookupName
	 * @return
	 */
	public static String getJNDIValue(String lookupName) {
		Context env;
        try {
            env = (Context) new InitialContext().lookup("java:comp/env");
            return  (String) env.lookup(lookupName);
        } catch (NamingException e) {
            throw new IllegalStateException(e);
        }
	}
	
	
}
