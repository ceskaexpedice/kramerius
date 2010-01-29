package cz.i.kramerius.gwtviewers.server;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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


public class PageServiceImpl extends RemoteServiceServlet implements PageService {

    public static String fedoraUrl = "http://194.108.215.227:8080/fedora";
	public static String defaultScale ="0.3";

    private String thumbnailUrl ="thumb";

	private String imgFolder;
	

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
		return fedoraUrl +"/get/uuid:"+uuid+"/RELS-EXT";
	}
	
	public static String thumbnail(String thumbUrl, String uuid, String scale) {
		return thumbUrl+"?scale="+scale+"&uuid="+uuid;
	}
	
	
	public ArrayList<SimpleImageTO> readPage(String uuid) {
		ArrayList<SimpleImageTO> pages = new ArrayList<SimpleImageTO>();
		try {
			URL url = new URL(relsExtUrl(uuid));
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
				StringTokenizer tokenizer = new StringTokenizer(attribute,"/");
				if (tokenizer.hasMoreTokens()) {
					// CUNARNA - > parser dle EBNF - viz http://www.fedora-commons.org/confluence/display/FCR30/Fedora+Identifiers
					String infoPart = tokenizer.nextToken();
					String uuidPart = "";
					if (tokenizer.hasMoreTokens()) uuidPart = tokenizer.nextToken(); 
					uuidPart = uuidPart.substring(uuidPart.indexOf(':')+1);

					SimpleImageTO imageTO = new SimpleImageTO();
					imageTO.setFirstPage(i==0);
					imageTO.setLastPage(i==lastIndex);
					imageTO.setIdentification(uuidPart);
					imageTO.setUrl(thumbnail(this.thumbnailUrl, uuidPart, defaultScale));
					// jak to dostat aniz bych to musel zase cist 
					imageTO.setWidth(142);
					imageTO.setHeight(200);
					
					pages.add(imageTO);
				}
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
		}
		return pages;
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
