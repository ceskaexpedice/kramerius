/*
 * Copyright (C) 2016 Pavel Stastny
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.fedora.om.impl;

import com.qbizm.kramerius.imp.jaxb.*;
import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.fedora.om.NotFoundInRepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryDatastream;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.fedora.utils.Fedora4Utils;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.XMLUtils.ElementsFilter;
import cz.incad.kramerius.utils.pid.PIDParser;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.DefaultTemplateLexer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.solr.client.solrj.SolrServerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * @author pavels
 */
public class AkubraObject implements RepositoryObject {

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final Logger LOGGER = Logger.getLogger(AkubraObject.class.getName());
    public static final String RDF_DESCRIPTION_ELEMENT = "Description";
    public static final String RDF_CONTAINS_ELEMENT = "contains";
    public static final String RDF_TYPE_ELEMENT = "type";
    public static final String RDF_ELEMENT = "RDF";
    private AkubraDOManager manager;
    private String pid;
    DigitalObject digitalObject;
    private ProcessingIndexFeeder feeder;


    public AkubraObject(AkubraDOManager manager, String pid, DigitalObject digitalObject, ProcessingIndexFeeder feeder) {
        super();
        this.manager = manager;
        this.pid = pid;
        this.feeder = feeder;
        this.digitalObject = digitalObject;
    }


    public String getPid() {
        return pid;
    }


    @Override
    public String getPath() {
        return pid;
    }


    private DatastreamType createDatastreamHeader(String streamId, String mimeType, String controlGroup) throws RepositoryException {
        List<DatastreamType> datastreamList = digitalObject.getDatastream();
        Iterator<DatastreamType> iterator = datastreamList.iterator();
        while (iterator.hasNext()) {
            DatastreamType datastreamType = iterator.next();
            if (streamId.equals(datastreamType.getID())) {
                iterator.remove();
            }
        }
        DatastreamType datastreamType = new DatastreamType();
        datastreamType.setID(streamId);
        datastreamType.setCONTROLGROUP(controlGroup);
        datastreamType.setSTATE(StateType.A);
        datastreamType.setVERSIONABLE(false);
        List<DatastreamVersionType> datastreamVersion = datastreamType.getDatastreamVersion();
        DatastreamVersionType datastreamVersionType = new DatastreamVersionType();
        datastreamVersionType.setID(streamId + ".0");
        datastreamVersionType.setCREATED(AkubraUtils.getCurrentXMLGregorianCalendar());
        datastreamVersionType.setMIMETYPE(mimeType);
        String formatUri = FedoraUtils.getFormatUriForDS(streamId);
        if (formatUri != null) {
            datastreamVersionType.setFORMATURI(formatUri);
        }
        datastreamVersion.add(datastreamVersionType);
        datastreamList.add(datastreamType);
        return datastreamType;
    }

    @Override
    public RepositoryDatastream createRedirectedStream(String streamId, String url, String mimeType) throws RepositoryException {
        DatastreamType datastreamType = createDatastreamHeader(streamId, mimeType, "E");

        ContentLocationType contentLocationType = new ContentLocationType();
        contentLocationType.setTYPE("URL");
        contentLocationType.setREF(url);
        datastreamType.getDatastreamVersion().get(0).setContentLocation(contentLocationType);

        RepositoryDatastream ds = new AkubraDatastream(manager, datastreamType, streamId, RepositoryDatastream.Type.INDIRECT);

        try {
            manager.commit(digitalObject, streamId);
            return ds;
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }


    @Override
    public List<RepositoryDatastream> getStreams() throws RepositoryException {
        List<RepositoryDatastream> list = new ArrayList<>();
        List<DatastreamType> datastreamList = digitalObject.getDatastream();
        for (DatastreamType datastreamType : datastreamList) {
            list.add(new AkubraDatastream(manager, datastreamType, datastreamType.getID(), controlGroup2Type(datastreamType.getCONTROLGROUP())));
        }
        return list;
    }


    private AkubraDatastream.Type controlGroup2Type(String controlGroup) {
        if ("E".equals(controlGroup) || "R".equals(controlGroup)) {
            return RepositoryDatastream.Type.INDIRECT;
        } else {
            return RepositoryDatastream.Type.DIRECT;
        }
    }


    @Override
    public void deleteStream(String streamId) throws RepositoryException {
        try {
            manager.deleteStream(pid, streamId);
            if (streamId.equals(FedoraUtils.RELS_EXT_STREAM)) {
                try {
                    this.feeder.deleteByRelationsForPid(this.getPid());
                } catch (Throwable th) {
                    LOGGER.log(Level.SEVERE, "Cannot update processing index for "+ pid + " - reindex manually.", th);
                }
            }
        } catch (IOException e) {
            throw new RepositoryException("Cannot delete  streamId " + streamId, e);
        }
    }


    @Override
    public RepositoryDatastream createStream(String streamId, String mimeType, InputStream input) throws RepositoryException {
        DatastreamType datastreamType = createDatastreamHeader(streamId, mimeType, "X");

        XmlContentType xmlContentType = new XmlContentType();
        xmlContentType.getAny().add(elementFromInputStream(input));
        datastreamType.getDatastreamVersion().get(0).setXmlContent(xmlContentType);

        RepositoryDatastream ds = new AkubraDatastream(manager, datastreamType, streamId, RepositoryDatastream.Type.DIRECT);

        try {
            manager.commit(digitalObject, streamId);
            if (streamId.equals(FedoraUtils.RELS_EXT_STREAM)) {
                try {
                    // process rels-ext and create all children and relations
                    this.feeder.deleteByRelationsForPid(pid);
                    input.reset();
                    rebuildProcessingIndexImpl(input);
                } catch (Throwable th) {
                    LOGGER.log(Level.SEVERE, "Cannot update processing index for "+ pid + " - reindex manually.", th);
                }
            }
            return ds;
        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
    }

    public static Element elementFromInputStream(InputStream in) {
        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;
        Document ret = null;

        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            ret = builder.parse(new InputSource(in));
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ret != null) {
            return ret.getDocumentElement();
        } else {
            return null;
        }
    }


    public RepositoryDatastream createManagedStream(String streamId, String mimeType, InputStream input) throws RepositoryException {
        DatastreamType datastreamType = createDatastreamHeader(streamId, mimeType, "M");

        try {
            datastreamType.getDatastreamVersion().get(0).setBinaryContent(IOUtils.toByteArray(input));

            RepositoryDatastream ds = new AkubraDatastream(manager, datastreamType, streamId, RepositoryDatastream.Type.DIRECT);


            manager.commit(digitalObject, streamId);
            return ds;
        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
    }

    /**
     * Process one relation and feed processing index
     */
    public void processRELSEXTRelationAndFeedProcessingIndex(String object, String localName) throws RepositoryException {
        if (localName.equals("hasModel")) {
            try {
                boolean dcStreamExists = this.streamExists(FedoraUtils.DC_STREAM);
                // TODO: Biblio mods ukladat jinam ??
                boolean modsStreamExists = this.streamExists(FedoraUtils.BIBLIO_MODS_STREAM);
                if (dcStreamExists || modsStreamExists) {
                    try {
                        //LOGGER.info("DC or BIBLIOMODS exists");
                        if (dcStreamExists) {
                            List<String> dcTList = dcTitle();
                            if (dcTList != null && !dcTList.isEmpty()) {
                                this.indexDescription(object, dcTList.stream().collect(Collectors.joining(" ")));
                            } else {
                                this.indexDescription(object, "");
                            }
                        } else if (modsStreamExists) {
                            // czech title or default
                            List<String> modsTList = modsTitle("cze");
                            if (modsTList != null && !modsTList.isEmpty()) {
                                this.indexDescription(object, modsTList.stream().collect(Collectors.joining(" ")), ProcessingIndexFeeder.TitleType.mods);
                            } else {
                                this.indexDescription(object, "");
                            }
                        }
                    } catch (ParserConfigurationException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        this.indexDescription(object, "");
                    } catch (SAXException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                        this.indexDescription(object, "");
                    }
                } else {
                    LOGGER.info("Index description without dc or mods");
                    this.indexDescription(object, "");
                }
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for "+ pid + " - reindex manually.", th);
            }
        } else {
            try {
                this.indexRelation(localName, object);
            } catch (Throwable th) {
                LOGGER.log(Level.SEVERE, "Cannot update processing index for "+ pid + " - reindex manually.", th);
            }
        }
    }


    private List<String> dcTitle() throws RepositoryException, ParserConfigurationException, SAXException, IOException {
        InputStream stream = this.getStream(FedoraUtils.DC_STREAM).getContent();
        Element title = XMLUtils.findElement(XMLUtils.parseDocument(stream, true).getDocumentElement(), "title", FedoraNamespaces.DC_NAMESPACE_URI);
        return title != null ? Arrays.asList(title.getTextContent()) : new ArrayList<>();
    }

    private List<String> modsTitle(String lang) throws RepositoryException, ParserConfigurationException, SAXException, IOException {
        InputStream stream = this.getStream(FedoraUtils.BIBLIO_MODS_STREAM).getContent();
        Element docElement = XMLUtils.parseDocument(stream, true).getDocumentElement();

        List<Element> elements = XMLUtils.getElementsRecursive(docElement, new XMLUtils.ElementsFilter() {
            @Override
            public boolean acceptElement(Element element) {
                if (element.getNamespaceURI().equals(FedoraNamespaces.BIBILO_MODS_URI)) {
                    if (element.getLocalName().equals("title") && element.hasAttribute("lang") && element.getAttribute("lang").equals("cze")) {
                        return true;
                    }
                }
                return false;
            }
        });


        if (elements.isEmpty()) {
            elements = XMLUtils.getElementsRecursive(docElement, new XMLUtils.ElementsFilter() {
                @Override
                public boolean acceptElement(Element element) {
                    if (element.getNamespaceURI().equals(FedoraNamespaces.BIBILO_MODS_URI)) {
                        // TODO: Change it  
                        if (element.getLocalName().equals("title")) {
                            return true;
                        }
                    }
                    return false;
                }
            });

        }

        return elements.stream().map(Element::getTextContent).collect(Collectors.toList());

    }


    private void indexRelation(String localName, String object) throws IOException, SolrServerException {
        this.feeder.feedRelationDocument(this.getPid(), localName, object);
    }

    private void indexDescription(String model, String title, ProcessingIndexFeeder.TitleType ttype) throws IOException, SolrServerException {
        this.feeder.feedDescriptionDocument(this.getPid(), model, title.trim(), AkubraUtils.getAkubraInternalId(this.getPid()), new Date(), ttype);
    }

    private void indexDescription(String model, String title) throws IOException, SolrServerException {
        this.feeder.feedDescriptionDocument(this.getPid(), model, title.trim(), AkubraUtils.getAkubraInternalId(this.getPid()), new Date());
    }

    public void deleteProcessingIndex() throws IOException, SolrServerException {
        feeder.deleteByPid(this.getPid());
    }

    @Override
    public boolean streamExists(String streamId) throws RepositoryException {
        return AkubraUtils.streamExists(digitalObject, streamId);
    }

    @Override
    public RepositoryDatastream getStream(String streamId) throws RepositoryException {
        List<DatastreamType> datastreamList = digitalObject.getDatastream();
        for (DatastreamType datastreamType : datastreamList) {
            if (streamId.equals(datastreamType.getID())) {
                return new AkubraDatastream(manager, datastreamType, datastreamType.getID(), controlGroup2Type(datastreamType.getCONTROLGROUP()));
            }
        }
        return null;
    }

    @Override
    public void updateSPARQL(String sparql) throws RepositoryException {

    }

    @Override
    public Date getLastModified() throws RepositoryException {
        try {
            return AkubraUtils.getLastModified(digitalObject);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public Document getMetadata() throws RepositoryException {
        return null;
    }


    @Override
    public InputStream getFoxml() throws RepositoryException {
        try {
            return manager.retrieveObject(pid);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }


    @Override
    public void addRelation(String relation, String namespace, String targetRelation) throws RepositoryException {
        try {
            RepositoryDatastream stream = this.getStream(FedoraUtils.RELS_EXT_STREAM);
            Document document = XMLUtils.parseDocument(stream.getContent(), true);
            Element rdfDesc = XMLUtils.findElement(document.getDocumentElement(), RDF_DESCRIPTION_ELEMENT, FedoraNamespaces.RDF_NAMESPACE_URI);
            Element subElm = document.createElementNS(namespace, relation);
            subElm.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "rdf:resource", targetRelation);
            rdfDesc.appendChild(subElm);
            changeRelations(document);
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (TransformerException e) {
            throw new RepositoryException(e);
        }
    }

    private void changeRelations(Document document) throws TransformerException, RepositoryException {
        StringWriter stringWriter = new StringWriter();
        XMLUtils.print(document, stringWriter);

        this.deleteStream(FedoraUtils.RELS_EXT_STREAM);
        this.createStream(FedoraUtils.RELS_EXT_STREAM, "text/xml", new ByteArrayInputStream(stringWriter.toString().getBytes(Charset.forName("UTF-8"))));
    }

    @Override
    public void addLiteral(String relation, String namespace, String value) throws RepositoryException {
        // only  RELS_EXT
        try {
            RepositoryDatastream stream = this.getStream(FedoraUtils.RELS_EXT_STREAM);
            Document document = XMLUtils.parseDocument(stream.getContent(), true);
            Element rdfDesc = XMLUtils.findElement(document.getDocumentElement(), RDF_DESCRIPTION_ELEMENT, FedoraNamespaces.RDF_NAMESPACE_URI);
            Element subElm = document.createElementNS(namespace, relation);
            subElm.setTextContent(value);
            rdfDesc.appendChild(subElm);
            changeRelations(document);
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (TransformerException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void removeRelation(String relation, String namespace, String targetRelation) throws RepositoryException {
        try {
            final String targetPID = targetRelation.startsWith(PIDParser.INFO_FEDORA_PREFIX) ? targetRelation : PIDParser.INFO_FEDORA_PREFIX + targetRelation;
            RepositoryDatastream stream = this.getStream(FedoraUtils.RELS_EXT_STREAM);
            Document document = XMLUtils.parseDocument(stream.getContent(), true);
            Element relationElement = XMLUtils.findElement(document.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                String elmLocalname = element.getLocalName();
                String elmResourceAttribute = element.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                return (elmNamespace.equals(namespace)) && (elmLocalname.equals(relation)) && elmResourceAttribute.equals(targetPID);
            });

            // Change RELS-EXT relations
            if (relationElement != null) {
                relationElement.getParentNode().removeChild(relationElement);
                changeRelations(document);
            } else {
                LOGGER.warning("Cannot find relation '" + namespace + relation);
            }


        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (TransformerException e) {
            throw new RepositoryException(e);
        }
    }


    public List<Triple<String, String, String>> getRelations(String namespace) throws RepositoryException {
        try {
            Document metadata = XMLUtils.parseDocument(getStream(FedoraUtils.RELS_EXT_STREAM).getContent(), true);
            List<Triple<String, String, String>> retvals = XMLUtils.getElementsRecursive(metadata.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                if (namespace != null) {
                    return namespace.equals(elmNamespace) && element.hasAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                } else {
                    return element.hasAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                }
            }).stream().map((elm) -> {
                String resource = elm.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                if (resource.startsWith(PIDParser.INFO_FEDORA_PREFIX)) {
                    resource = resource.substring(PIDParser.INFO_FEDORA_PREFIX.length());
                }

                Triple<String, String, String> triple = new ImmutableTriple<>(elm.getNamespaceURI(), elm.getLocalName(), resource);
                return triple;
            }).collect(Collectors.toList());
            Collections.reverse(retvals);
            return retvals;
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }


    public List<Triple<String, String, String>> getLiterals(String namespace) throws RepositoryException {
        try {
            Document metadata = XMLUtils.parseDocument(getStream(FedoraUtils.RELS_EXT_STREAM).getContent(), true);

            List<Triple<String, String, String>> retvals = XMLUtils.getElementsRecursive(metadata.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                if (namespace != null) {
                    return namespace.equals(elmNamespace) && !element.hasAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource") && StringUtils.isAnyString(element.getTextContent());
                } else {
                    return !element.hasAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource") && StringUtils.isAnyString(element.getTextContent());
                }
            }).stream().filter((elm) -> {
                return !elm.getLocalName().equals(RDF_ELEMENT) && !elm.getLocalName().equals(RDF_DESCRIPTION_ELEMENT);
            }).map((elm) -> {
                String content = elm.getTextContent();
                Triple<String, String, String> triple = new ImmutableTriple<>(elm.getNamespaceURI(), elm.getLocalName(), content);
                return triple;
            }).collect(Collectors.toList());

            Collections.reverse(retvals);
            return retvals;
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    private Element findRelationElement(String relation, String namespace, String targetRelation) throws RepositoryException {
        final String targetPID = targetRelation.startsWith(PIDParser.INFO_FEDORA_PREFIX) ? targetRelation : PIDParser.INFO_FEDORA_PREFIX + targetRelation;
        RepositoryDatastream stream = this.getStream(FedoraUtils.RELS_EXT_STREAM);
        if (stream == null) {
            throw new RepositoryException("FOXML object " + this.pid + "does not have RELS-EXT stream ");
        }
        Document document = null;
        try {
            document = XMLUtils.parseDocument(stream.getContent(), true);
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
        Element relationElement = XMLUtils.findElement(document.getDocumentElement(), (element) -> {
            String elmNamespace = element.getNamespaceURI();
            String elmLocalname = element.getLocalName();
            String elmResourceAttribute = element.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
            return (elmNamespace.equals(namespace)) && (elmLocalname.equals(relation)) && elmResourceAttribute.equals(targetPID);
        });
        return relationElement;
    }


    @Override
    public boolean relationExists(String relation, String namespace, String targetRelation) throws RepositoryException {
        Element foundElement = findRelationElement(relation, namespace, targetRelation);
        return foundElement != null;
    }

    @Override
    public boolean literalExists(String relation, String namespace, String value) throws RepositoryException {
        try {
            Document metadata = XMLUtils.parseDocument(getStream(FedoraUtils.RELS_EXT_STREAM).getContent(), true);
            Element foundElement = XMLUtils.findElement(metadata.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                String elmName = element.getLocalName();
                if (elmName.equals(relation) && namespace.equals(elmNamespace)) {
                    String cont = element.getTextContent();
                    return cont.endsWith(value);
                }
                return false;
            });
            return foundElement != null;
        } catch (RepositoryException e) {
            throw new RepositoryException(e);
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void removeLiteral(String relation, String namespace, String value) throws RepositoryException {

        // Element subElm = document.createElementNS(namespace, relation);
        try {
            RepositoryDatastream stream = this.getStream(FedoraUtils.RELS_EXT_STREAM);
            Document document = XMLUtils.parseDocument(stream.getContent(), true);

            Element rdfDesc = XMLUtils.findElement(document.getDocumentElement(), RDF_DESCRIPTION_ELEMENT, FedoraNamespaces.RDF_NAMESPACE_URI);

            List<Element> descs = XMLUtils.getElementsRecursive(rdfDesc, (element) -> {
                String elmNamespace = element.getNamespaceURI();
                String elmName = element.getLocalName();
                if (elmNamespace != null && elmNamespace.equals(namespace) && elmName.equals(relation)) {
                    String content = element.getTextContent();
                    if (content.equals(value)) return true;
                }
                return false;

            });

            if (!descs.isEmpty()) {
                descs.stream().forEach(literal -> {
                    literal.getParentNode().removeChild(literal);
                });
                changeRelations(document);
            }
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (TransformerException e) {
            throw new RepositoryException(e);
        }

    }

    @Override
    public void removeRelationsByNamespace(String namespace) throws RepositoryException {
        try {
            RepositoryDatastream stream = this.getStream(FedoraUtils.RELS_EXT_STREAM);
            Document document = XMLUtils.parseDocument(stream.getContent(), true);
            List<Element> relationElements = XMLUtils.getElementsRecursive(document.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                return (elmNamespace.equals(namespace));
            });


            // Change RELS-EXT relations
            if (!relationElements.isEmpty()) {
                relationElements.stream().forEach((elm) -> {
                    elm.getParentNode().removeChild(elm);
                });
                changeRelations(document);
            } else {
                LOGGER.warning("Cannot find relation '" + namespace);
            }

        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (TransformerException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void removeRelationsByNameAndNamespace(String relation, String namespace) throws RepositoryException {
        try {
            RepositoryDatastream stream = this.getStream(FedoraUtils.RELS_EXT_STREAM);
            Document document = XMLUtils.parseDocument(stream.getContent(), true);
            List<Element> relationElements = XMLUtils.getElementsRecursive(document.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                String elmLocalname = element.getLocalName();
                return (elmNamespace.equals(namespace)) && (elmLocalname.equals(relation));
            });

            // Change RELS-EXT relations
            if (!relationElements.isEmpty()) {
                relationElements.stream().forEach((elm) -> {
                    elm.getParentNode().removeChild(elm);
                });
                changeRelations(document);
            } else {
                LOGGER.info("Cannot find relation '" + namespace + relation);
            }

        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (TransformerException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public boolean relationsExists(String relation, String namespace) throws RepositoryException {
        try {
            Document metadata = XMLUtils.parseDocument(this.getStream(FedoraUtils.RELS_EXT_STREAM).getContent(), true);
            Element foundElement = XMLUtils.findElement(metadata.getDocumentElement(), (element) -> {
                String elmNamespace = element.getNamespaceURI();
                String elmName = element.getLocalName();
                return (elmName.equals(relation) && namespace.equals(elmNamespace));
            });
            return foundElement != null;
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void removeRelationsAndRelsExt() throws RepositoryException {
        if (this.streamExists(FedoraUtils.RELS_EXT_STREAM)) {
            this.removeRelationsByNamespace(FedoraNamespaces.KRAMERIUS_URI);
            this.removeRelationsByNameAndNamespace("isMemberOfCollection", FedoraNamespaces.RDF_NAMESPACE_URI);
            this.deleteStream(FedoraUtils.RELS_EXT_STREAM);
        }
    }

    @Override
    public String getFullPath() throws RepositoryException {
        throw new UnsupportedOperationException("getFullPath is not supported in Akubra");
    }

    @Override
    public void rebuildProcessingIndex() throws RepositoryException {
        RepositoryDatastream stream = this.getStream(FedoraUtils.RELS_EXT_STREAM);
        InputStream content = stream.getContent();
        rebuildProcessingIndexImpl(content);
    }

    private void rebuildProcessingIndexImpl(InputStream content) throws RepositoryException {
        try {
            String s = IOUtils.toString(content, "UTF-8");
            RELSEXTSPARQLBuilder sparqlBuilder = new RELSEXTSPARQLBuilderImpl();
            sparqlBuilder.sparqlProps(s.trim(), (object, localName) -> {
                processRELSEXTRelationAndFeedProcessingIndex(object, localName);
                return object;
            });
        } catch (IOException e) {
            throw new RepositoryException(e);
        } catch (SAXException e) {
            throw new RepositoryException(e);
        } catch (ParserConfigurationException e) {
            throw new RepositoryException(e);
        } finally {
            try {
                LOGGER.info("IS ABOUT CALL COMMIT");
                this.feeder.commit();
                LOGGER.info("CALLED PROCESSING INDEX COMMIT");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (SolrServerException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
