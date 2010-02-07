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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import cz.i.kramerius.gwtviewers.client.PageService;
import cz.i.kramerius.gwtviewers.client.SimpleImageTO;
import cz.i.kramerius.gwtviewers.client.panels.utils.Dimension;
import cz.i.kramerius.gwtviewers.server.pid.LexerException;
import cz.i.kramerius.gwtviewers.server.pid.PIDParser;


public class PageServiceImpl extends RemoteServiceServlet implements PageService {

    public static String FEDORA_URL = "http://194.108.215.227:8080/fedora";
	public static String DEFAULT_SCALE_PERCENTAGE ="0.3";
	public static String DEFAULT_SCALE_HEIGHT ="220";

    private String thumbnailUrl ="thumb";
	private String imgFolder;
	private String scaledHeight;
	private String maxInitWidth;
	

	// dodelat nejakou vlastni implementaci !!
	private WeakHashMap<String, ArrayList<SimpleImageTO>> cachedPages = new WeakHashMap<String, ArrayList<SimpleImageTO>>();

	// 
	public ArrayList<SimpleImageTO> getPages(String masterUuid) {
		if (!this.cachedPages.containsKey(masterUuid)) {
			this.cachedPages.put(masterUuid, readPage(masterUuid));
		}
		return this.cachedPages.get(masterUuid);
	}
	
	public static String relsExtUrl(String uuid) {
		String url = FEDORA_URL +"/get/uuid:"+uuid+"/RELS-EXT";
		System.out.println(url);
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
		String url = thumbUrl+"?scaledHeight="+scaledHeight+"&uuid="+uuid;
		return url;
	}
	
	
	public ArrayList<SimpleImageTO> readPage(String uuid) {
		ArrayList<SimpleImageTO> pages = new ArrayList<SimpleImageTO>();
		try {
			URL url = new URL(relsExtUrl(uuid));
//			if (uuid.equals("8f526130-8b0d-11de-8994-000d606f5dc6")) {
//				url = new URL(homeExtUrl(uuid));
//			} else {
//				url = new URL(homeExtUrlBig(uuid));
//			}
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document parsed = builder.parse(url.openStream());

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
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (LexerException e) {
			e.printStackTrace();
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
		this.imgFolder = config.getInitParameter("imgFolder");
		this.thumbnailUrl = config.getInitParameter("thumbUrl");
		this.scaledHeight = config.getInitParameter("scaledHeight");
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
