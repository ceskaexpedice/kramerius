package cz.incad.Kramerius;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.inject.Inject;

import cz.incad.Kramerius.backend.guice.GuiceServlet;
import cz.incad.Kramerius.views.ApplicationURL;
import cz.incad.kramerius.intconfig.InternalConfiguration;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class HandleServlet extends GuiceServlet {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(HandleServlet.class.getName());
	
	@Inject
	KConfiguration kConfiguration;

	private XPathFactory fact;
	private XPathExpression pidPathExpr; //cesta pidu 
	private XPathExpression pidExpr; // ja 
	private XPathExpression pathExpr; // cesta modelu
	
	@Override
	public void init() throws ServletException {
		super.init();
		try {
			fact = XPathFactory.newInstance();
			pidPathExpr = pidPathExpr();
			pidExpr = pidExpr();
			pathExpr = pathExpr();
		} catch (XPathExpressionException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e.getMessage());
		}
	}


	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			fact = XPathFactory.newInstance();
			pidPathExpr = pidPathExpr();
		} catch (XPathExpressionException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			throw new RuntimeException(e.getMessage());
		}
	}



	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String pidPath = null;
			String pid = null;
			String path = null;
			
			String requestURL = req.getRequestURL().toString();
			String handle = disectHandle(requestURL);
			String uri = HandleType.createType(handle).construct(handle);
			InputStream inputStream = RESTHelper.inputStream(uri, "<no_user>", "<no_pass>");
			Document parseDocument = XMLUtils.parseDocument(inputStream);
		    Node node = (Node) pidPathExpr.evaluate(parseDocument, XPathConstants.NODE);
		    if (node != null) {
		    	Element pidPathElm = (Element) node;
		    	pidPath = pidPathElm.getTextContent();
		    }
		    
		    node = (Node) pidExpr.evaluate(parseDocument, XPathConstants.NODE);
		    if (node != null) {
		    	Element pidElm = (Element) node;
		    	pid = pidElm.getTextContent();
		    }

		    node = (Node) pathExpr.evaluate(parseDocument, XPathConstants.NODE);
		    if (node != null) {
		    	Element pathElm = (Element) node;
		    	path = pathElm.getTextContent();
		    }

		    String appURL = ApplicationURL.applicationURL(req);
		    String redirectUrl=  "item.jsp?pid="+pid+"&pid_path="+pidPath+"&path="+path;
		    resp.sendRedirect(appURL+"/"+redirectUrl);
		    
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);;
			resp.sendError(500);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);;
			resp.sendError(500);
		} catch (XPathExpressionException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);;
			resp.sendError(500);
		}
	}



	private XPathExpression pidPathExpr() throws XPathExpressionException {
		XPathExpression pidPathExpr = fact.newXPath().compile("//str[@name='pid_path']");
		return pidPathExpr;
	}

	private XPathExpression pidExpr() throws XPathExpressionException {
		XPathExpression pidExpr = fact.newXPath().compile("//str[@name='PID']");
		return pidExpr;
	}

	private XPathExpression pathExpr() throws XPathExpressionException {
		XPathExpression pathExpr = fact.newXPath().compile("//str[@name='path']");
		return pathExpr;
	}


	
	
	public static String disectHandle(String requestURL) {
		//"dvju"
		try {
			StringBuffer buffer = new StringBuffer();
			URL url = new URL(requestURL);
			String path = url.getPath();
			String application = path;
			StringTokenizer tokenizer = new StringTokenizer(path,"/");
			if (tokenizer.hasMoreTokens()) application = tokenizer.nextToken();
			String handleServlet = path;
			if (tokenizer.hasMoreTokens()) handleServlet = tokenizer.nextToken();
			// check handle servlet
			while(tokenizer.hasMoreTokens()) {
				buffer.append(tokenizer.nextElement());
				if (tokenizer.hasMoreTokens()) buffer.append("/");
			}
			return buffer.toString();
		} catch (MalformedURLException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return "<no handle>";
		}
		
	}
	
	enum HandleType {
		UUID {
			@Override
			public String construct(String handle) {
				String uuid = handle.substring("uuid:".length());
				String solrHost = KConfiguration.getInstance().getSolrHost();
				String uri = solrHost +"/select/?q=PID:"+uuid;
				return uri;
			}
		},
		KRAMERIUS3{
			@Override
			public String construct(String handle) {
				
				try {
					handle = URLEncoder.encode(handle, "UTF-8");
					String solrHost = KConfiguration.getInstance().getSolrHost();
					String uri = solrHost +"/select/?q=handle:"+handle;
					return uri;
				} catch (UnsupportedEncodingException e) {
					LOGGER.log(Level.SEVERE, e.getMessage(), e);
					return "<err in handle>";
				}

			}
		};		
	
		abstract String construct(String handle);

		public static HandleType createType(String handle) {
			if (handle.toLowerCase().startsWith("uuid:")) {
				return UUID;
			} else {
				return KRAMERIUS3;
			}
		}
	}
	
}
