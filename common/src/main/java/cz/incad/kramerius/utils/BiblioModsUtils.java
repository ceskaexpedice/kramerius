package cz.incad.kramerius.utils;

import static cz.incad.kramerius.FedoraNamespaces.*;
import static cz.incad.kramerius.utils.XMLUtils.*;

import java.util.Iterator;
import java.util.logging.Level;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.KrameriusModels;

public class BiblioModsUtils {
	
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(BiblioModsUtils.class.getName());
	
	
	
	

	public static String getPageNumber(Document doc) {
		try {
			XPathFactory xpfactory = XPathFactory.newInstance();
			XPath xpath = xpfactory.newXPath();
			xpath.setNamespaceContext(new FedoraNamespaceContext());
			XPathExpression expr = xpath.compile("//mods:mods/mods:part/mods:detail[@type='pageNumber']/mods:number/text()");
			Object pageNumber = expr.evaluate(doc,XPathConstants.STRING);
			return (String) pageNumber;
		} catch (XPathExpressionException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return null;
		} catch (DOMException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}


//	public static String getTitle(Document doc, KrameriusModels model) {
//	    String title = titleFromBiblioMods(doc);
//		if ((title == null) || (title.equals(""))) {
//			switch(model) {
//				case PERIODICALITEM: return PeriodicalItemUtils.getItemNumber(doc) + " ("+PeriodicalItemUtils.getDate(doc)+")";
//				case PERIODICALVOLUME: return PeriodicalItemUtils.getItemNumber(doc) + " ("+PeriodicalItemUtils.getDate(doc)+")";
//				default: throw new UnsupportedOperationException("'"+model+"'");
//			}
//		} else return title;
//	}
	
	public static String titleFromBiblioMods(Document doc) {
		try {
			XPathFactory xpfactory = XPathFactory.newInstance();
			XPath xpath = xpfactory.newXPath();
			xpath.setNamespaceContext(new FedoraNamespaceContext());
			XPathExpression expr = xpath.compile("//mods:titleInfo/mods:title");
			Node node = (Node) expr.evaluate(doc, XPathConstants.NODE);
			if (node != null) {
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element elm = (Element) node;
					return elm.getTextContent();
				} else return null;
			} else return null;
		} catch (XPathExpressionException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return null;
		} catch (DOMException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			return null;
		}
	}
}
