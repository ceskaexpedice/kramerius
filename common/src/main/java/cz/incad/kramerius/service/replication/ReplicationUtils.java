package cz.incad.kramerius.service.replication;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.XMLUtils;

/**
 * @author pavels
 *
 */
public class ReplicationUtils {

    /**
     * @param version
     * @throws IOException 
     */
    public static void binaryContentForStream(Document document, Element datastream, Element version, URL url) throws IOException {
        InputStream is = null; 
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            URLConnection urlConnection = url.openConnection();
            is = urlConnection.getInputStream();
            IOUtils.copyStreams(is, bos);
            version.setAttribute("SIZE", ""+bos.size());
            version.removeChild(XMLUtils.findElement( version,"contentLocation",version.getNamespaceURI()));
            Element binaryContent = document.createElementNS(version.getNamespaceURI(), "binaryContent");
            document.adoptNode(binaryContent);
            binaryContent.setTextContent(new String(Base64.encodeBase64(bos.toByteArray())));
            version.appendChild(binaryContent);
            datastream.setAttribute("CONTROL_GROUP", "M");
        } finally {
            IOUtils.tryClose(is);
        }
    }

    /**
     * @param version
     * @throws IOException
     * @throws URISyntaxException
     * @throws DOMException
     */
    public static void referenceForStream(Document document, Element datastream,
            Element version, URL url) throws IOException, DOMException,
            URISyntaxException {
        InputStream is = null;
        try {
            Element digestElm = XMLUtils.findElement(version, "contentDigest",
                    version.getNamespaceURI());
            if (digestElm != null) {
                version.removeChild(XMLUtils.findElement(version,
                        "contentDigest", version.getNamespaceURI()));
            }
    
            Element location = document.createElementNS(
                    version.getNamespaceURI(), "contentLocation");
            location.setAttribute("REF", url.toURI().toString());
            location.setAttribute("TYPE", "URL");
    
            
            List<Node> list = new ArrayList<Node>();
            NodeList childNodes = version.getChildNodes();
            for (int i = 0,ll = childNodes.getLength(); i < ll; i++) {
                list.add(childNodes.item(i));
            }
            while(!list.isEmpty()) {
                version.removeChild(list.remove(0));
            }
            
            document.adoptNode(location);
            version.appendChild(location);
    
            datastream.setAttribute("CONTROL_GROUP", "E");
    
        } finally {
            IOUtils.tryClose(is);
        }
    }

}