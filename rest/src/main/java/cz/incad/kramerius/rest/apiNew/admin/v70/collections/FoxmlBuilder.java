package cz.incad.kramerius.rest.apiNew.admin.v70.collections;

import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.utils.Dom4jUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

public class FoxmlBuilder {

    private static Logger LOGGER = Logger.getLogger(FoxmlBuilder.class.getName());

    private static final Namespace NS_XSI = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    private static final Namespace NS_FOXML = new Namespace("foxml", "info:fedora/fedora-system:def/foxml#");
    //RELS-EXT
    private static final Namespace NS_RDF = new Namespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    private static final Namespace NS_MODEL = new Namespace("model", "info:fedora/fedora-system:def/model#");
    private static final Namespace NS_REL = new Namespace("rel", "http://www.nsdl.org/ontologies/relationships#");//hasPage, tiles-url, policy
    private static final Namespace NS_OAI = new Namespace("oai", "http://www.openarchives.org/OAI/2.0/");
    //BIBLIO_MODS
    private static final Namespace NS_MODS = new Namespace("mods", "http://www.loc.gov/mods/v3");
    //DC
    private static final Namespace NS_OAI_DC = new Namespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
    private static final Namespace NS_DC = new Namespace("dc", "http://purl.org/dc/elements/1.1/");

    private static final Namespace NS_EMPTY = new Namespace("", "");

    public Document buildFoxml(Collection collection, List<String> pidsOfItemsInCollection) {
        Document document = DocumentHelper.createDocument();
        Element digitalObject = document.addElement(new QName("digitalObject", NS_FOXML));
        digitalObject.addAttribute("VERSION", "1.1");
        digitalObject.addAttribute("PID", collection.pid);
        digitalObject.addAttribute(new QName("schemaLocation", NS_XSI), "info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd");
        Element objectProperties = addFoxmlElement(digitalObject, "objectProperties");
        //label
        Element propertyLabel = addFoxmlElement(objectProperties, "property");
        propertyLabel.addAttribute("NAME", "info:fedora/fedora-system:def/model#label");
        propertyLabel.addAttribute("VALUE", collection.nameCz != null ? collection.nameCz : collection.nameEn);
        //state
        Element propertyState = addFoxmlElement(objectProperties, "property");
        propertyState.addAttribute("NAME", "info:fedora/fedora-system:def/model#state");
        propertyState.addAttribute("VALUE", "Active");
        //created
        LocalDateTime now = LocalDateTime.now();
        Element propertyCreated = addFoxmlElement(objectProperties, "property");
        propertyCreated.addAttribute("NAME", "info:fedora/fedora-system:def/model#createdDate");
        propertyCreated.addAttribute("VALUE", now.format(RepositoryApi.TIMESTAMP_FORMATTER));
        //last modified
        Element propertyLastModified = addFoxmlElement(objectProperties, "property");
        propertyLastModified.addAttribute("NAME", "info:fedora/fedora-system:def/view#lastModifiedDate");
        propertyLastModified.addAttribute("VALUE", now.format(RepositoryApi.TIMESTAMP_FORMATTER));
        //MODS
        Element dsModsEl = addFoxmlElement(digitalObject, "datastream");
        dsModsEl.addAttribute("ID", KrameriusRepositoryApi.KnownDatastreams.BIBLIO_MODS.toString());
        dsModsEl.addAttribute("STATE", "A");
        dsModsEl.addAttribute("CONTROL_GROUP", "X");
        dsModsEl.addAttribute("VERSIONABLE", "true");
        Element dsModsVersionEl = addFoxmlElement(dsModsEl, "datastreamVersion");
        dsModsVersionEl.addAttribute("ID", KrameriusRepositoryApi.KnownDatastreams.BIBLIO_MODS + ".0");
        dsModsVersionEl.addAttribute("MIMETYPE", "application/xml");
        dsModsVersionEl.addAttribute("FORMAT_URI", KrameriusRepositoryApi.KnownXmlFormatUris.BIBLIO_MODS);
        dsModsVersionEl.addAttribute("CREATED", now.format(RepositoryApi.TIMESTAMP_FORMATTER));
        Element modsXmlContent = addFoxmlElement(dsModsVersionEl, "xmlContent");
        modsXmlContent.add(buildMods(collection).getRootElement().detach());
        //RELS-EXT
        Element dsRelsExtEl = addFoxmlElement(digitalObject, "datastream");
        dsRelsExtEl.addAttribute("ID", KrameriusRepositoryApi.KnownDatastreams.RELS_EXT.toString());
        dsRelsExtEl.addAttribute("STATE", "A");
        dsRelsExtEl.addAttribute("CONTROL_GROUP", "X");
        dsRelsExtEl.addAttribute("VERSIONABLE", "true");
        Element dsRelsExtVersionEl = addFoxmlElement(dsRelsExtEl, "datastreamVersion");
        dsRelsExtVersionEl.addAttribute("ID", KrameriusRepositoryApi.KnownDatastreams.RELS_EXT + ".0");
        dsRelsExtVersionEl.addAttribute("MIMETYPE", "application/xml");
        dsRelsExtVersionEl.addAttribute("FORMAT_URI", KrameriusRepositoryApi.KnownXmlFormatUris.RELS_EXT);
        dsRelsExtVersionEl.addAttribute("CREATED", now.format(RepositoryApi.TIMESTAMP_FORMATTER));
        Element relsExtXmlContent = addFoxmlElement(dsRelsExtVersionEl, "xmlContent");
        relsExtXmlContent.add(buildRelsExt(collection, pidsOfItemsInCollection).getRootElement().detach());
        return document;
    }

    public Document buildMods(Collection collection) {
        Document document = DocumentHelper.createDocument();
        Element modsCollection = document.addElement(new QName("modsCollection", NS_MODS));
        Element mods = addModsElement(modsCollection, "mods");
        mods.addAttribute("version", "3.4");
        if (collection.nameCz != null) {
            Element tileInfo = addModsElement(mods, "titleInfo");
            tileInfo.addAttribute("lang", "cze");
            Element title = addModsElement(tileInfo, "title");
            title.addText(collection.nameCz);
        }
        if (collection.nameEn != null) {
            Element tileInfo = addModsElement(mods, "titleInfo");
            tileInfo.addAttribute("lang", "eng");
            Element title = addModsElement(tileInfo, "title");
            title.addText(collection.nameEn);
        }
        if (collection.descriptionCz != null) {
            Element abstractEl = addModsElement(mods, "abstract");
            abstractEl.addAttribute("lang", "cze");
            abstractEl.addText(collection.descriptionCz);
        }
        if (collection.descriptionEn != null) {
            Element abstractEl = addModsElement(mods, "abstract");
            abstractEl.addAttribute("lang", "eng");
            abstractEl.addText(collection.descriptionEn);
        }
        if (collection.contentCz != null) {
            Element note = addModsElement(mods, "note");
            note.addAttribute("lang", "cze");
            note.addText(StringEscapeUtils.escapeHtml(collection.contentCz));
        }
        if (collection.contentEn != null) {
            Element note = addModsElement(mods, "note");
            note.addAttribute("lang", "eng");
            note.addText(StringEscapeUtils.escapeHtml(collection.contentEn));
        }
        return document;
    }

    public Document buildRelsExt(Collection collection, List<String> pidsOfItemsInCollection) {
        Document document = DocumentHelper.createDocument();
        Element rdf = document.addElement(new QName("RDF", NS_RDF));
        Element description = rdf.addElement(new QName("Description", NS_RDF));
        description.addAttribute(new QName("about", NS_RDF), "info:fedora/" + collection.pid);
        Element hasModel = description.addElement(new QName("hasModel", NS_MODEL));
        hasModel.addAttribute(new QName("resource", NS_RDF), "info:fedora/model:collection");
        Element itemId = description.addElement(new QName("itemID", NS_OAI));
        itemId.addText(collection.pid);
        Element policy = description.addElement(new QName("policy", NS_REL));
        policy.addText("policy:public");
        Element standalone = description.addElement(new QName("standalone", NS_REL));
        boolean standaloneBool = collection.standalone != null ? collection.standalone : false;
        standalone.addText(Boolean.toString(standaloneBool));
        if (pidsOfItemsInCollection != null) {
            for (String itemPid : pidsOfItemsInCollection) {
                Element contains = description.addElement(new QName("contains", NS_REL));
                contains.addAttribute(new QName("resource", NS_RDF), "info:fedora/" + itemPid);
            }
        }
        return document;
    }

    private Element addFoxmlElement(Element parent, String name) {
        return addElement(parent, NS_FOXML, name);
    }

    private Element addModsElement(Element parent, String name) {
        return addElement(parent, NS_MODS, name);
    }

    private Element addElement(Element parent, Namespace namespace, String name) {
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
