package cz.incad.kramerius.rest.apiNew.admin.v10.collections;

import org.dom4j.*;

public class FoxmlBuilder {

    private static final Namespace NS_FOXML = new Namespace("foxml", "info:fedora/fedora-system:def/foxml#");
    private static final Namespace NS_MODS = new Namespace("mods", "http://www.loc.gov/mods/v3");
    private static final Namespace NS_OAI_DC = new Namespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
    private static final Namespace NS_DC = new Namespace("dc", "http://purl.org/dc/elements/1.1/");
    private static final Namespace NS_RDF = new Namespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    private static final Namespace NS_REL = new Namespace("rel", "http://www.nsdl.org/ontologies/relationships#");
    private static final Namespace NS_FEDORA_MODEL = new Namespace("fedora-model", "info:fedora/fedora-system:def/model#");
    private static final Namespace NS_EMPTY = new Namespace("", "");

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

    private Element addModsElement(Element parent, String name) {
        return addElement(parent, NS_MODS, name);
    }

    private Element addElement(Element parent, Namespace namespace, String name) {
        return parent.addElement(new QName(name, namespace));
    }
}
