package cz.incad.kramerius.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.google.gwt.user.server.Base64Utils;

import cz.incad.kramerius.utils.conf.KConfiguration;


/**
 * Umoznuje se dotazovat na fedoru, ktera potrebuje autentizaci
 * @author pavels
 */
public class RESTHelper {


	public static InputStream inputStream(String urlString, String user, String pass) throws IOException {
		URLConnection uc = openConnection(urlString, user, pass);
		return uc.getInputStream();
	}

	
	
	
	public static URLConnection openConnection(String urlString, String user,
			String pass) throws MalformedURLException, IOException {
		URL url = new URL(urlString);
		String userPassword = user + ":" + pass;
		String encoded = Base64Utils.toBase64(userPassword.getBytes()); 
		URLConnection uc = url.openConnection();
		uc.setReadTimeout(Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout","10000")));
		uc.setConnectTimeout(Integer.parseInt(KConfiguration.getInstance().getProperty("http.timeout","10000")));
		uc.setRequestProperty ("Authorization", "Basic " + encoded);
		return uc;
	}

}
