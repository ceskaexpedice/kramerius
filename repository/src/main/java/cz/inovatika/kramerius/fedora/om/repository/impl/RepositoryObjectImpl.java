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
package cz.inovatika.kramerius.fedora.om.repository.impl;

import com.qbizm.kramerius.imp.jaxb.*;
import cz.inovatika.kramerius.fedora.om.processingindex.ProcessingIndexFeeder;
import cz.inovatika.kramerius.fedora.FedoraNamespaces;
import cz.inovatika.kramerius.fedora.om.repository.RepositoryDatastream;
import cz.inovatika.kramerius.fedora.om.repository.RepositoryException;
import cz.inovatika.kramerius.fedora.om.repository.RepositoryObject;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.inovatika.kramerius.fedora.utils.FedoraUtils;
import cz.inovatika.kramerius.fedora.utils.pid.PIDParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * @author pavels
 */
class RepositoryObjectImpl implements RepositoryObject {
    private static final Logger LOGGER = Logger.getLogger(RepositoryObjectImpl.class.getName());
    private static final String RDF_DESCRIPTION_ELEMENT = "Description";
    private static final String RDF_ELEMENT = "RDF";
    private AkubraDOManager manager;
    private DigitalObject digitalObject;
    private ProcessingIndexFeeder feeder;

    RepositoryObjectImpl(DigitalObject digitalObject, AkubraDOManager manager, ProcessingIndexFeeder feeder) {
        super();
        this.manager = manager;
        this.feeder = feeder;
        this.digitalObject = digitalObject;
    }

    @Override
    public List<RepositoryDatastream> getStreams() throws RepositoryException {
        List<RepositoryDatastream> list = new ArrayList<>();
        List<DatastreamType> datastreamList = digitalObject.getDatastream();
        for (DatastreamType datastreamType : datastreamList) {
            list.add(new RepositoryDatastreamImpl(manager, datastreamType, datastreamType.getID(), controlGroup2Type(datastreamType.getCONTROLGROUP())));
        }
        return list;
    }

    @Override
    public String getPath() {
        return digitalObject.getPID();
    }

    private String getPid() {
        return digitalObject.getPID();
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
        datastreamVersionType.setCREATED(RepositoryUtils.getCurrentXMLGregorianCalendar());
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

        RepositoryDatastream ds = new RepositoryDatastreamImpl(manager, datastreamType, streamId, RepositoryDatastream.Type.INDIRECT);

        try {
            manager.commit(digitalObject, streamId);
            return ds;
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    private RepositoryDatastreamImpl.Type controlGroup2Type(String controlGroup) {
        if ("E".equals(controlGroup) || "R".equals(controlGroup)) {
            return RepositoryDatastream.Type.INDIRECT;
        } else {
            return RepositoryDatastream.Type.DIRECT;
        }
    }

    @Override
    public void deleteStream(String streamId) throws RepositoryException {
        try {
            manager.deleteStream(getPid(), streamId);
            if (streamId.equals(FedoraUtils.RELS_EXT_STREAM)) {
                try {
                    this.feeder.deleteByRelationsForPid(this.getPid());
                } catch (Throwable th) {
                    LOGGER.log(Level.SEVERE, "Cannot update processing index for "+ getPid() + " - reindex manually.", th);
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

        RepositoryDatastream ds = new RepositoryDatastreamImpl(manager, datastreamType, streamId, RepositoryDatastream.Type.DIRECT);

        try {
            manager.commit(digitalObject, streamId);
            if (streamId.equals(FedoraUtils.RELS_EXT_STREAM)) {
                try {
                    // process rels-ext and create all children and relations
                    this.feeder.deleteByRelationsForPid(getPid());
                    input.reset();
                    feeder.rebuildProcessingIndex(this, input);
                } catch (Throwable th) {
                    LOGGER.log(Level.SEVERE, "Cannot update processing index for "+ getPid() + " - reindex manually.", th);
                }
            }
            return ds;
        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
    }

    private static Element elementFromInputStream(InputStream in) {
        DocumentBuilderFactory factory;
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

    @Override
    public RepositoryDatastream createManagedStream(String streamId, String mimeType, InputStream input) throws RepositoryException {
        DatastreamType datastreamType = createDatastreamHeader(streamId, mimeType, "M");

        try {
            datastreamType.getDatastreamVersion().get(0).setBinaryContent(IOUtils.toByteArray(input));
            RepositoryDatastream ds = new RepositoryDatastreamImpl(manager, datastreamType, streamId, RepositoryDatastream.Type.DIRECT);
            manager.commit(digitalObject, streamId);
            return ds;
        } catch (Exception ex) {
            throw new RepositoryException(ex);
        }
    }

    @Override
    public boolean streamExists(String streamId) throws RepositoryException {
        return RepositoryUtils.streamExists(digitalObject, streamId);
    }

    @Override
    public RepositoryDatastream getStream(String streamId) throws RepositoryException {
        List<DatastreamType> datastreamList = digitalObject.getDatastream();
        for (DatastreamType datastreamType : datastreamList) {
            if (streamId.equals(datastreamType.getID())) {
                return new RepositoryDatastreamImpl(manager, datastreamType, datastreamType.getID(), controlGroup2Type(datastreamType.getCONTROLGROUP()));
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
            return RepositoryUtils.getLastModified(digitalObject);
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
            return manager.retrieveObject(getPid());
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
            throw new RepositoryException("FOXML object " + this.getPid() + "does not have RELS-EXT stream ");
        }
        Document document;
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
        feeder.rebuildProcessingIndex(this, content);
    }


    DigitalObject getDigitalObject() {
        return digitalObject;
    }
}
