package indexer;

import org.dom4j.*;

public class FoxmlBuilder {

    public static final Namespace NS_FOXML = new Namespace("foxml", "info:fedora/fedora-system:def/foxml#");
    public static final Namespace NS_MODS = new Namespace("mods", "http://www.loc.gov/mods/v3");
    public static final Namespace NS_OAI_DC = new Namespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
    public static final Namespace NS_DC = new Namespace("dc", "http://purl.org/dc/elements/1.1/");
    public static final Namespace NS_RDF = new Namespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    public static final Namespace NS_REL = new Namespace("rel", "http://www.nsdl.org/ontologies/relationships#");
    public static final Namespace NS_FEDORA_MODEL = new Namespace("fedora-model", "info:fedora/fedora-system:def/model#");
    public static final Namespace NS_EMPTY = new Namespace("", "");


    private String pid;
    private String modsXmlDoc;
    private String dcXmlDoc;
    private String relsExtXmlDoc;

    public FoxmlBuilder withPid(String pid) {
        this.pid = pid;
        return this;
    }

    public FoxmlBuilder withMods(String modsXmlDoc) {
        this.modsXmlDoc = modsXmlDoc;
        return this;
    }

    public FoxmlBuilder withDc(String dcXmlDoc) {
        this.dcXmlDoc = dcXmlDoc;
        return this;
    }

    public FoxmlBuilder withRelsExt(String relsExtXmlDoc) {
        this.relsExtXmlDoc = relsExtXmlDoc;
        return this;
    }

    Document build() throws DocumentException {
        Document document = DocumentHelper.createDocument();
        Element digitalObjectEl = document.addElement(new QName("digitalObject", NS_FOXML));
        digitalObjectEl.addAttribute("PID", pid);
        appendMods(digitalObjectEl);
        appendDc(digitalObjectEl);
        appendRelsExt(digitalObjectEl);
        return document;
    }


    private void appendMods(Element digitalObjectEl) throws DocumentException {
        //MODS
        Element dsEl = addElement(digitalObjectEl, NS_FOXML, "datastream");
        dsEl.addAttribute("ID", "BIBLIO_MODS");
        Element versionEl = addElement(dsEl, NS_FOXML, "datastreamVersion");
        versionEl.addAttribute("ID", "BIBLIO_MODS.0");
        Element contentEl = addElement(versionEl, NS_FOXML, "xmlContent");
        if (modsXmlDoc != null) {
            Document modsDoc = DocumentHelper.parseText(modsXmlDoc);
            modsDoc.accept(new NamespaceChangingVisitor(NS_EMPTY, NS_MODS));
            Element modsRoot = (Element) modsDoc.getRootElement().detach();
            contentEl.add(modsRoot);
        }
    }

    private void appendDc(Element digitalObjectEl) throws DocumentException {
        //DC
        Element dsEl = addElement(digitalObjectEl, NS_FOXML, "datastream");
        dsEl.addAttribute("ID", "DC");
        Element versionEl = addElement(dsEl, NS_FOXML, "datastreamVersion");
        versionEl.addAttribute("ID", "DC.0");
        Element contentEl = addElement(versionEl, NS_FOXML, "xmlContent");
        if (dcXmlDoc != null) {
            Document modsDoc = DocumentHelper.parseText(dcXmlDoc);
            modsDoc.accept(new NamespaceChangingVisitor(NS_EMPTY, NS_DC));
            Element dcRoot = (Element) modsDoc.getRootElement().detach();
            dcRoot.setQName(new QName("dc", NS_OAI_DC));
            contentEl.add(dcRoot);
        }
    }

    private void appendRelsExt(Element digitalObjectEl) throws DocumentException {
        //RELS-EXT
        Element dsEl = addElement(digitalObjectEl, NS_FOXML, "datastream");
        dsEl.addAttribute("ID", "RELS-EXT");
        Element dsVersionEl = addElement(dsEl, NS_FOXML, "datastreamVersion");
        dsVersionEl.addAttribute("ID", "RELS-EXT.0");
        Element contentEl = addElement(dsVersionEl, NS_FOXML, "xmlContent");
        if (relsExtXmlDoc != null) {
            Document relsExtDoc = DocumentHelper.parseText(relsExtXmlDoc);
            relsExtDoc.accept(new VisitorSupport() {
                public void visit(Element element) {
                    String elName = element.getName();
                    //also attributes Description/@about and @resource(for example hasPage/@resource) are in this namespace
                    String[] elsToGoToRdfNs = new String[]{"RDF", "Description", "isMemberOfCollection"};
                    for (String el : elsToGoToRdfNs) {
                        if (elName.equals(el)) {
                            QName newQName = new QName(element.getName(), NS_RDF);
                            element.setQName(newQName);
                        }
                    }
                    //see class KnownRelations
                    String[] elsToGoToRelNs = new String[]{
                            "hasPage", "hasUnit", "hasVolume", "hasItem", "hasSoundUnit", "hasTrack", "containsTrack", "hasIntCompPart", "isOnPage", "contains",
                            "policy", "tiles-url", "file"
                    };
                    for (String el : elsToGoToRelNs) {
                        if (elName.equals(el)) {
                            QName newQName = new QName(element.getName(), NS_REL);
                            element.setQName(newQName);
                        }
                    }
                    String[] elsToGoToFedoraModelNs = new String[]{"hasModel"};
                    for (String el : elsToGoToFedoraModelNs) {
                        if (elName.equals(el)) {
                            QName newQName = new QName(element.getName(), NS_FEDORA_MODEL);
                            element.setQName(newQName);
                        }
                    }

                    String[] attrsToGoToRdfNs = new String[]{"about", "resource"};
                    for (int i = 0; i < element.attributeCount(); i++) {
                        Attribute attribute = element.attribute(i);
                        for (String attrName : attrsToGoToRdfNs) {
                            if (attribute.getName().equals(attrName)) {
                                String value = attribute.getValue();
                                element.remove(attribute);
                                element.addAttribute(new QName(attrName, NS_RDF), value);
                            }
                        }
                    }
                }
            });
            Element relsExtRoot = (Element) relsExtDoc.getRootElement().detach();
            contentEl.add(relsExtRoot);
        }
    }


    private Element addElement(Element parent, Namespace namespace, String name) {
        return parent.addElement(new QName(name, namespace));
    }

}
