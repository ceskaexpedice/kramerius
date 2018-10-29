package cz.incad.kramerius.utils;

import static cz.incad.kramerius.FedoraNamespaces.DC_NAMESPACE_URI;
import static cz.incad.kramerius.utils.XMLUtils.findElement;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.document.model.DCConent;

/**
 * Dublin core utilities
 * @author pavels
 */
public class DCUtils {

    
    /**
     * Returns dc content from given document
     * @param doc parsed DC stream
     * @return
     */
    public static DCConent contentFromDC(org.w3c.dom.Document doc) {
        DCConent content = new DCConent();
        String title = titleFromDC(doc);
        if (title != null) content.setTitle(title);
        
        String model = modelFromDC(doc);
        if (model != null) content.setType(model);
        
        String date = dateFromDC(doc);
        if (date != null) content.setDate(date);
        
        String[] publishersFromDC = publishersFromDC(doc);
        if (publishersFromDC != null) content.setPublishers(publishersFromDC);
        
        String[] creatorsFromDC = creatorsFromDC(doc);
        if (creatorsFromDC != null) content.setCreators(creatorsFromDC);
        
        String[] identsFromDC = identifierlsFromDC(doc);
        if (identsFromDC != null) content.setIdentifiers(identsFromDC);
        
        return content;
         
    }

    /**
     * Rights from Dublin core
     * @param doc
     * @return
     */
    public static String rightsFromDC(org.w3c.dom.Document doc) {
        return rightsFromDC(doc.getDocumentElement());
    }

    /**
     * Rights from Dublin core
     * @param docElement
     * @return
     */
    public static String rightsFromDC(Element docElement) {
        Element elm = findElement(docElement, "rights", DC_NAMESPACE_URI);
        if (elm != null) {
            String policy = elm.getTextContent().trim();
            return policy;
        } else return null;
    }




    /**
     * Returns title from dc stream
     * @param doc parsed DC stream
     * @return
     */
    public static String titleFromDC(org.w3c.dom.Document doc) {
        Element elm = findElement(doc.getDocumentElement(), "title", DC_NAMESPACE_URI);
        if (elm == null) elm = findElement(doc.getDocumentElement(), "identifier", DC_NAMESPACE_URI);
        String title = elm.getTextContent();
        return title;
    }

    /**
     * Returns model from dc stream
     * @param doc parsed DC stream
     * @return
     */
    public static String modelFromDC(org.w3c.dom.Document doc) {
        List<Element> elements = XMLUtils.getElements(doc.getDocumentElement(),  new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                return (element.getLocalName().equals("type") && element.getNamespaceURI().equals(DC_NAMESPACE_URI));
            }
        });

        for (Element elm : elements) {
            String type = elm.getTextContent();
            if (type.contains(":")) {
                StringTokenizer tokenizer = new StringTokenizer(type,":");
                if ((tokenizer.hasMoreTokens() && tokenizer.nextToken() != null) && tokenizer.hasMoreTokens()) {
                    String model = tokenizer.nextToken();
                    return model;
                } else return null;
            }
        }
        return null;
    }

    /**
     * Returns publishers from DC stream
     * @param doc parsed DC stream
     * @return
     */
    public static String[] publishersFromDC(org.w3c.dom.Document doc) {
        ArrayList<String> texts = findElmTexts(doc, "publisher");
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

    public static String languageFromDC(org.w3c.dom.Document dc) {
        ArrayList<String> dates = findElmTexts(dc, "language");
        if (!dates.isEmpty()) return dates.get(0);
        else return null;
    }

    
    public static String[] identifierlsFromDC(org.w3c.dom.Document dc) {
        ArrayList<String> idents = findElmTexts(dc, "identifier");
        return (String[]) idents.toArray(new String[idents.size()]);
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
