package cz.incad.kramerius;

import java.io.File;

public class Constants {

	public static final String WORKING_DIR=System.getProperty("user.home")+File.separator+".kramerius4";
//	public static final String WORKING_DB_JDBC_URL="jdbc:derby:"+WORKING_DIR+"/kramerius4.db;create=true;user=kramerius4;password=kramerius4";
	public static final String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	public static final String WORKING_DB_JDBC_URL="jdbc:derby:"+WORKING_DIR+"/kramerius4.db;create=true;user=kramerius4;password=kramerius4";

	static {
		File dir = new File(WORKING_DIR);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}
}
