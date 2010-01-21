package cz.i.kramerius.gwtviewers.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import cz.i.kramerius.gwtviewers.client.PageService;
import cz.i.kramerius.gwtviewers.client.SimpleImageTO;

public class PageServiceImpl extends RemoteServiceServlet implements
		PageService {

	private String imgFolder;

	private HashMap<Integer, String> pageMapping = new HashMap<Integer, String>(); {
		pageMapping.put(0, "uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc1");
		pageMapping.put(1, "uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc2");
		pageMapping.put(2, "uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc3");
		pageMapping.put(3, "uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc4");
		pageMapping.put(4, "uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc5");
		pageMapping.put(5, "uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc6");
		pageMapping.put(6, "uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc7");
		pageMapping.put(7, "uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc8");
	}

	// sample data
	// uuid_ce5c9630-a9b2-11dd-b265-000d606f5dcA.jpg
	// uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc1.jpg
	// uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc2.jpg
	// uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc3.jpg
	// uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc4.jpg
	// uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc5.jpg
	// uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc6.jpg
	// uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc7.jpg
	// uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc8.jpg
	// uuid_ce5c9630-a9b2-11dd-b265-000d606f5dc9.jpg

	public Integer getNumberOfPages(String uuid) {
		File folder = new File(this.imgFolder);
		File[] listFiles = folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				if ((pathname.getName().endsWith(".jpg"))
						&& (pathname.isFile())) {
					return true;
				} else return false;
			}
		});
		return listFiles.length;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.imgFolder = config.getInitParameter("imgFolder");
	}

	public SimpleImageTO getPage(String uuid, int index) {
		try {
			String imgUuid = digestUUID(index);
			return getPage(imgUuid);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private SimpleImageTO getPage(String imgUuid) throws IOException {
		String url = digestURL(imgUuid);
		
		InputStream is = readStream(imgUuid);
		BufferedImage read = ImageIO.read(is);
		int height = read.getHeight();
		int width = read.getWidth();

		SimpleImageTO ito = new SimpleImageTO(); {
			ito.setUrl(url);
			ito.setHeight(height);
			ito.setWidth(width);
		}
		return ito;
	}

	private String digestURL(String imgUuid) {
		return "data/"+imgUuid+".jpg";
	}

	private String digestUUID(int index) {
		return this.pageMapping.get(index);
	}

	private InputStream readStream(String imgUuid) {
		try {
			File imgFile = new File(this.imgFolder, imgUuid+".jpg");
			return new FileInputStream(imgFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("illegal uuid "+imgUuid);
		}
	}

	public List<String> getPagesUUIDs(String masterUuid) {
		String[] list = new File(this.imgFolder).list();
		List<String> uuids = new ArrayList<String>();
		for (String uuid : list) {
			uuids.add(uuid);
		}
		return uuids;
	}

	public String getUUId(String masterUuid, int index) {
		Set<Integer> keySet = pageMapping.keySet();
		for (Integer key : keySet) {
			if (key == index) return pageMapping.get(key);
		}
		return null;
	}

	@Override
	public SimpleImageTO getNoPage() {
		
		return null;
	}

	@Override
	public ArrayList<SimpleImageTO> getPagesSet(String masterUuid) {
		try {
			List<String> pagesUUIDs = getPagesUUIDs("any-uuid");
			ArrayList<SimpleImageTO> itos = new ArrayList<SimpleImageTO>();
			for (String string : pagesUUIDs) {
				string = string.substring(0, string.length()-4);
				itos.add(getPage(string));
			}
			return itos;
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	
}
