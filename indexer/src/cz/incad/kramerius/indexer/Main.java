/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.kramerius.indexer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import cz.incad.kramerius.utils.IOUtils;

/**
 * 
 * @author Administrator
 */
public class Main {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		try {
			System.out.println(Arrays.asList(args));
			ProgramArguments arguments = new ProgramArguments();
			if (!arguments.parse(args)) {
				System.out.println("Program arguments are invalid");
			}

			
			checkFileOrCreateNew(arguments.log4jFile, "res/log4j.properties");
			checkFileOrCreateNew(arguments.configFile,"res/config.properties");

			
			Indexer indexer = new Indexer(arguments);
			indexer.run();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex);
		}
	}


	private static void checkFileOrCreateNew(String log4jFile, String resPath) throws IOException {
		File file = new File(log4jFile);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
			InputStream resStream = null;
			try {
				FileOutputStream fos = new FileOutputStream(file);
				resStream = Main.class.getResourceAsStream(resPath);
				IOUtils.copyStreams(resStream, fos);
			} finally {
				if (resStream != null) {
					resStream.close();
				}
			}
		}
	}


	private static void createParentDirs(File file) {
		// TODO Auto-generated method stub
		
	}

}
