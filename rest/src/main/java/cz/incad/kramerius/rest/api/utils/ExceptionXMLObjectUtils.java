package cz.incad.kramerius.rest.api.utils;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.utils.XMLUtils;


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
    public static Document fromMessageDOM(String mess, int statuscode) throws ParserConfigurationException {
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

    public static String fromMessageString(String mess, int statuscode) throws ParserConfigurationException, TransformerException {
        StringWriter strWriter = new StringWriter();
        XMLUtils.print(fromMessageDOM(mess, statuscode), strWriter);
        return strWriter.toString();
    }
    
}
