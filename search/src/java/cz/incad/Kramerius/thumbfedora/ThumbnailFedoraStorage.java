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

import com.google.inject.Inject;

import cz.incad.Kramerius.ThumbnailStorage;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Nahledy ulozene ve fedore
 * @author pavels
 */
public class ThumbnailFedoraStorage implements ThumbnailStorage {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ThumbnailFedoraStorage.class.getName());

	@Inject
	FedoraAccess fedoraAccess;
	@Inject
	KConfiguration configuration;
	
	private InputStream is;
	
	@Override
	public boolean checkExists(String uuid) {
		try {
			is = fedoraAccess.getThumbnail(uuid);
			return is!=null;
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return false;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return false;
		}
	}


	@Override
	public void redirectToServlet(String uuid, HttpServletResponse response) {
		try {
			if (this.is != null) {
				response.setContentType("image/jpeg");
				ServletOutputStream outputStream = response.getOutputStream();
				IOUtils.copyStreams(is, outputStream);
			}
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
			uploadThumbnailAsDatastream(configuration, "uuid:"+uuid, request);
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

	public KConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(KConfiguration configuration) {
		this.configuration = configuration;
	}


	public FedoraAccess getFedoraAccess() {
		return fedoraAccess;
	}


	public void setFedoraAccess(FedoraAccess fedoraAccess) {
		this.fedoraAccess = fedoraAccess;
	}
	
}
