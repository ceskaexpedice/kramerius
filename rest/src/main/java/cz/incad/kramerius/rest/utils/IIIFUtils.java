package cz.incad.kramerius.rest.utils;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.RelsExtHelper;

public class IIIFUtils {

    public static final Logger LOGGER = Logger.getLogger(IIIFUtils.class.getName());
    
    public static String iiifImageEndpoint(String pid, FedoraAccess fedoraAccess) throws IOException {
        try {
            String url = RelsExtHelper.getRelsExtTilesUrl(pid, fedoraAccess);
            if (url == null)
                return null;
            if (url.trim().equals(RelsExtHelper.CACHE_RELS_EXT_LITERAL))
                return null;
            return url.replaceAll("[z|Z]oomify|deepZoom","iiif");
        } catch (XPathExpressionException  e) {
            throw new IOException(e.getMessage());
        }
    }

    public static String iiifImageEndpoint( Document relsExt) throws IOException {
        try {
            String url = RelsExtHelper.getRelsExtTilesUrl(relsExt);
            if (url == null)
                return null;
            if (url.trim().equals(RelsExtHelper.CACHE_RELS_EXT_LITERAL))
                return null;
            return url.replaceAll("[z|Z]oomify|deepZoom","iiif");
        } catch (XPathExpressionException  e) {
            throw new IOException(e.getMessage());
        }
    }

}
