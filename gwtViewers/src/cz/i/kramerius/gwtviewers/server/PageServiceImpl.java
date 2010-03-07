package cz.i.kramerius.gwtviewers.server;

import static cz.i.kramerius.gwtviewers.server.utils.UtilsDecorator.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.SAXException;


import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import cz.i.kramerius.gwtviewers.client.PageService;
import cz.i.kramerius.gwtviewers.client.SimpleImageTO;
import cz.i.kramerius.gwtviewers.client.panels.utils.Dimension;
import cz.i.kramerius.gwtviewers.server.utils.MetadataStore;
import cz.i.kramerius.gwtviewers.server.utils.ThumbnailServerUtils;
import cz.i.kramerius.gwtviewers.server.utils.Utils;
import cz.i.kramerius.gwtviewers.server.utils.UtilsDecorator;
import cz.incad.kramerius.FedoraModels;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.utils.JNDIUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;


public class PageServiceImpl extends RemoteServiceServlet implements PageService {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PageServiceImpl.class.getName());

	
    private String thumbnailUrl ="thumb";
	private String imgFolder;
	private String scaledHeight;
	private String maxInitWidth;
	
	
	private KConfiguration kConfiguration;
	
	// dodelat nejakou vlastni implementaci !!
	//private WeakHashMap<String, ArrayList<SimpleImageTO>> cachedPages = new WeakHashMap<String, ArrayList<SimpleImageTO>>();
	private MetadataStore metadataStore = new MetadataStore();
	
	
	// 
	public ArrayList<SimpleImageTO> getPages(String pidpath) {
		String[] path = pidpath.split("/");
		LOGGER.info("path = "+Arrays.asList(path));
		return readPages(path);
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
	
	public ArrayList<SimpleImageTO> readPages(String[] uuidPath) {
		System.out.println("Reading pages ... ");
		ArrayList<SimpleImageTO> pages = null;
		InputStream docStream = null;
		try {
			Stack<String> uuidStack = new Stack<String>();
			for (String uuid : uuidPath) { uuidStack.push(uuid); }
			String currentProcessinguuid = null;
			while(!uuidStack.isEmpty()) {
				currentProcessinguuid = uuidStack.pop();
				LOGGER.info("Current uuid:"+currentProcessinguuid);
				FedoraModels model = Utils.getModel(kConfiguration, currentProcessinguuid);
				if (model.equals(FedoraModels.page)) continue;
				pages = UtilsDecorator.getPages(kConfiguration, currentProcessinguuid); 
			}
			
			if (pages == null) pages = new ArrayList<SimpleImageTO>();
			if (!pages.isEmpty()) {
				// nastaveni indexu
				for (int i = 0; i < pages.size(); i++) {
					pages.get(i).setIndex(i);
				}
				// nastaveni priznaku
				pages.get(0).setFirstPage(true);
				pages.get(pages.size()-1).setLastPage(true);
			}
			
			LOGGER.info("Reading image sizes");
			Properties props = metadataStore.loadCollected(currentProcessinguuid);
            /*if (props.isEmpty()) */ {
    			LOGGER.info("Disecting image sizes");
                props = ThumbnailServerUtils.disectSizes(currentProcessinguuid, pages);
                metadataStore.storeCollected(currentProcessinguuid, props);
            }
            for (SimpleImageTO sit : pages) {
				String property = props.getProperty(sit.getIdentification());
				if (property == null) throw new RuntimeException("not width property !");
				sit.setWidth(Integer.parseInt(property));
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
		} catch (LexerException e) {
			throw new RuntimeException(e);
		}
		return pages;
	}

	public Integer getNumberOfPages(String pidpath) {
		LOGGER.info("Get number of images "+pidpath);
		List<SimpleImageTO> pages = getPages(pidpath);
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
	public ArrayList<SimpleImageTO> getPagesSet(String uuidPath) {
		ArrayList<SimpleImageTO> pgs = getPages(uuidPath);
		return pgs;
	}
}
