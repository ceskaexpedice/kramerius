package cz.incad.kramerius.utils;

import static cz.incad.kramerius.FedoraNamespaces.DC_NAMESPACE_URI;
import static cz.incad.kramerius.utils.XMLUtils.findElement;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class DCUtils {

	public static String titleFromDC(org.w3c.dom.Document dc) {
		Element elm = findElement(dc.getDocumentElement(), "title", DC_NAMESPACE_URI);	
		if (elm == null) elm = findElement(dc.getDocumentElement(), "identifier", DC_NAMESPACE_URI);
		String title = elm.getTextContent();
		return title;
	}
	
	public static String modelFromDC(org.w3c.dom.Document dc) {
        Element elm = findElement(dc.getDocumentElement(), "type", DC_NAMESPACE_URI);  
        if (elm != null) {
            String type = elm.getTextContent();
            StringTokenizer tokenizer = new StringTokenizer(type,":");
            if ((tokenizer.hasMoreTokens() && tokenizer.nextToken() != null) && tokenizer.hasMoreTokens()) {
                String model = tokenizer.nextToken();
                return model;
            } else return null;
        } else return null;
	}
	
	public static String[] publishersFromDC(org.w3c.dom.Document dc) {
        ArrayList<String> texts = findElmTexts(dc, "publisher");
        return (String[]) texts.toArray(new String[texts.size()]);
	}
	
	public static String[] creatorsFromDC(org.w3c.dom.Document dc) {
	    ArrayList<String> texts = findElmTexts(dc, "creator");
		return (String[]) texts.toArray(new String[texts.size()]);
	}

    public static String dateFromDC(org.w3c.dom.Document dc) {
        ArrayList<String> dates = findElmTexts(dc, "date");
        if (!dates.isEmpty()) return dates.get(0);
        else return null;
    }
	
    
    public static ArrayList<String> findElmTexts(org.w3c.dom.Document dc, String elmName) {
        ArrayList<String> texts  = new ArrayList<String>();
		Element documentElement = dc.getDocumentElement();
		NodeList childNodes = documentElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if (item.getNodeType() == Node.ELEMENT_NODE) {
                if (item.getLocalName().equals(elmName)) {
					texts.add(item.getTextContent().trim());
				}
			}
		}
        return texts;
    }
	

}
