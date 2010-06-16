package cz.incad.kramerius.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
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
	

	public static String readAsString(InputStream is, Charset charset, boolean closeInput) throws IOException {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			copyStreams(is, bos);
			return new String(bos.toByteArray(), charset);
		} finally  {
			if ((is != null) && closeInput) { is.close(); }
		}
	}
	

	public static void saveToFile(String data, File file) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(data.getBytes());
		} finally {
			if (fos != null) fos.close();
		}
	}
	

	public static ByteArrayInputStream bos(File inFile) throws IOException {
		InputStream is =  null;
		try {
			is = new FileInputStream(inFile);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			copyStreams(is, bos);
			return new ByteArrayInputStream(bos.toByteArray());
		}finally {
			if (is != null) is.close();
		}
		
	}
}
