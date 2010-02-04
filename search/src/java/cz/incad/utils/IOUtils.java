package cz.incad.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IOUtils {
	
	
	public static void copyStreams(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[4098];
		int read = 1;
		while((read = is.read(buffer)) > 0) {
			os.write(buffer, 0, read);
		}
	}
}
