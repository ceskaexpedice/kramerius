
package cz.incad.kramerius.k5indexer;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.FedoraNamespaceContext;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.DCUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.VirtualCollection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author alberto
 */
public class CollectionDocument {

    private static final Logger logger = Logger.getLogger(CollectionDocument.class.getName());
    private final Configuration config;

    Commiter commiter;

    VirtualCollection collection;

    public CollectionDocument(VirtualCollection collection, Commiter commiter) throws IOException, JSONException{ 
        config = KConfiguration.getInstance().getConfiguration();
        this.commiter = commiter;
        this.collection = collection;
        
    }

    public void index() throws SolrServerException, IOException {
        commiter.add(makeDoc());
    }

    
    /**
     * make the document
     */
    private SolrInputDocument makeDoc(){
        SolrInputDocument doc = new SolrInputDocument();
        

        //Set fields 
        doc.addField("PID", collection.getPid());
        for (VirtualCollection.CollectionDescription desc : collection.getDescriptions()){
            doc.addField("name_" + desc.getLang(), desc.getText());
        }
        
        
        return doc;
    }
    
}
