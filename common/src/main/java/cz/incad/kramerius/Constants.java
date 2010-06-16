package cz.incad.kramerius;

import java.io.File;

public class Constants {

	public static final String WORKING_DIR=System.getProperty("user.home")+File.separator+".kramerius4";

	static {
		File dir = new File(WORKING_DIR);
		if (!dir.exists()) {
			boolean mkdirs = dir.mkdirs();
			if (!mkdirs) throw new RuntimeException("cannot crate dir '"+dir.getAbsolutePath()+"'");
		}
	}
}
