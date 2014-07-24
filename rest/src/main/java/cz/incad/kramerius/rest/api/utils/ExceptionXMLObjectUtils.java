package cz.incad.kramerius.rest.api.utils;

import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.utils.XMLUtils;


public class ExceptionXMLObjectUtils {

    public static final Logger LOGGER = Logger.getLogger(ExceptionXMLObjectUtils.class.getName());
    
    public static final String MESSAGE_KEY ="message";
    public static final String STATUS_CODE_KEY ="status";
    public static final String CAUSE_KEY ="cause";

    /**
     * Returns json object contains message key
     * @param mess 
     * @return
     * @throws ParserConfigurationException 
     */
    public static Document fromMessageDOM(String mess, int statuscode)  {
    	try {
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
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return null;
        } catch (ParserConfigurationException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }

    public static String fromMessageString(String mess, int statuscode)  {
        try {
            StringWriter strWriter = new StringWriter();
            XMLUtils.print(fromMessageDOM(mess, statuscode), strWriter);
            return strWriter.toString();
        } catch (TransformerException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return "";
        }
    }
    
}
