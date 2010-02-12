package cz.incad.kramerius.utils;

import java.util.logging.Level;

import javax.management.RuntimeErrorException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JNDIUtils {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(JNDIUtils.class.getName());
	
	/**
	 * Vraci konfiguracni promennou z JNDI kontextu
	 * @param lookupName
	 * @return
	 */
	public static String getJNDIValue(String lookupName) {
		Context env;
        try {
            InitialContext initialContext = new InitialContext();
			env = (Context) initialContext.lookup("java:comp/env");
            return  (String) env.lookup(lookupName);
        } catch (NamingException e) {
        	LOGGER.log(Level.SEVERE, e.getMessage(), e);
        	throw new RuntimeException(e);
        }
	}

	/**
	 * Only for tests
	 * @param string
	 * @param property
	 * @return
	 * @deprecated
	 */
	public static String getJNDIValue(String string, String property) {
		try {
			return getJNDIValue(string);
		} catch(Exception e) {
			return property;
		}
	}
	
	
}
