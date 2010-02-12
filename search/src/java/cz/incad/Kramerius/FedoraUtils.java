/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.incad.Kramerius;

/**
 *
 * @author incad
 */

import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class FedoraUtils {
    
    
    public static final String IMG_THUMB = "IMG_THUMB";
    
        
    public static ArrayList<String> getRdfPids(String pid, String relation) {
        ArrayList<String> pids = new ArrayList<String>();
        try {
            String command = KConfiguration.getKConfiguration().getFedoraHost() + "/get/" + pid + "/RELS-EXT";
            Document contentDom = UrlReader.getDocument(command);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            String xPathStr = "/RDF/Description/" + relation;
            XPathExpression expr = xpath.compile(xPathStr);
            NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                if(!childnode.getNodeName().contains("hasModel")){
                    pids.add(childnode.getNodeName() + " " +
                            childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            pids.add(e.toString());
        }
        return pids;
    }
    

    /**
     * Vraci url na stream s DJVU
     * @param uuid objektu
     * @return
     */
    public static String getDjVuImage(KConfiguration configuration, String uuid) {
    	String imagePath = configuration.getFedoraHost()+"/get/uuid:"+uuid+"/IMG_FULL";
    	return imagePath;
    }
    
    /**
     * Vraci url na stream THUMB
     * @param uuid
     * @return
     */
    public static String getThumbnailFromFedora(KConfiguration configuration, String uuid) {
    	String imagePath = configuration.getFedoraHost()+"/get/uuid:"+uuid+"/" + IMG_THUMB;
    	return imagePath;
    }
    
}
