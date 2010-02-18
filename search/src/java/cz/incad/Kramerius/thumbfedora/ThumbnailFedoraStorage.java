package cz.incad.Kramerius.thumbfedora;

import static cz.incad.Kramerius.FedoraUtils.getThumbnailFromFedora;
import static cz.incad.utils.WSSupport.uploadThumbnailAsDatastream;
import static cz.incad.kramerius.utils.RESTHelper.*;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

import javax.management.RuntimeErrorException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.incad.Kramerius.ThumbnailStorage;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Nahledy ulozene ve fedore
 * @author pavels
 */
public class ThumbnailFedoraStorage implements ThumbnailStorage {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ThumbnailFedoraStorage.class.getName());
	
	private HttpURLConnection connection;

	@Override
	public boolean checkExists(String uuid) {
		try {
			KConfiguration configuration = KConfiguration.getKConfiguration();
			HttpURLConnection con = createConnection(configuration ,uuid);
			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				return true;
			} else return false;
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return false;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
	}

	private HttpURLConnection createConnection(KConfiguration configuration, String uuid) throws MalformedURLException,
			IOException {
		if (connection == null) {
			connection = (HttpURLConnection) openConnection(getThumbnailFromFedora(configuration ,uuid),configuration.getFedoraUser(), configuration.getFedoraPass());
		}
		return connection;
	}

	@Override
	public void redirectToServlet(String uuid, HttpServletResponse response) {
		try {
			KConfiguration configuration = KConfiguration.getKConfiguration();
			HttpURLConnection con = createConnection(configuration, uuid);
			InputStream inputStream = con.getInputStream();
			response.setContentType("image/jpeg");
			ServletOutputStream outputStream = response.getOutputStream();
			IOUtils.copyStreams(inputStream, outputStream);
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void uploadThumbnail(String uuid, HttpServletRequest request) {
		LOGGER.info("uploading to fedora");
		try {
			uploadThumbnailAsDatastream(KConfiguration.getKConfiguration(), "uuid:"+uuid, request);
		} catch (NoSuchAlgorithmException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} catch(Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	
}
