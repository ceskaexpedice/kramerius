/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.utils;

/**
 *
 * @author incad
 */
import java.io.InputStream;
import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class FedoraUtils {

    
    public static final String IMG_THUMB = "IMG_THUMB";

    public static ArrayList<String> getRdfPids(String pid, String relation) {
        ArrayList<String> pids = new ArrayList<String>();
        try {
            
            String command = KConfiguration.getKConfiguration().getFedoraHost() + "/get/" + pid + "/RELS-EXT";
            InputStream is = RESTHelper.inputStream(command, KConfiguration.getKConfiguration().getFedoraUser(), KConfiguration.getKConfiguration().getFedoraPass());
            Document contentDom = XMLUtils.parseDocument(is);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            String xPathStr = "/RDF/Description/" + relation;
            XPathExpression expr = xpath.compile(xPathStr);
            NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                if (!childnode.getNodeName().contains("hasModel")) {
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

    public static boolean fillFirstPagePid(ArrayList<String> pids, ArrayList<String> models) {
        
        String pid= pids.get(pids.size()-1);
        try {
            String command = KConfiguration.getKConfiguration().getFedoraHost() + "/get/uuid:" + pid + "/RELS-EXT";
            InputStream is = RESTHelper.inputStream(command, KConfiguration.getKConfiguration().getFedoraUser(), KConfiguration.getKConfiguration().getFedoraPass());
            Document contentDom = XMLUtils.parseDocument(is);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expr = xpath.compile("/RDF/Description/*");
            NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                String nodeName = childnode.getNodeName();
                if (nodeName.contains("hasPage")) {
                    pids.add(childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("uuid:")[1]);
                    models.add("page");
                    return true;
                } else if(!nodeName.contains("hasModel")) {
                    pids.add(childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("uuid:")[1]);
                    models.add(KrameriusModels.toString(cz.incad.kramerius.RDFModels.convertRDFToModel(nodeName)));
                    return FedoraUtils.fillFirstPagePid(pids, models);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static String findFirstPagePid(String pid) {

        ArrayList<String> pids = new ArrayList<String>();
        try {
            String command = KConfiguration.getKConfiguration().getFedoraHost() + "/get/" + pid + "/RELS-EXT";
            InputStream is = RESTHelper.inputStream(command, KConfiguration.getKConfiguration().getFedoraUser(), KConfiguration.getKConfiguration().getFedoraPass());
            Document contentDom = XMLUtils.parseDocument(is);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expr = xpath.compile("/RDF/Description/*");
            NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                String nodeName = childnode.getNodeName();
                if (nodeName.contains("hasPage")) {
                    return childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("uuid:")[1];
                } else if(!nodeName.contains("hasModel")) {
                    pids.add(childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1]);
                }
            }
            for (String relpid : pids) {
                return FedoraUtils.findFirstPagePid(relpid);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
