package cz.incad.kramerius.lp;

import java.io.File;
import java.util.Stack;

public class FileUtils {

	
	public static void deleteRecursive(File folder) {
		File[] listFiles = folder.listFiles();
		if (listFiles != null) {
			for (File file : listFiles) {
				if (file.isFile()) {
					file.delete();
				} else {
					deleteRecursive(file);
					file.delete();
				}
			}
		}
	}

	public static void deleteTMPFolder() {
		String property = System.getProperty("user.dir");
		File ffolder = new File(property+File.separator+"tmp");
		deleteRecursive(ffolder);
		ffolder.delete();
	}

	public static File createTMPFolder() {
		String property = System.getProperty("user.dir");
		File ffolder = new File(property+File.separator+"tmp");
		ffolder.mkdirs();
		return ffolder;
	}

}
