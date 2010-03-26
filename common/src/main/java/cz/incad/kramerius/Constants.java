package cz.incad.kramerius;

import java.io.File;

public class Constants {

	public static final String WORKING_DB_JDBC_URL="jdbc:derby:"+System.getProperty("user.home")+File.separator+".kramerius4/kramerius4.db;create=true;user=kramerius4;password=kramerius4";
	public static final String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

}
