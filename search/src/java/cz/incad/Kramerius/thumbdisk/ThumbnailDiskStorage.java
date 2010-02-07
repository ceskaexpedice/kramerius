package cz.incad.Kramerius.thumbdisk;

import static cz.incad.utils.IOUtils.*;

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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cz.incad.Kramerius.ThumbnailServlet;
import cz.incad.Kramerius.ThumbnailStorage;
import cz.incad.utils.IOUtils;

public class ThumbnailDiskStorage implements ThumbnailStorage {

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			String rawContent = ThumbnailServlet.rawContent(uuid, request);
			URL url = new URL(rawContent);
			URLConnection con = url.openConnection();
			copyStreams(con.getInputStream(), fos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (fos != null)
				try { fos.close(); } catch (IOException e) { e.printStackTrace(); }
		}
	}

	
}
