package cz.incad.kramerius.gwtviewers.server;

import static cz.incad.kramerius.gwtviewers.server.utils.UtilsDecorator.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.SAXException;


import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import cz.incad.kramerius.FedoraModels;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.gwtviewers.client.PageService;
import cz.incad.kramerius.gwtviewers.client.PagesResultSet;
import cz.incad.kramerius.gwtviewers.client.SimpleImageTO;
import cz.incad.kramerius.gwtviewers.client.panels.utils.Dimension;
import cz.incad.kramerius.gwtviewers.server.utils.CallCache;
import cz.incad.kramerius.gwtviewers.server.utils.MetadataStore;
import cz.incad.kramerius.gwtviewers.server.utils.ThumbnailServerUtils;
import cz.incad.kramerius.gwtviewers.server.utils.Utils;
import cz.incad.kramerius.gwtviewers.server.utils.UtilsDecorator;
import cz.incad.kramerius.utils.JNDIUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;


public class PageServiceImpl extends RemoteServiceServlet implements PageService {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(PageServiceImpl.class.getName());

	private KConfiguration kConfiguration;
	private MetadataStore metadataStore = new MetadataStore();

	public PagesResultSet getPages(String parentUUID, String selectedUUID) {
		PagesResultSet readPages = readPages(parentUUID, selectedUUID);
		return readPages;
	}
	
	
	
	
	@Override
	public PagesResultSet getPagesSet(String parentUUID, String selectedUUID) {
		PagesResultSet pgs = getPages(parentUUID, selectedUUID);
		return pgs;
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
	
	public static String thumbnail( String uuid, String scaledHeight, HttpServletRequest request) {
//		String url = KConfiguration.getKConfiguration().getThumbServletUrl()+"?outputFormat=RAW&uuid="+uuid;
		String url = currentURL(request)+"?outputFormat=RAW&uuid="+uuid;
		return url;
	}
	
	public static String currentURL(HttpServletRequest request) {
		//"dvju"
		try {
			URL url = new URL(request.getRequestURL().toString());
			String path = url.getPath();
			StringBuffer buffer = new StringBuffer();
			StringTokenizer tokenizer = new StringTokenizer(path,"/");
			if(tokenizer.hasMoreTokens()) { buffer.append(tokenizer.nextToken()); }

			String imagePath = url.getProtocol()+"://"+url.getHost()+":"+url.getPort()+"/"+buffer.toString()+"/thumb";

			return imagePath;
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return "<no url>";
		}
	}

	
	public PagesResultSet readPages(String parentUUID, String selectedUUID) {
		long start = System.currentTimeMillis();
		ArrayList<SimpleImageTO> images = null;
		//PagesResultSet pages = null;
		InputStream docStream = null;
		try {
			LOGGER.info("Current uuid:"+parentUUID);
			FedoraModels model = Utils.getModel(kConfiguration, parentUUID);
			//if (model.equals(FedoraModels.page)) continue;
			images = UtilsDecorator.getPages(kConfiguration,this.getThreadLocalRequest() , parentUUID); 
			
			if (images == null) images = new ArrayList<SimpleImageTO>();
			if (!images.isEmpty()) {
				for (int i = 0; i < images.size(); i++) {
					images.get(i).setIndex(i);
				}
				images.get(0).setFirstPage(true);
				images.get(images.size()-1).setLastPage(true);
			}
			
			LOGGER.info("Reading image sizes");
			Properties props = metadataStore.loadCollected(parentUUID);
            if (props.isEmpty()) {
    			LOGGER.info("Disecting image sizes");
                props = ThumbnailServerUtils.disectSizesOldStyle(parentUUID, images);
                metadataStore.storeCollected(parentUUID, props);
            }
            
//            String identification = uuidPath[uuidPath.length - 1];
            int index = -1;
            for (int i = 0; i < images.size(); i++) {
            	SimpleImageTO sit = images.get(i);
				String property = props.getProperty(sit.getIdentification());
				if (property == null) throw new RuntimeException("not width property !");
				sit.setWidth(Integer.parseInt(property));
				if (selectedUUID.equals(sit.getIdentification())) {
					index = i;
				}
            }
            PagesResultSet rs = new PagesResultSet();
            rs.setData(images);
            LOGGER.info("SIZE = "+rs.getData().size());
            rs.setCurrentSimpleImageTOId(selectedUUID);
            rs.setCurrentSimpleImageTOIndex(index);
            rs.setMasterSimpleImageTOId(parentUUID);
            LOGGER.info("_TIME == "+(System.currentTimeMillis() - start));
            return rs;
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
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	public Integer getNumberOfPages(String parentUUID, String selectedUUID) {
		LOGGER.info("getNumberOfPages("+parentUUID+","+selectedUUID+")");
		PagesResultSet pages = getPages(parentUUID, selectedUUID);
		LOGGER.info("PagesResultSet : "+pages.getData().size());
		return pages.getData().size();
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.kConfiguration = (KConfiguration) getServletContext().getAttribute("kconfig");
		try {
			if (kConfiguration == null) {
				//String configFile = JNDIUtils.getJNDIValue("configPath", System.getProperty("configPath"));
			    kConfiguration = KConfiguration.getKConfiguration();
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public SimpleImageTO getNoPage() {
		return null;
	}

}
