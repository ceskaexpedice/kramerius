package cz.incad.Kramerius.backend.impl;

import static cz.incad.Kramerius.FedoraUtils.*;
import static cz.incad.kramerius.utils.RESTHelper.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

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

import com.google.inject.Inject;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;

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
	public List<Element> getPages(String uuid) throws IOException {
		Document relsExt = getRelsExt(uuid);
		return getPages(uuid, relsExt.getDocumentElement());
	}
	
	@Override
	public Document getRelsExt(String uuid) throws IOException {
		String relsExtUrl = relsExtUrl(KConfiguration.getKConfiguration(), uuid);
		LOGGER.info("Reading rels ext +"+relsExtUrl);
		InputStream docStream = RESTHelper.inputStream(relsExtUrl, KConfiguration.getKConfiguration().getFedoraUser(), KConfiguration.getKConfiguration().getFedoraPass());
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return builder.parse(docStream);
		} catch (ParserConfigurationException e) {
			LOGGER.log(Level.SEVERE,e.getMessage(), e);
			throw new IOException(e);
		} catch (SAXException e) {
			LOGGER.log(Level.SEVERE,e.getMessage(), e);
			throw new IOException(e);
		}
	}

	
	public static String relsExtUrl(KConfiguration configuration, String uuid) {
		String url = configuration.getFedoraHost() +"/get/uuid:"+uuid+"/RELS-EXT";
		return url;
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
	
	public InputStream getDJVU(String uuid) throws IOException {
		HttpURLConnection con = (HttpURLConnection) openConnection(getDjVuImage(configuration ,uuid),configuration.getFedoraUser(), configuration.getFedoraPass());
		InputStream thumbInputStream = con.getInputStream();
		return thumbInputStream;
	}
}
