package cz.incad.kramerius.lp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

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
		File ffolder = new File(property + File.separator + "tmp");
		deleteRecursive(ffolder);
		ffolder.delete();
	}

	public static File createTMPFolder() {
		String property = System.getProperty("user.dir");
		File ffolder = new File(property + File.separator + "tmp");
		ffolder.mkdirs();
		return ffolder;
	}

	public static void copyFile(File sourceFile, File destFile)
			throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

}
