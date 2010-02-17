package cz.i.kramerius.gwtviewers.server;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import sun.nio.cs.KOI8_R;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import cz.i.kramerius.gwtviewers.client.PageService;
import cz.i.kramerius.gwtviewers.client.SimpleImageTO;
import cz.i.kramerius.gwtviewers.client.panels.utils.Dimension;
import cz.incad.kramerius.utils.JNDIUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;


public class PageServiceImpl extends RemoteServiceServlet implements PageService {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PageServiceImpl.class.getName());

    private String thumbnailUrl ="thumb";
	private String imgFolder;
	private String scaledHeight;
	private String maxInitWidth;
	
	
	private KConfiguration kConfiguration;
	
	// dodelat nejakou vlastni implementaci !!
	private WeakHashMap<String, ArrayList<SimpleImageTO>> cachedPages = new WeakHashMap<String, ArrayList<SimpleImageTO>>();

	// 
	public ArrayList<SimpleImageTO> getPages(String masterUuid) {
		if (!this.cachedPages.containsKey(masterUuid)) {
			this.cachedPages.put(masterUuid, readPage(masterUuid));
		}
		return this.cachedPages.get(masterUuid);
	}
	
	public static String relsExtUrl(KConfiguration configuration, String uuid) {
		String url = configuration.getFedoraHost() +"/get/uuid:"+uuid+"/RELS-EXT";
		return url;
	}
	
	public static String homeExtUrl(String uuid) {
		String url = "file:///home/pavels/RELS-EXT";
		return url;
	}
	
	public static String homeExtUrlBig(String uuid) {
		String url = "file:///home/pavels/Plocha/RELS-EXT.full.xml";
		return url;
	}
	
	
	public static String thumbnail(String thumbUrl, String uuid, String scaledHeight) {
		String url = KConfiguration.getKConfiguration().getThumbServletUrl()+"?scaledHeight="+scaledHeight+"&uuid="+uuid;
		return url;
	}
	
	
	public ArrayList<SimpleImageTO> readPage(String uuid) {
		ArrayList<SimpleImageTO> pages = new ArrayList<SimpleImageTO>();
		InputStream docStream = null;
		try {
			//URL url = new URL(relsExtUrl(uuid));
			docStream = RESTHelper.inputStream(relsExtUrl(kConfiguration, uuid), kConfiguration.getFedoraUser(), kConfiguration.getFedoraPass());
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document parsed = builder.parse(docStream);

			XPathFactory xpfactory = XPathFactory.newInstance();
            XPath xpath = xpfactory.newXPath();
            String xPathStr = "/RDF/Description/hasPage";
            XPathExpression expr = xpath.compile(xPathStr);
            NodeList nodes = (NodeList) expr.evaluate(parsed, XPathConstants.NODESET);
            for (int i = 0,lastIndex=nodes.getLength()-1; i <= lastIndex; i++) {
				Element elm = (Element) nodes.item(i);
				//info:fedora/uuid:4308eb80-b03b-11dd-a0f6-000d606f5dc6
				String attribute = elm.getAttribute("rdf:resource");
				PIDParser pidParser= new PIDParser(attribute);
				pidParser.disseminationURI();
				String objectId = pidParser.getObjectId();
				
				SimpleImageTO imageTO = new SimpleImageTO();
				imageTO.setFirstPage(i==0);
				imageTO.setLastPage(i==lastIndex);
				imageTO.setIdentification(objectId);
				String thumbnailURL = thumbnail(this.thumbnailUrl, objectId, this.scaledHeight);
				imageTO.setUrl(thumbnailURL);
				imageTO.setHeight(Integer.parseInt(scaledHeight));
				imageTO.setWidth(Integer.parseInt(maxInitWidth));
				imageTO.setIndex(i);
				pages.add(imageTO);
            }
			
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (XPathExpressionException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		} catch (LexerException e) {
			throw new RuntimeException(e);
		}
		return pages;
	}

	//TODO: Zjisteni velikosti... nic vic.
	private Image readThumbnail(String thumbnailURL) {
		try {
			URL url = new URL(thumbnailURL);
			URLConnection connection = url.openConnection();
			return ImageIO.read(connection.getInputStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
	}
	
	public Integer getNumberOfPages(String uuid) {
		List<SimpleImageTO> pages = getPages(uuid);
		return pages.size();	
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.kConfiguration = (KConfiguration) getServletContext().getAttribute("kconfig");
		try {
			if (kConfiguration == null) {
				String configFile = JNDIUtils.getJNDIValue("configPath", System.getProperty("configPath"));
			    kConfiguration = KConfiguration.getKConfiguration(configFile);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
		
		this.imgFolder = config.getInitParameter("imgFolder");
		this.thumbnailUrl = config.getInitParameter("thumbUrl");
		this.scaledHeight = KConfiguration.getKConfiguration().getScaledHeight();
		this.maxInitWidth = config.getInitParameter("maxWidth");
	}

	public SimpleImageTO getPage(String uuid, int index) {
		throw new UnsupportedOperationException();
	}

	private SimpleImageTO getPage(String imgUuid) throws IOException {
		throw new UnsupportedOperationException();
	}


	public List<String> getPagesUUIDs(String masterUuid) {
		String[] list = new File(this.imgFolder).list();
		List<String> uuids = new ArrayList<String>();
		for (String uuid : list) {
			uuids.add(uuid);
		}
		return uuids;
	}


	
	
	@Override
	public String getUUId(String masterUuid, int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimpleImageTO getNoPage() {
		
		return null;
	}

	@Override
	public ArrayList<SimpleImageTO> getPagesSet(String masterUuid) {
		ArrayList<SimpleImageTO> pgs = getPages(masterUuid);
		return pgs;
	}

	
}
