package cz.incad.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;

public class IOUtils {
	
	
	public static void copyStreams(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[8192];
		int read = -1;
		while((read = is.read(buffer)) > 0) {
			os.write(buffer, 0, read);
		}
	}
	
	public static void copyStreams(InputStream is, OutputStream os, MessageDigest digest) throws IOException {
		byte[] buffer = new byte[8192];
		int read = -1;
		while((read = is.read(buffer)) > 0) {
			os.write(buffer, 0, read);
			digest.update(buffer, 0, read);
		}
	}
	
}
