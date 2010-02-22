package cz.incad.Kramerius.thumbdisk;

import static cz.incad.kramerius.utils.IOUtils.*;
import static cz.incad.kramerius.utils.RESTHelper.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

import cz.incad.Kramerius.ThumbnailServlet;
import cz.incad.Kramerius.ThumbnailStorage;
import cz.incad.Kramerius.thumbfedora.ThumbnailFedoraStorage;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.utils.WSSupport;

public class ThumbnailDiskStorage implements ThumbnailStorage {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(ThumbnailDiskStorage.class.getName());

	@Inject
	KConfiguration configuration;
	
	private File imgFolder() {
		File f = new File(System.getProperty("user.dir")+File.separator+"thmbs");
		if (!f.exists()) f.mkdirs();
		return f;
	}
	private File imgFile(String uuid) {
		return new File(imgFolder(), uuid+".jpeg");
	}
	@Override
	public boolean checkExists(String uuid) {
		return imgFile(uuid).exists();
	}

	@Override
	public void redirectToServlet(String uuid, HttpServletResponse response) {
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(imgFile(uuid));
			response.setContentType("image/jpeg");
			ServletOutputStream outputStream = response.getOutputStream();
			IOUtils.copyStreams(inputStream, outputStream);
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			if (inputStream != null)
				try { inputStream.close(); } catch (IOException e) { e.printStackTrace(); }
		} 

	}

	@Override
	public void uploadThumbnail(String uuid, HttpServletRequest request) {
		FileOutputStream fos = null;
		try {
			File file = imgFile(uuid);
			fos = new FileOutputStream(file);
			KConfiguration configuration = KConfiguration.getKConfiguration();
			String rawContent = WSSupport.rawImage(configuration, uuid, request);
			URLConnection con = openConnection(rawContent, configuration.getFedoraUser(), configuration.getFedoraPass());
			copyStreams(con.getInputStream(), fos);
		} catch (FileNotFoundException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}finally {
			if (fos != null)
				try { fos.close(); } catch (IOException e) { e.printStackTrace(); }
		}
	}
	public KConfiguration getConfiguration() {
		return configuration;
	}
	public void setConfiguration(KConfiguration configuration) {
		this.configuration = configuration;
	}

	
	
}
