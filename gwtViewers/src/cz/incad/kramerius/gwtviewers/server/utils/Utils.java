package cz.incad.kramerius.gwtviewers.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraModels;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.gwtviewers.client.SimpleImageTO;
import cz.incad.kramerius.gwtviewers.server.PageServiceImpl;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;

public class Utils {

	public static FedoraModels getModel(KConfiguration kConfiguration, String uuid) throws LexerException, ParserConfigurationException, IOException, SAXException {
		String relsExtUrl = PageServiceImpl.relsExtUrl(kConfiguration, uuid);
		PageServiceImpl.LOGGER.info("Reading rels ext +"+relsExtUrl);
		InputStream docStream = RESTHelper.inputStream(relsExtUrl, kConfiguration.getFedoraUser(), kConfiguration.getFedoraPass());
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document parsed = builder.parse(docStream);
		Stack<Element> stack = new Stack<Element>();
		stack.push(parsed.getDocumentElement());
		while(!stack.isEmpty()) {
			Element popElement = stack.pop();
			String uri = popElement.getNamespaceURI();
			String localName = popElement.getLocalName();
			if ((localName.equals("hasModel")) &&(uri.equals(FedoraNamespaces.FEDORA_MODELS_URI))){
				String resourceAttr = popElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
				PIDParser pidParser = new PIDParser(resourceAttr);
				pidParser.disseminationURI();
				String nmscp = pidParser.getNamespaceId();
				// jedna se o fedoraModel
				if (nmscp.equals("model")) {
					FedoraModels model = FedoraModels.valueOf(pidParser.getObjectId());
					PageServiceImpl.LOGGER.info("Model is :"+model);
					return model;
				}
			}
			NodeList nlist = popElement.getChildNodes();
			for (int i = 0,ll=nlist.getLength(); i < ll; i++) {
				Node item = nlist.item(i);
				if (item.getNodeType() == Node.ELEMENT_NODE) {
					stack.push((Element) item);
				}
			}
		}
		return null;
	}

	
	public static ArrayList<SimpleImageTO> getPages(KConfiguration kConfiguration, HttpServletRequest request,
			String currentProcessinguuid) throws IOException,
			ParserConfigurationException, SAXException, LexerException {
		ArrayList<SimpleImageTO> pages = new ArrayList<SimpleImageTO>();
		InputStream docStream;
		String relsExtUrl = PageServiceImpl.relsExtUrl(kConfiguration, currentProcessinguuid);
		PageServiceImpl.LOGGER.info("Reading rels ext +"+relsExtUrl);
		docStream = RESTHelper.inputStream(relsExtUrl, kConfiguration.getFedoraUser(), kConfiguration.getFedoraPass());
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document parsed = builder.parse(docStream);
		Stack<Element> stack = new Stack<Element>();
		stack.push(parsed.getDocumentElement());
		while(!stack.isEmpty()) {
			Element popElement = stack.pop();
			String uri = popElement.getNamespaceURI();
			String localName = popElement.getLocalName();
			if ((localName.equals("hasPage")) &&(uri.equals(FedoraNamespaces.KRAMERIUS_URI))){
				String resourceAttr = popElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
				PIDParser pidParser = new PIDParser(resourceAttr);
				pidParser.disseminationURI();
				String nmscp = pidParser.getNamespaceId();
				if (nmscp.equals("uuid")) {
					String objectId = pidParser.getObjectId();
					SimpleImageTO imageTO = new SimpleImageTO();
					imageTO.setIdentification(objectId);
					String thumbnailURL = PageServiceImpl.thumbnail(objectId, kConfiguration.getScaledHeight(), request);
					imageTO.setUrl(thumbnailURL);
					imageTO.setHeight(Integer.parseInt(kConfiguration.getScaledHeight()));
					imageTO.setWidth(Integer.parseInt(kConfiguration.getScaledHeight()));
					pages.add(0,imageTO);
				}
			}
			
			NodeList chNodes = popElement.getChildNodes();
			for (int i = 0,ll=chNodes.getLength(); i < ll; i++) {
				Node item = chNodes.item(i);
				if (item.getNodeType() == Node.ELEMENT_NODE) {
					stack.push((Element) item);
				}
			}
		}
		return pages;
	}
}
