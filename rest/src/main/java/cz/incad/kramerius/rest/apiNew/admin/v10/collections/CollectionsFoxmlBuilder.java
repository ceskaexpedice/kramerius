package cz.incad.kramerius.rest.apiNew.admin.v10.collections;

import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.rest.apiNew.admin.v10.FoxmlBuilder;
import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

import java.time.LocalDateTime;
import java.util.List;

public class CollectionsFoxmlBuilder extends FoxmlBuilder {
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
}
