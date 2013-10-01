package cz.incad.kramerius.rest.api.utils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class ExceptionXMLObjectUtils {

    public static final String MESSAGE_KEY ="message";
    public static final String STATUS_CODE_KEY ="status";
    public static final String CAUSE_KEY ="cause";

    /**
     * Returns json object contains message key
     * @param mess 
     * @return
     * @throws ParserConfigurationException 
     */
    public static Document fromMessage(String mess, int statuscode) throws ParserConfigurationException {
    	DocumentBuilderFactory docBuilder = DocumentBuilderFactory.newInstance();
    	Document document = docBuilder.newDocumentBuilder().newDocument();
    	Element rootElm = document.createElement("message");
    	
    	Element status = document.createElement("status");
    	status.setTextContent(""+statuscode);
    	rootElm.appendChild(status);
    	
    	Element message = document.createElement("message");
    	message.setTextContent(mess);
    	rootElm.appendChild(message);
    	
    	document.appendChild(rootElm);
    	
    	return document;
    }

}
