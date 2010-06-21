package cz.incad.kramerius.lp;

import java.io.File;

public class FileUtils {

	public static void recursion(File folder) {
		File[] listFiles = folder.listFiles();
		if (listFiles != null) {
			for (File file : listFiles) {
				if (file.isFile()) {
					file.delete();
				} else {
					recursion(file);
					file.delete();
				}
			}
		}
	}

	public static void deleteTMPFolder() {
		String property = System.getProperty("user.dir");
		File ffolder = new File(property+File.separator+"tmp");
		recursion(ffolder);
		ffolder.delete();
	}

	public static File createTMPFolder() {
		String property = System.getProperty("user.dir");
		File ffolder = new File(property+File.separator+"tmp");
		ffolder.mkdirs();
		return ffolder;
	}

}
