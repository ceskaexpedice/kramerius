package cz.incad.kramerius.rest.oai.metadata.decorators.impl;

import cz.incad.kramerius.rest.oai.metadata.decorators.MetadataDecorator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.w3c.dom.*;

public class AddCreatedFromDateDecorator implements MetadataDecorator {

    private static final String DC_NS = "http://purl.org/dc/elements/1.1/";
    private static final String DCTERMS_NS = "http://purl.org/dc/terms/";
    private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    @Override
    public Document decorate(Document dc, Document mods) {
        Element root = dc.getDocumentElement();
        NodeList dateNodes = root.getElementsByTagNameNS(DC_NS, "date");

        boolean createdAppended = false;

        for (int i = 0; i < dateNodes.getLength(); i++) {
            String raw = dateNodes.item(i).getTextContent().trim();

            // Odstraň hranaté závorky a tečky
            String clean = raw.replaceAll("[\\[\\].]", "");

            // Validní rok (např. 1993, 2017)
            if (clean.matches("\\d{4}")) {
                if (!createdAppended) {
                    appendCreated(dc, "#" + clean);
                    createdAppended = true;
                }
            }

            // Rozsah (např. 1974-1987 nebo 1974–1987)
            else if (clean.matches("\\d{4}[-–]\\d{4}")) {
                if (!createdAppended) {
                    appendCreated(dc, "#" + clean);
                    createdAppended = true;
                }
            }

            if (createdAppended) {
                break;
            }
        }

        return dc;
    }

    private void appendCreated(Document dc, String value) {
        Element created = dc.createElementNS(DCTERMS_NS, "dcterms:created");
        created.setAttributeNS(RDF_NS, "rdf:resource", value);
        dc.getDocumentElement().appendChild(created);
    }
}
