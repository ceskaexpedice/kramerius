/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.kramerius.utils;

/**
 *
 * @author incad
 */


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.fedora.api.RelationshipTuple;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.KrameriusModels;
import cz.incad.kramerius.utils.conf.KConfiguration;
import java.util.Set;

public class FedoraUtils {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(FedoraUtils.class.getName());
    public static final String RELS_EXT_STREAM = "RELS-EXT";
    public static final String IMG_THUMB_STREAM = "IMG_THUMB";
    public static final String IMG_FULL_STREAM = "IMG_FULL";
    public static final String IMG_PREVIEW_STREAM = "IMG_PREVIEW";
    public static final String ALTO_STREAM = "ALTO";
    public static final String DC_STREAM = "DC";
    public static final String BIBLIO_MODS_STREAM = "BIBLIO_MODS";
    public static final String TEXT_OCR_STREAM = "TEXT_OCR";
    public static final String MP3_STREAM = "MP3";
    public static final String OGG_STREAM = "OGG";
    public static final String WAV_STREAM = "WAV";

    
    public static final String POLICY_STREAM="POLICY";
    
    public static List<String> INTERNAL_STREAM = new ArrayList<String>(){{
       add(RELS_EXT_STREAM);
       add(IMG_THUMB_STREAM);
       add(IMG_FULL_STREAM);
       add(IMG_PREVIEW_STREAM);
       add(ALTO_STREAM);
       add(DC_STREAM);
       add(BIBLIO_MODS_STREAM);
    }};

    public static List<String> AUDIO_STREAMS = new ArrayList<String>(){{
        add(OGG_STREAM);
        add(MP3_STREAM);
        add(WAV_STREAM);
     }};

    /** Stream for fedora internal use */
    public static List<String> FEDORA_INTERNAL_STREAMS = new ArrayList<String>(){{
        add(RELS_EXT_STREAM);
        add(POLICY_STREAM);
     }};
    

    
    public static final int THUMBNAIL_HEIGHT = 128;
    public static final int PREVIEW_HEIGHT = 700;

    
    
    public static ArrayList<String> getRdfPids(String pid, String relation) {
        ArrayList<String> pids = new ArrayList<String>();
        try {

            String command = KConfiguration.getInstance().getFedoraHost() + "/get/" + pid + "/" + RELS_EXT_STREAM;
            InputStream is = RESTHelper.inputStream(command, KConfiguration.getInstance().getFedoraUser(), KConfiguration.getInstance().getFedoraPass());
            Document contentDom = XMLUtils.parseDocument(is);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            String xPathStr = "/RDF/Description/" + relation;
            XPathExpression expr = xpath.compile(xPathStr);
            NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                if (!childnode.getNodeName().contains("hasModel")) {
                    pids.add(childnode.getNodeName() + " "
                            + childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1]);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return pids;
    }

    public static List<RelationshipTuple> getSubjectPids(String objectPid) {
        List<RelationshipTuple> retval = new ArrayList<RelationshipTuple>();
        String command = KConfiguration.getInstance().getFedoraHost() + "/risearch?type=triples&lang=spo&format=N-Triples&query=*%20*%20%3Cinfo:fedora/" + objectPid + "%3E";
        try {
            String result = IOUtils.readAsString(RESTHelper.inputStream(command, KConfiguration.getInstance().getFedoraUser(), KConfiguration.getInstance().getFedoraPass()), Charset.forName("UTF-8"), true);
            String[] lines = result.split("\n");
            for (String line : lines) {
                String[] tokens = line.split(" ");
                if (tokens.length < 3) {
                    continue;
                }
                try {
                    RelationshipTuple tuple = new RelationshipTuple();
                    tuple.setSubject(tokens[0].substring(1, tokens[0].length() - 1));
                    tuple.setPredicate(tokens[1].substring(1, tokens[1].length() - 1));
                    tuple.setObject(tokens[2].substring(1, tokens[2].length() - 1));
                    tuple.setIsLiteral(false);
                    retval.add(tuple);
                } catch (Exception ex) {
                    LOGGER.info("Problem parsing RDF, skipping line:" + Arrays.toString(tokens) + " : " + ex);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return retval;
    }

    public static void main(String[] args) {
        getSubjectPids("uuid:4a8a8630-af36-11dd-ae9c-000d606f5dc6");
    }

     public static String findFirstPagePid(String pid) {

        ArrayList<String> pids = new ArrayList<String>();
        try {
            KConfiguration config = KConfiguration.getInstance();
            String command = config.getFedoraHost() + "/get/" + pid + "/" + RELS_EXT_STREAM;
            InputStream is = RESTHelper.inputStream(command, config.getFedoraUser(), KConfiguration.getInstance().getFedoraPass());
            Document contentDom = XMLUtils.parseDocument(is);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expr = xpath.compile("/RDF/Description/*");
            NodeList nodes = (NodeList) expr.evaluate(contentDom, XPathConstants.NODESET);
            List<String> treePredicates = Arrays.asList(config.getPropertyList("fedora.treePredicates"));
            for (int i = 0; i < nodes.getLength(); i++) {
                Node childnode = nodes.item(i);
                String nodeName = childnode.getNodeName();
                String simpleNodeName = nodeName.substring(nodeName.lastIndexOf(":")+1);
                if (nodeName.contains("hasPage") || nodeName.contains("isOnPage")) {
                    return childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue();
                } else if (!nodeName.contains("hasModel") && childnode.hasAttributes() && treePredicates.contains(simpleNodeName)
                        && childnode.getAttributes().getNamedItem("rdf:resource") != null) {

                    pids.add(childnode.getAttributes().getNamedItem("rdf:resource").getNodeValue().split("/")[1]);
                }
            }
            for (String relpid : pids) {
                return FedoraUtils.findFirstPagePid(relpid);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns url stream 
     * @param uuid of object
     * @return
     */
    public static String getDjVuImage(KConfiguration configuration, String pid) {
        String imagePath = configuration.getFedoraHost() + "/get/" + pid + "/" + IMG_FULL_STREAM;
        return imagePath;
    }

    /**
     * Returns path to fedora stream
     * @param conf KConfiguraiton 
     * @param uuid UUID of the object 
     * @param stream Stream ID
     * @return
     */
    public static String getFedoraStreamPath(KConfiguration conf, String pid, String stream) {
        String imagePath = conf.getFedoraHost() + "/get/" + pid + "/" + stream;
        return imagePath;
    }
    
    /**
     * Returns path to fedora description
     * @return
     */
    public static String getFedoraDescribe(KConfiguration conf) {
        String describePath = conf.getFedoraHost() + "/describe?xml=true";
        return describePath;
    }
    

    /**
     * Returns true if given stream (profile of the stream) is referenced stream by URL
     * @param conf KConfiguration object
     * @param profileDoc Profile document
     */
    public static boolean isFedoraExternalStream(KConfiguration conf, Document profileDoc) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile("/datastreamProfile/dsLocationType/text()");
        NodeList nodes = (NodeList) expr.evaluate(profileDoc, XPathConstants.NODESET);
        if (nodes.getLength() > 0) {
            Text text = (Text) nodes.item(0);
            String trimedString = text.getData().trim();
            return trimedString.equals("URL");
        } else {
            return false;
        }
    }

    public static String getLocation(KConfiguration conf, Document profileDoc) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile("/datastreamProfile/dsLocation/text()");
        NodeList nodes = (NodeList) expr.evaluate(profileDoc, XPathConstants.NODESET);
        if (nodes.getLength() > 0) {
            Text text = (Text) nodes.item(0);
            String trimedString = text.getData().trim();
            return trimedString;
        } else {
            return null;
        }
    }

    /**
     * Returns thumb stream
     * @param uuid UUID of the object
     * @return
     */
    public static String getThumbnailFromFedora(KConfiguration configuration, String pid) {
        String imagePath = configuration.getFedoraHost() + "/get/" + pid + "/" + IMG_THUMB_STREAM;
        return imagePath;
    }

    /**
     * Returns list of fedora streams
     * @param configuration KConfiguration configuration object
     * @param uuid UUID reqested object
     * @return
     */
    public static String getFedoraDatastreamsList(KConfiguration configuration, String pid) {
        String datastreamsListPath = configuration.getFedoraHost() + "/objects/" + pid + "/datastreams?format=xml";
        return datastreamsListPath;
    }

    
    public static String getVersionCompatibilityPrefix(String fedoraVersion) {
        return fedoraVersion.substring(0,3).replace('.', '_');
    }
    
}
