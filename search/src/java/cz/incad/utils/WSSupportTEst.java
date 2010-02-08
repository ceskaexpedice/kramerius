package cz.incad.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.gwt.user.server.Base64Utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.security.provider.MD5;

public class WSSupportTEst {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws NoSuchAlgorithmException, MalformedURLException, IOException {
		
		toBase64();
		
//		System.out.println(MessageDigest.getInstance("MD5").digest().length);
//		BASE64Encoder enc = new BASE64Encoder();
//		System.out.println(enc.encode(MessageDigest.getInstance("MD5").digest()));
		
//		BASE64Decoder dec = new BASE64Decoder();
//		byte[] decodeBuffer = dec.decodeBuffer("b947bf5d133a9ffa4b81727ba3fe9f5b");
//		System.out.println(decodeBuffer.length);
//		for (int i = 0; i < decodeBuffer.length; i++) {
//			System.out.println("\t "+decodeBuffer[i]);
//		}
//		
//		System.out.println(Base64Utils.toBase64(decodeBuffer));
	}

	
	
	private static void toBase64() throws IOException,
			NoSuchAlgorithmException, MalformedURLException {
		File f = new File(System.getProperty("user.home")+File.separator+"firstthumb.jpeg");
		System.out.println(f.getAbsolutePath());
		System.out.println(f.toURL().toString());
		String calcMD5SUM = WSSupport.calcMD5SUM(f.toURL().toString());
		System.out.println(calcMD5SUM);
		
	}
	
	
}
