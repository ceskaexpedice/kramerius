package cz.incad.kramerius.utils;

import static cz.incad.kramerius.FedoraNamespaces.DC_NAMESPACE_URI;
import static cz.incad.kramerius.utils.XMLUtils.findElement;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DCUtils {

	public static String titleFromDC(org.w3c.dom.Document dc) {
		Element elm = findElement(dc.getDocumentElement(), "title", DC_NAMESPACE_URI);	
		if (elm == null) elm = findElement(dc.getDocumentElement(), "identifier", DC_NAMESPACE_URI);
		String title = elm.getTextContent();
		return title;
	}

}
