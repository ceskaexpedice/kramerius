package cz.incad.Kramerius.thumbfedora;

import static cz.incad.Kramerius.FedoraUtils.getThumbnailFromFedora;
import static cz.incad.utils.JNDIUtils.getJNDIValue;
import static cz.incad.utils.WSSupport.uploadThumbnailAsDatastream;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.i.kramerius.gwtviewers.server.pid.LexerException;
import cz.i.kramerius.gwtviewers.server.pid.PIDParser;
import cz.incad.Kramerius.ThumbnailStorage;
import cz.incad.utils.IOUtils;

/**
 * Nahledy ulozene ve fedore
 * @author pavels
 */
public class ThumbnailFedoraStorage implements ThumbnailStorage {

	// TODO! Dat do jndi
	private static final String FEDORA_ADMIN_USER_KEY="fedoraAdminUser";
	private static final String FEDORA_ADMIN_USER_PSWD="fedoraAdminPassword";

	private HttpURLConnection connection;

	@Override
	public boolean checkExists(String uuid) {
		try {
			HttpURLConnection con = createConnection(uuid);
			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				return true;
			} else return false;
		} catch (MalformedURLException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	private HttpURLConnection createConnection(String uuid) throws MalformedURLException,
			IOException {
		if (connection == null) {
			URL url = new URL(getThumbnailFromFedora(uuid));
			connection = (HttpURLConnection) url.openConnection();
		}
		return connection;
	}

	@Override
	public void redirectToServlet(String uuid, HttpServletResponse response) {
		try {
			HttpURLConnection con = createConnection(uuid);
			InputStream inputStream = con.getInputStream();
			response.setContentType("image/jpeg");
			ServletOutputStream outputStream = response.getOutputStream();
			IOUtils.copyStreams(inputStream, outputStream);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void uploadThumbnail(String uuid, HttpServletRequest request) {
		// fedora stream
		try {
			String adminName = getJNDIValue(FEDORA_ADMIN_USER_KEY);
			String adminPswd = getJNDIValue(FEDORA_ADMIN_USER_PSWD);
			uploadThumbnailAsDatastream(adminName, adminPswd, "uuid:"+uuid, request);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (LexerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
