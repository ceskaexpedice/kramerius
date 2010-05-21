package cz.incad.kramerius.ngwt.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraModels;
import cz.incad.kramerius.ngwt.client.ImageMetadata;
import cz.incad.kramerius.ngwt.client.PageService;
import cz.incad.kramerius.ngwt.client.PagesResultSet;
import cz.incad.kramerius.utils.pid.LexerException;


public class PageServiceImpl extends RemoteServiceServlet implements PageService {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(PageServiceImpl.class.getName());
	
	private int distance = 5;

	@Inject
	@Named("rawFedoraAccess")
	private FedoraAccess fedoraAccess;
	
	@Override
	public void init() throws ServletException {
		super.init();
		Injector injector = getInjector();
		injector.injectMembers(this);
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		Injector injector = getInjector();
		injector.injectMembers(this);
	}

	protected Injector getInjector() {
		return (Injector) getServletContext().getAttribute(Injector.class.getName());
	}

	
	@Override
	public ImageMetadata getNoPage() {
		return null;
	}

	@Override
	public Integer getNumberOfPages(String masterUuid, String selection) {
		return null;
	}

	public static String thumbnail( String uuid, String scaledHeight, HttpServletRequest request) {
//		String url = KConfiguration.getKConfiguration().getThumbServletUrl()+"?outputFormat=RAW&uuid="+uuid;
		String url = currentURL(request)+"?outputFormat=RAW&uuid="+uuid;
		return url;
	}
	
	public static String currentURL(HttpServletRequest request) {
		return "http://192.168.56.1:8080/search/thumb";
//		try {
//			URL url = new URL(request.getRequestURL().toString());
//			String path = url.getPath();
//			StringBuffer buffer = new StringBuffer();
//			StringTokenizer tokenizer = new StringTokenizer(path,"/");
//			if(tokenizer.hasMoreTokens()) { buffer.append(tokenizer.nextToken()); }
//			String imagePath = url.getProtocol()+"://"+url.getHost()+":"+url.getPort()+"/"+buffer.toString()+"/thumb";
//			return imagePath;
//		} catch (MalformedURLException e) {
//			LOGGER.log(Level.SEVERE, e.getMessage(), e);
//			return "<no url>";
//		}
	}

	@Override
	public PagesResultSet getPagesSet(String masterUuid, String selection) {
		try {
			PagesResultSet resultSet = new PagesResultSet();
			long relsExtStart = System.currentTimeMillis();
			ArrayList<ImageMetadata> pages = Utils.getPages(this.fedoraAccess, this.getThreadLocalRequest(), masterUuid);
			long relsExtEnd = System.currentTimeMillis();
			LOGGER.info("rels ext takes "+(relsExtEnd - relsExtStart)+" ms ");
			HashMap<Integer, Integer> index2position = new HashMap<Integer, Integer>();
			int offset = distance;
			int sumWidth = 0;
			
//			int parts = pages.size() / 3;
//			CyclicBarrier barrier = new CyclicBarrier(4);

			long imgStarts = System.currentTimeMillis();
			disectSizes(resultSet, pages, index2position, offset, sumWidth);
			long imgStop = System.currentTimeMillis();
			LOGGER.info("imgs takes "+(imgStop - imgStarts)+" ms ");
			
			return resultSet;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (LexerException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	private void disectSizes(PagesResultSet resultSet,ArrayList<ImageMetadata> pages,HashMap<Integer, Integer> index2position, int offset, int sumWidth)throws IOException {
		for (int i = 0; i < pages. size(); i++) {
			ImageMetadata mtd = pages.get(i);
			
			BufferedImage read = ImageIO.read(this.fedoraAccess.getThumbnail(mtd.getIdentification()));
			int width = read.getWidth();
			int height = read.getHeight();
			
			mtd.setFirstPage(i==0);
			mtd.setLastPage(i==pages.size() -1);
			mtd.setHeight(height);
			mtd.setWidth(width);
			mtd.setIndex(i);
			mtd.setOffset(offset);
			index2position.put(i, offset);
			
			sumWidth += width;
			offset += width+distance;
		}
		
		resultSet.setData(pages);
		resultSet.setWidth(offset);
		resultSet.setIndexToPositionMapping(index2position);
		resultSet.setAvgWidth(sumWidth / pages.size());
	}


	public FedoraAccess getFedoraAccess() {
		return fedoraAccess;
	}

	public void setFedoraAccess(FedoraAccess fedoraAccess) {
		this.fedoraAccess = fedoraAccess;
	}
	
	class Worker extends Thread {

		List<ImageMetadata> mtds;
		int from;
		int to;

		public Worker(List<ImageMetadata> mtds, int from, int to) {
			super();
			this.mtds = mtds;
			this.from = from;
			this.to = to;
		}




		@Override
		public void run() {
			
		}
	}
}
