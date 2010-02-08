/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.Kramerius;

/**
 *
 * @author Administrator
 */
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import org.apache.log4j.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author Incad
 */
public class KConfiguration {

    Logger logger = Logger.getLogger(this.getClass().getName());
    public HashMap<String, String> properties = new HashMap<String, String>();
    public String fedoraHost;
    private String solrHost;
    public String indexerHost;
    public String fedoraUser;
    public String fedoraPass;

    public KConfiguration(String file) {
        try {
            logger.info("Loading configuration");
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(false); // never forget this!

            DocumentBuilder builder = domFactory.newDocumentBuilder();

            
            InputSource source = new InputSource(new FileInputStream(new File(file)));
            Document contentDom = builder.parse(source);

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            
            //Loading properties
            XPathExpression expr = xpath.compile("//setup/properties/property");
            NodeList propertiesNodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            for (int i = 0; i < propertiesNodes.getLength(); i++) {
                Node node = propertiesNodes.item(i);
                properties.put(node.getAttributes().getNamedItem("name").getNodeValue(),
                        node.getAttributes().getNamedItem("value").getNodeValue());
            }
            
            //Loading tables
            //expr = xpath.compile("//setup/document_definitions/document_definition");
            //NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            //for (int i = 0; i < nodes.getLength(); i++) {
                //DocumentDefinition dd = DocumentDefinition.Load(nodes.item(i), this);
                //documentDefinitions.add(dd);
            //}
        } catch (Exception ex) {
            logger.error("Can't load configuration");
            throw new RuntimeException(ex.toString());
        }
    }
    
    public String getFedoraHost(){
        return getProperty("fedoraHost");
    }
    public String getSolrHost(){
        return getProperty("solrHost");
    }
    public String getIndexerHost(){
        return getProperty("indexerHost");
    }
    
    public String getProperty(String key) {
        if (properties.containsKey(key)) {
            return properties.get(key);
        } else {
            logger.warn("Can't get property " + key);
            return null;
        }
    }

    public String getProperty(String key, String defaultValue) {
        if (properties.containsKey(key)) {
            return properties.get(key);
        } else {
            return defaultValue;
        }
    }
}

