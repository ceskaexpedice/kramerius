package cz.incad.kramerius.rest.api.k5.client.item.utils;

import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.utils.RelsExtHelper;

public class IIIFUtils {

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

}
