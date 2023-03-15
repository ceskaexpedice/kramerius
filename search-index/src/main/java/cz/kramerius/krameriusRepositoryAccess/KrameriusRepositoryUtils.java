package cz.kramerius.krameriusRepositoryAccess;

import cz.kramerius.shared.Dom4jUtils;
import cz.kramerius.shared.Pair;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class KrameriusRepositoryUtils {

    public static final Logger LOGGER = Logger.getLogger(KrameriusRepositoryUtils.class.getName());

    /**
     * Replaces AbstractFedoraAccess.getKrameriusModelName(relsExtDoc)
     *
     * @param relsExt input document (namespace aware)
     * @return
     */
    public static String extractKrameriusModelName(Document relsExt) {
        Attribute rdfModelResource = (Attribute) Dom4jUtils.buildXpath("rdf:Description/model:hasModel/@rdf:resource").selectSingleNode(relsExt.getRootElement());
        if (rdfModelResource != null) {
            String model = rdfModelResource.getValue().substring("info:fedora/model:".length());
            return model;
        } else {
            return null;
        }
    }

    /**
     * Extacts pids of children, own-children and foster-children separate
     *
     * @param relsExt
     * @return pair of lists of pids; first list contains own-children (has relations), second list contains foster-children (is-on relations)
     */
    public static Pair<List<String>, List<String>> extractChildren(Document relsExt) {
        List<Node> children = Dom4jUtils.buildXpath("rdf:Description/*").selectNodes(relsExt.getRootElement());
        List<String> own = new ArrayList<>();
        List<String> foster = new ArrayList<>();
        for (Node child : children) {
            if (child instanceof Element) {
                Element childEl = (Element) child;
                String localName = childEl.getName();
                Attribute resourceAttr = childEl.attribute("resource");
                if (resourceAttr != null) {
                    String pid = resourceAttr.getValue().substring("info:fedora/".length());
                    if (isOwningRelation(localName)) {
                        own.add(pid);
                    } else if (isNotOwningRelation(localName)) {
                        foster.add(pid);
                    }
                }
            }
        }
        return new Pair(own, foster);
    }

    private static boolean isOwningRelation(String localName) {
        String[] relations = {
                "hasPage",
                //monograph-type trees only
                "hasUnit",
                //periodicals trees only
                "hasVolume",
                "hasItem",
                //sound-recording trees only
                "hasSoundUnit",
                "hasTrack", "containsTrack", //hasTrack should replace containsTrack in the future, but we do recognize containsTrack for now
                //collections cannot contain other collections with owning (has-) relation, because one collection could be contained in two collections, i.e. it's not tree but more general graph
                //other
                "hasIntCompPart", //internal part, possibly article
        };
        for (String relation : relations) {
            if (relation.equals(localName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNotOwningRelation(String localName) {
        String[] relations = {
                "isOnPage", //article/internalPart isOnPage of multiple pages
                "contains" //collection contains monograph/page/graphic/issue/whatever, even another collection
        };
        for (String relation : relations) {
            if (relation.equals(localName)) {
                return true;
            }
        }
        return false;
    }

    /*
     Replaces AbstractFedoraAccess.getDonator(relsExtDoc)
     */
    public String extractDonator(Document relsExt) {
        /*try {
            Element foundElement = XMLUtils.findElement(relsExt.getDocumentElement(), "hasDonator",
                    FedoraNamespaces.KRAMERIUS_URI);
            if (foundElement != null) {
                String sform = foundElement.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                PIDParser pidParser = new PIDParser(sform);
                pidParser.disseminationURI();
                return pidParser.getObjectId();
            } else {
                return "";
            }
        } catch (DOMException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        } catch (LexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }*/
        throw new UnsupportedOperationException();
    }
}
