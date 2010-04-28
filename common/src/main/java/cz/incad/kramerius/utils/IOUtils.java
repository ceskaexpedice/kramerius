package cz.incad.kramerius.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.logging.Level;

public class IOUtils {
	
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(IOUtils.class.getName());
	
	private IOUtils() {}
	
	/**
	 * Kopirovani ze vstupniho proudo do vystupniho
	 * @param is Vstupni proud
	 * @param os Vystupni proud
	 * @throws IOException
	 */
	public static void copyStreams(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[8192];
		int read = -1;
		while((read = is.read(buffer)) > 0) {
			os.write(buffer, 0, read);
		}
	}
	
	/**
	 * Kopiruje a pocita digest
	 * @param is Vstupni stream
	 * @param os Vystupni stream
	 * @param digest Digest
	 * @throws IOException
	 */
	public static void copyStreams(InputStream is, OutputStream os, MessageDigest digest) throws IOException {
		byte[] buffer = new byte[8192];
		int read = -1;
		while((read = is.read(buffer)) > 0) {
			os.write(buffer, 0, read);
			digest.update(buffer, 0, read);
		}
	}
	
	public static void copyFile(File src, File dst) throws IOException {
		LOGGER.info("Copying file '"+src.getAbsolutePath()+"' to '"+dst.getAbsolutePath()+"'");
		FileChannel in = null;
		FileChannel out = null;
		try {
			FileInputStream fis = new FileInputStream(src);
			in = fis.getChannel();
			FileOutputStream fos = new FileOutputStream(dst);
			out = fos.getChannel();
		    out.transferFrom(in, 0, in.size());
		} finally  {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getMessage(), e);
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
	}

}
