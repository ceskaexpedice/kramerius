package cz.incad.kramerius.gwtviewers.server.utils;

public class ClassOne {
	
	public static String CLASSONE_ID = "1AB"; 

	
	public static String getMethodA(String uuid) {
		return CLASSONE_ID+"_A_"+uuid;
	}

	public static String getMethodB(String uuid) {
		return CLASSONE_ID+"_B_"+uuid;
	}
}
