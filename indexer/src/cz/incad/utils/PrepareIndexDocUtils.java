package cz.incad.utils;

import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cz.incad.kramerius.utils.XMLUtils;

public class PrepareIndexDocUtils {

    public static String wrapByAddCommand(Document prevDoc) throws ParserConfigurationException, TransformerException {
        Document document = XMLUtils.crateDocument("add");

        Element rootElm = prevDoc.getDocumentElement();
        document.adoptNode(rootElm);
        document.getDocumentElement().appendChild(rootElm);
        
        StringWriter writer = new StringWriter();
        XMLUtils.print(document, writer);
        
        return writer.toString();
    }
    
    public static void enhanceByCompositeId(Document ndoc,Element docElm) {
        Element pidElm = XMLUtils.findElement(docElm, new XMLUtils.ElementsFilter() {
            
            @Override
            public boolean acceptElement(Element paramElement) {
                String attribute = paramElement.getAttribute("name");
                return attribute.equals("PID");
            }
        });
        Element rootPidElm = XMLUtils.findElement(docElm, new XMLUtils.ElementsFilter() {
            
            @Override
            public boolean acceptElement(Element paramElement) {
                String attribute = paramElement.getAttribute("name");
                return attribute.equals("root_pid");
            }
        });
        
            
        
        String txt = rootPidElm.getTextContent().trim()+"!"+pidElm.getTextContent().trim();
        Element compositeIdElm = ndoc.createElement("field");
        String compositeIdName = System.getProperty("compositeId.field.name","compositeId");
        compositeIdElm.setAttribute("name", compositeIdName);
        compositeIdElm.setTextContent(txt);
        docElm.appendChild(compositeIdElm);
        
    }

}
