package cz.incad.kramerius.rest.apiNew.admin.v70.collections;

import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.rest.apiNew.admin.v70.FoxmlBuilder;
import cz.incad.kramerius.utils.StringUtils;

import org.apache.commons.lang.StringEscapeUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

import java.text.BreakIterator;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class CollectionsFoxmlBuilder extends FoxmlBuilder {
    public Document buildFoxml(Collection collection, List<String> pidsOfItemsInCollection) {
        Document document = DocumentHelper.createDocument();
        Element digitalObject = document.addElement(new QName("digitalObject", NS_FOXML));
        digitalObject.addAttribute("VERSION", "1.1");
        digitalObject.addAttribute("PID", collection.pid);
        digitalObject.addAttribute(new QName("schemaLocation", NS_XSI), "info:fedora/fedora-system:def/foxml# http://www.fedora.info/definitions/1/0/foxml1-1.xsd");
        Element objectProperties = addFoxmlElement(digitalObject, "objectProperties");
        //label
//        Element propertyLabel = addFoxmlElement(objectProperties, "property");
//        propertyLabel.addAttribute("NAME", "info:fedora/fedora-system:def/model#label");
//        propertyLabel.addAttribute("VALUE", collection.nameCz != null ? collection.nameCz : collection.nameEn);
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
        
        if (collection.names.size() > 0) {
            collection.names.keySet().forEach(key-> {

                Element tileInfo = addModsElement(mods, "titleInfo");
                tileInfo.addAttribute("lang", key);
                Element title = addModsElement(tileInfo, "title");
                title.addText(collection.names.get(key));
            });
        }
        

        if (collection.descriptions.size() > 0) {
            collection.descriptions.keySet().forEach(key-> {
                Element abstractEl = addModsElement(mods, "abstract");
                abstractEl.addAttribute("lang", key);
                abstractEl.addText(collection.descriptions.get(key));
            });
        }

        

        if (collection.contents.size() > 0) {
            collection.contents.keySet().forEach(key-> {
                Element note = addModsElement(mods, "note");
                note.addAttribute("lang", key);
                note.addText(StringEscapeUtils.escapeHtml(collection.contents.get(key)));
            });
        }
        if (collection.keywords.size() > 0) {
            collection.keywords.keySet().forEach(key-> {
                List<String> keywords = collection.keywords.get(key);
                for (String keyword : keywords) {

                    Element subjectElm = addModsElement(mods, "subject");
                    subjectElm.addAttribute("lang", key);
                    
                    Element topic = addModsElement(subjectElm, "topic");
                    topic.addText(keyword);
                }
                
            });
        }

        
        if (collection.author != null && StringUtils.isAnyString(collection.author)) {
            
            List<String> authorParts = new ArrayList<>();
            BreakIterator wordIterator =
                    BreakIterator.getWordInstance(Locale.getDefault());

            wordIterator.setText(collection.author);
            
            // Iterace přes jednotlivá slova
            int start = wordIterator.first();
            for (int end = wordIterator.next(); end != BreakIterator.DONE; start = end, end = wordIterator.next()) {
                String word = collection.author.substring(start, end);
                authorParts.add(word);
            }
            
            if (authorParts.size() > 0) {

                Element personalName = addModsElement(mods, "name");
                personalName.addAttribute("type", "personal");
                personalName.addAttribute("usage", "primary");
                
                Element personalNamePart1 = addModsElement(personalName, "namePart");
                personalNamePart1.addAttribute("type", "family");
                personalNamePart1.setText(authorParts.get(0));

                Element personalNamePart2 = addModsElement(personalName, "namePart");
                personalNamePart2.addAttribute("type", "given");
                personalNamePart2.setText(authorParts.subList(1, authorParts.size()).stream().collect(Collectors.joining(" ")));
                
            }
            
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
