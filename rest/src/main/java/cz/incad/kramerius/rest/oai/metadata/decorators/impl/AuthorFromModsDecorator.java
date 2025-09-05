package cz.incad.kramerius.rest.oai.metadata.decorators.impl;

import cz.incad.kramerius.rest.oai.metadata.decorators.MetadataDecorator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.logging.Logger;

public class AuthorFromModsDecorator implements MetadataDecorator {

    public static final Logger LOGGER = Logger.getLogger(AuthorFromModsDecorator.class.getName());

    public AuthorFromModsDecorator() {

    }

    @Override
    public Document decorate(Document dc, Document modsDoc) {
        if (modsDoc != null) {
            LOGGER.info("Processing mods");

            NodeList nameElements = modsDoc.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "name");

            for (int i = 0; i < nameElements.getLength(); i++) {
                Element name = (Element) nameElements.item(i);
                String type = name.getAttribute("type");

                if (!"personal".equals(type)) continue;

                LOGGER.info("Found personal");
                NodeList roles = name.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "roleTerm");
                boolean isAuthor = false;

                for (int j = 0; j < roles.getLength(); j++) {
                    Element role = (Element) roles.item(j);
                    if ("aut".equals(role.getTextContent().trim())) {
                        isAuthor = true;
                        LOGGER.info("Found Author");
                        break;
                    }
                }

                if (isAuthor) {
                    LOGGER.info("Author in MODS");
                    NodeList ids = name.getElementsByTagNameNS("http://www.loc.gov/mods/v3", "nameIdentifier");
                    if (ids.getLength() > 0) {
                        String id = ids.item(0).getTextContent().trim();
                        String uri = "https://aleph.nkp.cz/dai/" + id;
                        LOGGER.info("Fond id = "+id);

                        Element creator = dc.createElementNS("http://purl.org/dc/elements/1.1/", "dc:creator");
                        creator.setAttributeNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:resource", uri);
                        dc.getDocumentElement().appendChild(creator);
                    } else {
                        LOGGER.info("No id");
                    }
                }
            }
        }

        return dc;
    }
}
