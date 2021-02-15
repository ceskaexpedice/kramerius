package cz.incad.kramerius.rest.apiNew.client.v60;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;


/**
 * see cz.incad.Kramerius.imaging.utils.ZoomChangeFromReplicated
 */
public class ZoomChangeFromReplicated {

    //TODO: pouklizet, neni moc videt, co to dela
    public static String zoomifyAddress(Document relsExt, String pid) {
        String replicatedFrom = replicatedFrom(relsExt);
        if (replicatedFrom != null) {
            int indexOf = replicatedFrom.indexOf("/handle/");
            String app = replicatedFrom.substring(0, indexOf);
            return app + "/zoomify/" + pid;
        } else
            return null;
    }

    private static String replicatedFrom(Document relsExt) {
        Element descElement = XMLUtils.findElement(
                relsExt.getDocumentElement(), "Description",
                FedoraNamespaces.RDF_NAMESPACE_URI);
        List<Element> delems = XMLUtils.getElements(descElement);
        for (Element del : delems) {
            if (del.getNamespaceURI() != null) {
                if (del.getNamespaceURI()
                        .equals(FedoraNamespaces.KRAMERIUS_URI)
                        && del.getLocalName().equals("replicatedFrom")) {
                    return del.getTextContent();
                }
            }
        }
        return null;
    }

    public static String deepZoomAddress(Document relsExt, String pid) {
        String replicatedFrom = replicatedFrom(relsExt);
        return deepZoomInternal(pid, replicatedFrom);
    }

    private static String deepZoomInternal(String pid, String replicatedFrom) {
        if (replicatedFrom != null) {
            int indexOf = replicatedFrom.indexOf("/handle/");
            String app = replicatedFrom.substring(0, indexOf);
            return app + "/deepZoom/" + pid;
        } else
            return null;
    }
}
