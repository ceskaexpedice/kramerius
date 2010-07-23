package cz.incad.kramerius.impl;

import static cz.incad.kramerius.utils.FedoraUtils.getDjVuImage;
import static cz.incad.kramerius.utils.FedoraUtils.getThumbnailFromFedora;
import static cz.incad.kramerius.utils.RESTHelper.openConnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.BindingProvider;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.fedora.api.FedoraAPIA;
import org.fedora.api.FedoraAPIAService;
import org.fedora.api.FedoraAPIM;
import org.fedora.api.FedoraAPIMService;
import org.fedora.api.ObjectFactory;
import org.fedora.api.RelationshipTuple;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.FedoraRelationship;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.RelsExtHandler;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

/**
 * Default implementation of fedoraAccess
 * @see FedoraAccess
 * @author pavels
 */
public class FedoraAccessImpl implements FedoraAccess {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(FedoraAccessImpl.class.getName());
	
	private final KConfiguration configuration;
	
	@Inject
	public FedoraAccessImpl(KConfiguration configuration) {
		super();
		this.configuration = configuration;
	}

	@Override
	public List<Element> getPages(String uuid, boolean deep) throws IOException {
		Document relsExt = getRelsExt(uuid);
		return getPages(uuid, relsExt.getDocumentElement());
	}
	
	@Override
	public Document getRelsExt(String uuid) throws IOException {
		String relsExtUrl = relsExtUrl(KConfiguration.getInstance(), uuid);
		LOGGER.info("Reading rels ext +"+relsExtUrl);
		InputStream docStream = RESTHelper.inputStream(relsExtUrl, KConfiguration.getInstance().getFedoraUser(), KConfiguration.getInstance().getFedoraPass());
		try {
			return XMLUtils.parseDocument(docStream, true);
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE,e.getMessage(), e);
			throw new IOException(e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE,e.getMessage(), e);
			throw new IOException(e);
		}
	}

	
	
	@Override
	public KrameriusModels getKrameriusModel(Document relsExt) {
		try {
			Element foundElement = XMLUtils.findElement(relsExt.getDocumentElement(), "hasModel", FedoraNamespaces.FEDORA_MODELS_URI);
			if (foundElement != null) {
				String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
				PIDParser pidParser = new PIDParser(sform);
				pidParser.disseminationURI();
				KrameriusModels model = KrameriusModels.parseString(pidParser.getObjectId());
				return model;
			} else throw new IllegalArgumentException("cannot find model of ");
		} catch (DOMException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IllegalArgumentException(e);
		} catch (LexerException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public KrameriusModels getKrameriusModel(String uuid) throws IOException {
		return getKrameriusModel(getRelsExt(uuid));
	}	

	@Override
	public Document getBiblioMods(String uuid) throws IOException {
		String biblioModsUrl = biblioMods(KConfiguration.getInstance(), uuid);
		LOGGER.info("Reading bibliomods +"+biblioModsUrl);
		InputStream docStream = RESTHelper.inputStream(biblioModsUrl, KConfiguration.getInstance().getFedoraUser(), KConfiguration.getInstance().getFedoraPass());
		try {
			return XMLUtils.parseDocument(docStream, true);
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		}	
	}

	@Override
	public Document getDC(String uuid) throws IOException {
		String dcUrl = dc(KConfiguration.getInstance(), uuid);
		LOGGER.info("Reading dc +"+dcUrl);
		InputStream docStream = RESTHelper.inputStream(dcUrl, KConfiguration.getInstance().getFedoraUser(), KConfiguration.getInstance().getFedoraPass());
		try {
			return XMLUtils.parseDocument(docStream, true);
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		}	
	}

	boolean processRelsExtInternal(Element topElem, RelsExtHandler handler, int level) throws IOException, LexerException{
		boolean breakProcess = false;
		String namespaceURI = topElem.getNamespaceURI();
		if (namespaceURI.equals(FedoraNamespaces.ONTOLOGY_RELATIONSHIP_NAMESPACE_URI)) {
			String nodeName = topElem.getLocalName();
			FedoraRelationship relation = FedoraRelationship.findRelation(nodeName);
			if (relation != null) {
				if (handler.accept(relation)) {
					handler.handle(topElem, relation, level);
					if (handler.breakProcess()) return true;

					// deep
					String attVal = topElem.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
					PIDParser pidParser = new PIDParser(attVal);
					pidParser.disseminationURI();
					String objectId = pidParser.getObjectId();
					//LOGGER.info("processing uuid =" +objectId);
					Document relsExt = getRelsExt(objectId);
					breakProcess = processRelsExtInternal(relsExt.getDocumentElement(), handler, level+1);
				}
			} else {
				LOGGER.severe("Unsupported type of relation '"+nodeName+"'");
			}
			
			if (breakProcess) {
				LOGGER.info("Process has been borken");
				return breakProcess;
			}
			NodeList childNodes = topElem.getChildNodes();
			for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
				Node item = childNodes.item(i);
				if (item.getNodeType() == Node.ELEMENT_NODE) {
					breakProcess = processRelsExtInternal((Element) item, handler, level);
					if (breakProcess) break;
				}
			}
		} else if (namespaceURI.equals(FedoraNamespaces.RDF_NAMESPACE_URI)) {
			NodeList childNodes = topElem.getChildNodes();
			for (int i = 0,ll=childNodes.getLength(); i < ll; i++) {
				Node item = childNodes.item(i);
				if (item.getNodeType() == Node.ELEMENT_NODE) {
					breakProcess = processRelsExtInternal((Element) item, handler, level);
					if (breakProcess) break;
				}
			}
		}
		return breakProcess;
		
	}
	
	@Override
	public void processRelsExt(Document relsExtDocument, RelsExtHandler handler) throws IOException{
		try {
			processRelsExtInternal(relsExtDocument.getDocumentElement(), handler, 1);
		} catch (DOMException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		} catch (LexerException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		}
	}

	@Override
	public void processRelsExt(String uuid, RelsExtHandler handler) throws IOException {
		LOGGER.info("processing uuid =" +uuid);
		processRelsExt(getRelsExt(uuid), handler);
	}

	@Override
	public List<Element> getPages(String uuid, Element rootElementOfRelsExt)
			throws IOException {
		try {
			ArrayList<Element> elms = new ArrayList<Element>();
			String xPathStr = "/RDF/Description/hasPage";
			XPathFactory xpfactory = XPathFactory.newInstance();
			XPath xpath = xpfactory.newXPath();
			XPathExpression expr = xpath.compile(xPathStr);
			NodeList nodes = (NodeList) expr.evaluate(rootElementOfRelsExt, XPathConstants.NODESET);
			for (int i = 0,lastIndex=nodes.getLength()-1; i <= lastIndex; i++) {
				Element elm = (Element) nodes.item(i);
				elms.add(elm);
			}
			return elms;
		} catch (XPathExpressionException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		}
	}


	@Override
	public InputStream getThumbnail(String uuid) throws IOException {
		HttpURLConnection con = (HttpURLConnection) openConnection(getThumbnailFromFedora(configuration ,uuid),configuration.getFedoraUser(), configuration.getFedoraPass());
		InputStream thumbInputStream = con.getInputStream();
		return thumbInputStream;
	}

	@Override
	public Document getThumbnailProfile(String uuid) throws IOException {
		HttpURLConnection con = (HttpURLConnection) openConnection(thumbImageProfile(configuration ,uuid),configuration.getFedoraUser(), configuration.getFedoraPass());
		InputStream stream = con.getInputStream();
		try {
			return XMLUtils.parseDocument(stream, true);
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		}	
	}
	
	
	@Override
	public String getThumbnailMimeType(String uuid) throws IOException,XPathExpressionException {
		Document profileDoc = getThumbnailProfile(uuid);
        return mimetypeFromProfile(profileDoc);
	}

	public InputStream getImageFULL(String uuid) throws IOException {
		HttpURLConnection con = (HttpURLConnection) openConnection(getDjVuImage(configuration ,uuid),configuration.getFedoraUser(), configuration.getFedoraPass());
		con.connect();
		if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
			InputStream thumbInputStream = con.getInputStream();
			return thumbInputStream;
		} throw new IOException("404");
	}

	
	@Override
	public boolean isImageFULLAvailable(String uuid) throws IOException {
		HttpURLConnection con = (HttpURLConnection) openConnection(getDjVuImage(configuration ,uuid),configuration.getFedoraUser(), configuration.getFedoraPass());
		con.connect();
		try {
			return con.getResponseCode() == HttpURLConnection.HTTP_OK;
		} finally {
			con.disconnect();
		}
	}

	
	public String getImageFULLMimeType(String uuid) throws IOException, XPathExpressionException {
		Document profileDoc = getImageFULLProfile(uuid);
        return mimetypeFromProfile(profileDoc);
	}

	
	private String mimetypeFromProfile(Document profileDoc)
			throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile("/datastreamProfile/dsMIME");
        Node oneNode = (Node) expr.evaluate(profileDoc, XPathConstants.NODE);
        if (oneNode != null) {
        	Element elm = (Element) oneNode;
        	String mimeType = elm.getTextContent();
        	if ((mimeType != null) && (!mimeType.trim().equals(""))) {
            	mimeType = mimeType.trim();
            	return mimeType;
        	}
        }
        return null;
	}
	
	
	@Override
	public Document getImageFULLProfile(String uuid) throws IOException {
		HttpURLConnection con = (HttpURLConnection) openConnection(fullImageProfile(configuration ,uuid),configuration.getFedoraUser(), configuration.getFedoraPass());
		InputStream stream = con.getInputStream();
		try {
			return XMLUtils.parseDocument(stream, true);
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		}	
	}
	

	public static String fullImageProfile(KConfiguration configuration, String uuid) {
		return dsProfile(configuration, "IMG_FULL", uuid);
	}

	public static String thumbImageProfile(KConfiguration configuration, String uuid) {
		return dsProfile(configuration, "IMG_THUMB", uuid);
	}

	public static String dcProfile(KConfiguration configuration, String uuid) {
		return dsProfile(configuration, "DC", uuid);
	}
	public static String biblioModsProfile(KConfiguration configuration, String uuid) {
		return dsProfile(configuration, "BIBLIO_MODS", uuid);
	}

	public static String relsExtProfile(KConfiguration configuration, String uuid) {
		return dsProfile(configuration, "RELS-EXT", uuid);
	}
	
	public static String dsProfile(KConfiguration configuration, String ds, String uuid) {
		String fedoraObject = configuration.getFedoraHost() +"/objects/uuid:"+uuid;
		return fedoraObject + "/datastreams/"+ds+"?format=text/xml";
	}

	public static String dsProfileForPid(KConfiguration configuration, String ds, String pid) {
		String fedoraObject = configuration.getFedoraHost() +"/objects/"+pid;
		return fedoraObject + "/datastreams/"+ds+"?format=text/xml";
	}

	
	public static String biblioMods(KConfiguration configuration, String uuid) {
		String fedoraObject = configuration.getFedoraHost() +"/get/uuid:"+uuid;
		return fedoraObject + "/BIBLIO_MODS";
	}

	public static String dc(KConfiguration configuration, String uuid) {
		String fedoraObject = configuration.getFedoraHost() +"/get/uuid:"+uuid;
		return fedoraObject + "/DC";
	}

	public static String relsExtUrl(KConfiguration configuration, String uuid) {
		String url = configuration.getFedoraHost() +"/get/uuid:"+uuid+"/RELS-EXT";
		return url;
	}
	
	private FedoraAPIM APIMport;
    private FedoraAPIA APIAport;
    
    private ObjectFactory of;
    
    public FedoraAPIA getAPIA(){
        if (APIAport == null){
            initAPIA();
        }
        return APIAport;
    }
    
    public FedoraAPIM getAPIM(){
        if (APIMport == null){
            initAPIM();
        }
        return APIMport;
    }
    
    public ObjectFactory getObjectFactory(){
        if (of == null){
            of = new ObjectFactory();
        }
        return of;
    }
    
    
    private void initAPIA(){
        final String user = KConfiguration.getInstance().getFedoraUser();
        final String pwd = KConfiguration.getInstance().getFedoraPass();
        Authenticator.setDefault(new Authenticator() { 
            protected PasswordAuthentication getPasswordAuthentication() { 
               return new PasswordAuthentication(user, pwd.toCharArray()); 
             } 
           }); 
        
        FedoraAPIAService APIAservice = null;
        try {
            APIAservice = new FedoraAPIAService(new URL(KConfiguration.getInstance().getFedoraHost()+"/wsdl?api=API-A"),
                    new QName("http://www.fedora.info/definitions/1/0/api/", "Fedora-API-A-Service"));
        } catch (MalformedURLException e) {
            LOGGER.severe("InvalidURL API-A:"+e);
            throw new RuntimeException(e);
        }
        APIAport = APIAservice.getPort(FedoraAPIA.class);
        ((BindingProvider) APIAport).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, user);
        ((BindingProvider) APIAport).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pwd);
       

    }
    
    private void initAPIM(){
        final String user = KConfiguration.getInstance().getFedoraUser();
        final String pwd = KConfiguration.getInstance().getFedoraPass();
        Authenticator.setDefault(new Authenticator() { 
            protected PasswordAuthentication getPasswordAuthentication() { 
               return new PasswordAuthentication(user, pwd.toCharArray()); 
             } 
           }); 
        
        FedoraAPIMService APIMservice = null;
        try {
            APIMservice = new FedoraAPIMService(new URL(KConfiguration.getInstance().getFedoraHost()+"/wsdl?api=API-M"),
                    new QName("http://www.fedora.info/definitions/1/0/api/", "Fedora-API-M-Service"));
        } catch (MalformedURLException e) {
            LOGGER.severe("InvalidURL API-M:"+e);
            throw new RuntimeException(e);
        }
        APIMport = APIMservice.getPort(FedoraAPIM.class);
        ((BindingProvider) APIMport).getRequestContext().put(BindingProvider.USERNAME_PROPERTY, user);
        ((BindingProvider) APIMport).getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, pwd);
       

    }
    
    
    
    private static final List<String> TREE_PREDICATES = Arrays.asList(new String[]{
            "http://www.nsdl.org/ontologies/relationships#hasPage",
            "http://www.nsdl.org/ontologies/relationships#hasPart",
            "http://www.nsdl.org/ontologies/relationships#hasVolume",
            "http://www.nsdl.org/ontologies/relationships#hasItem",
            "http://www.nsdl.org/ontologies/relationships#hasUnit"
    });
    
    public void processSubtree(String pid, TreeNodeProcessor processor){
        processor.process(pid);
        for (RelationshipTuple rel : getAPIM().getRelationships(pid, null)){
            if (TREE_PREDICATES.contains(rel.getPredicate())){
                processSubtree(rel.getObject(), processor);
            }
        }
    }
    
    public Set<String> getPids(String pid){
        final Set<String> retval = new HashSet<String>();
        processSubtree(pid, new TreeNodeProcessor(){

            @Override
            public void process(String pid) {
               retval.add(pid);
            }});
        return retval;
    }

	@Override
	public InputStream getDataStream(String pid, String datastreamName) throws IOException {
    	String datastream = configuration.getFedoraHost()+"/get/"+pid+"/"+datastreamName;
		HttpURLConnection con = (HttpURLConnection) openConnection(datastream,configuration.getFedoraUser(), configuration.getFedoraPass());
		con.connect();
		if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
			InputStream thumbInputStream = con.getInputStream();
			return thumbInputStream;
		} throw new IOException("404");
	}

	@Override
	public String getMimeTypeForStream(String pid, String datastreamName) throws IOException {
		HttpURLConnection con = (HttpURLConnection) openConnection(dsProfileForPid(configuration, datastreamName ,pid),configuration.getFedoraUser(), configuration.getFedoraPass());
		InputStream stream = con.getInputStream();
		try {
			Document parseDocument = XMLUtils.parseDocument(stream, true);
			return mimetypeFromProfile(parseDocument);
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		} catch (XPathExpressionException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new IOException(e);
		}	
	}
}
