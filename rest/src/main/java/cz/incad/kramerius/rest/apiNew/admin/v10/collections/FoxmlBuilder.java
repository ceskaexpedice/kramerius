package cz.incad.kramerius.rest.apiNew.admin.v10.collections;

import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.RepositoryApi;
import org.dom4j.*;

import java.time.LocalDateTime;
import java.util.List;

public class FoxmlBuilder {

    private static final Namespace NS_XSI = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
    private static final Namespace NS_FOXML = new Namespace("foxml", "info:fedora/fedora-system:def/foxml#");
    private static final Namespace NS_FEDORA_MODEL = new Namespace("fedora-model", "info:fedora/fedora-system:def/model#");
    private static final Namespace NS_RDF = new Namespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    private static final Namespace NS_MODS = new Namespace("mods", "http://www.loc.gov/mods/v3");
    private static final Namespace NS_OAI = new Namespace("oai", "http://www.openarchives.org/OAI/2.0/");
    private static final Namespace NS_REL = new Namespace("rel", "http://www.nsdl.org/ontologies/relationships#");

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
        propertyLabel.addAttribute("VALUE", collection.name);
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
        dsModsEl.addAttribute("ID", KrameriusRepositoryApi.KnownDatastreams.BIBLIO_MODS);
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
        dsRelsExtEl.addAttribute("ID", KrameriusRepositoryApi.KnownDatastreams.RELS_EXT);
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
        Element tileInfo = addModsElement(mods, "titleInfo");
        Element title = addModsElement(tileInfo, "title");
        title.addText(collection.name);
        if (collection.description != null) {
            Element abstractEl = addModsElement(mods, "abstract");
            abstractEl.addText(collection.description);
        }
        if (collection.content != null) {
            //TODO: escaping (muze tam byt html)
            Element note = addModsElement(mods, "note");
            note.addText(collection.content);
        }
        return document;
    }

    public Document buildRelsExt(Collection collection, List<String> pidsOfItemsInCollection) {
        Document document = DocumentHelper.createDocument();
        Element rdf = document.addElement(new QName("RDF", NS_RDF));
        Element description = rdf.addElement(new QName("Description", NS_RDF));
        description.addAttribute(new QName("about", NS_RDF), "info:fedora/" + collection.pid);
        Element hasModel = description.addElement(new QName("hasModel", NS_FEDORA_MODEL));
        hasModel.addAttribute(new QName("resource", NS_RDF), "info:fedora/model:collection");
        Element itemId = description.addElement(new QName("itemID", NS_OAI));
        itemId.addText(collection.pid);
        Element policy = description.addElement(new QName("policy", NS_REL));
        policy.addText("policy:public");
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
}
