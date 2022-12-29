package cz.incad.kramerius.rest.apiNew.admin.v10;

import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.utils.Dom4jUtils;
import org.dom4j.*;

import java.util.logging.Logger;

public class FoxmlBuilder {

    private static Logger LOGGER = Logger.getLogger(FoxmlBuilder.class.getName());

    public static final Namespace NS_XSI = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    public static final Namespace NS_FOXML = new Namespace("foxml", "info:fedora/fedora-system:def/foxml#");
    //RELS-EXT
    public static final Namespace NS_RDF = new Namespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    public static final Namespace NS_MODEL = new Namespace("model", "info:fedora/fedora-system:def/model#");
    public static final Namespace NS_REL = new Namespace("rel", "http://www.nsdl.org/ontologies/relationships#");//hasPage, tiles-url, policy
    public static final Namespace NS_OAI = new Namespace("oai", "http://www.openarchives.org/OAI/2.0/");
    //BIBLIO_MODS
    public static final Namespace NS_MODS = new Namespace("mods", "http://www.loc.gov/mods/v3");
    //DC
    public static final Namespace NS_OAI_DC = new Namespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
    public static final Namespace NS_DC = new Namespace("dc", "http://purl.org/dc/elements/1.1/");

    public static final Namespace NS_EMPTY = new Namespace("", "");

    public Element addFoxmlElement(Element parent, String name) {
        return addElement(parent, NS_FOXML, name);
    }

    public Element addModsElement(Element parent, String name) {
        return addElement(parent, NS_MODS, name);
    }

    public Element addElement(Element parent, Namespace namespace, String name) {
        return parent.addElement(new QName(name, namespace));
    }

    /**
     * @return true if new relation has been added, false if it was already present
     */
    public boolean appendRelationToRelsExt(String ownerPid, Document relsExt, KrameriusRepositoryApi.KnownRelations relation, String newItemPid) {
        Element description = (Element) Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description").selectSingleNode(relsExt.getRootElement());
        Element relationEl = (Element) Dom4jUtils.buildXpath(String.format("rel:%s[@rdf:resource='info:fedora/%s']", relation.toString(), newItemPid)).selectSingleNode(description);
        if (relationEl == null) {
            Element element = description.addElement(new QName(relation.toString(), NS_REL));
            element.addAttribute(new QName("resource", NS_RDF), "info:fedora/" + newItemPid);
            return true;
        } else {
            LOGGER.warning(String.format("Relation %s:%s already found in rels-ext of %s, ignoring", relation, newItemPid, ownerPid));
            return false;
        }
    }

    /**
     * @return true if new relation has been added, false if it was already present
     */
    public boolean appendRelationToRelsExt(String ownerPid, Document relsExt, String relation, String newItemPid) {
        Element description = (Element) Dom4jUtils.buildXpath("/rdf:RDF/rdf:Description").selectSingleNode(relsExt.getRootElement());
        Element relationEl = (Element) Dom4jUtils.buildXpath(String.format("rel:%s[@rdf:resource='info:fedora/%s']", relation, newItemPid)).selectSingleNode(description);
        if (relationEl == null) {
            Element element = description.addElement(new QName(relation.toString(), NS_REL));
            element.addAttribute(new QName("resource", NS_RDF), "info:fedora/" + newItemPid);
            return true;
        } else {
            LOGGER.warning(String.format("Relation %s:%s already found in rels-ext of %s, ignoring", relation, newItemPid, ownerPid));
            return false;
        }
    }

    /**
     * @return true if relation has been removed, false if it was not there
     */
    public boolean removeRelationFromRelsExt(String ownerPid, Document relsExt, KrameriusRepositoryApi.KnownRelations relation, String itemPid) {
        String relationXpath = String.format("/rdf:RDF/rdf:Description/rel:%s[@rdf:resource='info:fedora/%s']", relation.toString(), itemPid);
        Element relationEl = (Element) Dom4jUtils.buildXpath(relationXpath).selectSingleNode(relsExt.getRootElement());
        if (relationEl != null) {
            relationEl.detach();
            return true;
        } else {
            LOGGER.warning(String.format("Relation %s:%s not found in rels-ext of %s, ignoring", relation, itemPid, ownerPid));
            return false;
        }
    }
}
