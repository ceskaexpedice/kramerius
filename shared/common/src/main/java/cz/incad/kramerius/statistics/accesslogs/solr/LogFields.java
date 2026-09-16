package cz.incad.kramerius.statistics.accesslogs.solr;

import cz.incad.kramerius.pdf.utils.ModsUtils;
import cz.incad.kramerius.utils.DCUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

class LogFields {

    private static final String FOXML_NS =
            "info:fedora/fedora-system:def/foxml#";

    private final Document foXml;

    private Document dc;
    private Document mods;
    private Document relsExt;

    LogFields(Document foXml) {
        this.foXml = foXml;
    }

    // -------- DC -----------------------
    Document getDC() {
        if (dc == null) {
            dc = getDatastreamContent("DC");
        }
        return dc;
    }

    String getDCDate() {
        Document dc1 = getDC();
        if (dc1 == null) {
            return null;
        }
        Object dateFromDC = DCUtils.dateFromDC(dc1);
        if (dateFromDC == null) {
            return null;
        }
        return dateFromDC.toString();
    }

    String getDCLanguage() {
        Document dc1 = getDC();
        if (dc1 == null) {
            return null;
        }
        Object languageFromDC = DCUtils.languageFromDC(dc1);
        if (languageFromDC == null) {
            return null;
        }
        return languageFromDC.toString();
    }

    String getDCTitle() {
        Document dc1 = getDC();
        if (dc1 == null) {
            return null;
        }
        Object titleFromDC = DCUtils.titleFromDC(dc1);
        if (titleFromDC == null) {
            return null;
        }
        return titleFromDC.toString();
    }

    String[] getDCCreators() {
        Document dc1 = getDC();
        if (dc1 == null) {
            return null;
        }
        String[] creatorsFromDC = DCUtils.creatorsFromDC(dc);
        return creatorsFromDC;
    }

    String[] getDCPublishers() {
        Document dc1 = getDC();
        if (dc1 == null) {
            return null;
        }
        String[] publishersFromDC = DCUtils.publishersFromDC(dc);
        return publishersFromDC;
    }

    // -------- MODS -----------------------
    Document getMods() {
        if (mods == null) {
            mods = getDatastreamContent("BIBLIO_MODS");
        }
        return mods;
    }

    Map<String, List<String>> getModsIdentifiers() throws XPathExpressionException, IOException {
        if (mods == null) {
            mods = getDatastreamContent("BIBLIO_MODS");
        }
        Map<String, List<String>> identifiers = ModsUtils.identifiersFromMods(mods);
        return identifiers;
    }

    // -------- RELS-EXT -----------------------
    Document getRelsExt() {
        if (relsExt == null) {
            relsExt = getDatastreamContent("RELS-EXT");
        }
        return relsExt;
    }

    String getRelsExtModel() {
        Document relsExt = getRelsExt();
        if (relsExt == null) {
            return null;
        }
        NodeList models = relsExt.getElementsByTagNameNS(
                "info:fedora/fedora-system:def/model#",
                "hasModel"
        );
        if (models.getLength() == 0) {
            return null;
        }
        Element model = (Element) models.item(0);
        String resource = model.getAttributeNS(
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
                "resource"
        );
        if (resource == null || resource.isEmpty()) {
            return null;
        }
        int index = resource.lastIndexOf(':');
        return index >= 0
                ? resource.substring(index + 1)
                : resource;
    }

    private Document getDatastreamContent(String id) {
        Element digitalObject = foXml.getDocumentElement();

        for (Node node = digitalObject.getFirstChild();
             node != null;
             node = node.getNextSibling()) {

            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Element element = (Element) node;

            if (!FOXML_NS.equals(element.getNamespaceURI())
                    || !"datastream".equals(element.getLocalName())) {
                continue;
            }

            if (!id.equals(element.getAttribute("ID"))) {
                continue;
            }

            // datastream -> datastreamVersion -> xmlContent
            for (Node versionNode = element.getFirstChild();
                 versionNode != null;
                 versionNode = versionNode.getNextSibling()) {

                if (versionNode.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                Element version = (Element) versionNode;

                if (!FOXML_NS.equals(version.getNamespaceURI())
                        || !"datastreamVersion".equals(version.getLocalName())) {
                    continue;
                }

                for (Node contentNode = version.getFirstChild();
                     contentNode != null;
                     contentNode = contentNode.getNextSibling()) {

                    if (contentNode.getNodeType() != Node.ELEMENT_NODE) {
                        continue;
                    }

                    Element content = (Element) contentNode;

                    if (!FOXML_NS.equals(content.getNamespaceURI())
                            || !"xmlContent".equals(content.getLocalName())) {
                        continue;
                    }

                    // xmlContent obsahuje právě jeden relevantní element:
                    // oai_dc:dc, mods:modsCollection nebo rdf:RDF
                    for (Node dataNode = content.getFirstChild();
                         dataNode != null;
                         dataNode = dataNode.getNextSibling()) {

                        if (dataNode.getNodeType() != Node.ELEMENT_NODE) {
                            continue;
                        }

                        return createDocument((Element) dataNode);
                    }

                    return null;
                }
            }

            return null;
        }

        return null;
    }

    private Document createDocument(Element root) {
        Document document = root.getOwnerDocument()
                .getImplementation()
                .createDocument(null, null, null);

        document.appendChild(document.importNode(root, true));

        return document;
    }
}
