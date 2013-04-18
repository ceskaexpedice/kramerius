/*
 * Copyright (C) 2010 Jan Pokorsky
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

package cz.incad.kramerius.relation.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.*;
import cz.incad.kramerius.relation.Relation;
import cz.incad.kramerius.relation.RelationModel;
import cz.incad.kramerius.relation.RelationService;
import cz.incad.kramerius.relation.RelationUtils;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.utils.pid.PIDParser;
import org.w3c.dom.*;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Other than Kramerius relations remain untouched by these operations.
 * 
 * @author Jan Pokorsky
 */
public final class RelationServiceImpl implements RelationService {

    private static final Logger LOGGER = Logger.getLogger(RelationServiceImpl.class.getName());
    private static final FedoraNamespaceContext FEDORA_NAMESPACE_CONTEXT = new FedoraNamespaceContext();
    private static final String FEDORA_URI_PREFIX = "info:fedora/";

    @Inject
    @Named("rawFedoraAccess")
    FedoraAccess fedoraAccess;

    @Override
    public RelationModel load(String pid) throws IOException {
        try {
            Document relsExt = RelationUtils.getRelsExt(pid, fedoraAccess);
            return Loader.load(pid, relsExt);
        } catch (Exception ex) {
            throw new IOException("Cannot load relations: " + pid, ex);
        }
    }

    @Override
    public void save(String pid, RelationModel model) throws IOException {
        try {
            Document relsExt = RelationUtils.getRelsExt(pid, fedoraAccess);
            RelationModel orig = Loader.load(pid, relsExt);

            if (isModified(orig, model)) {
                // XXX use also timestamp or checksum to detect concurrent modifications
                String dsContent = Saver.save(relsExt, model);
                fedoraAccess.getAPIM().modifyDatastreamByValue(pid, "RELS-EXT", null, null, null, null, dsContent.getBytes("UTF-8"), null, null, null, false);

                List<String> movedPids = new ArrayList<String>();
                for (KrameriusModels kind : model.getRelationKinds()) {
                    if (KrameriusModels.DONATOR.equals(kind)) continue;
                    List<Relation> newRelations = model.getRelations(kind);
                    List<Relation> origRelations = orig.getRelations(kind);
                    if (newRelations.size()==origRelations.size()){
                        for (int i = 0; i< newRelations.size();i++){
                            if (!newRelations.get(i).equals(origRelations.get(i))) {
                                movedPids.add(newRelations.get(i).getPID());
                            }
                        }
                    }
                }
                for (String movedPid:movedPids){
                    fedoraAccess.getAPIM().modifyObject(movedPid,null,null,null,"Relation order changed.");
                }

                RelationModelImpl modelImpl = (RelationModelImpl) model;
                modelImpl.onSave();
            }
        } catch (Exception ex) {
            throw new IOException("Cannot store relations: " + pid, ex);
        }
    }

    private boolean isModified(RelationModel origModel, RelationModel newModel) {
        Set<KrameriusModels> origKinds = origModel.getRelationKinds();
        Set<KrameriusModels> newKinds = newModel.getRelationKinds();
        if (origKinds.size() != newKinds.size()) {
            return true;
        }
        for (KrameriusModels kind : newKinds) {
            List<Relation> newRelations = newModel.getRelations(kind);
            List<Relation> origRelations = origModel.getRelations(kind);
            if (origRelations == null || !origRelations.equals(newRelations)) {
                return true;
            }
        }
        return false;
    }


    private static final class Saver {

        private final Document relsExt;
        private final RelationModel model;
        private final String rdfResourceAttrName;
        private final String krameriusPrefix = FEDORA_NAMESPACE_CONTEXT.getPrefix(FedoraNamespaces.KRAMERIUS_URI);

        private Saver(Document relsExt, RelationModel model) {
            this.relsExt = relsExt;
            this.model = model;
            rdfResourceAttrName = String.format("%s:resource",
                    FEDORA_NAMESPACE_CONTEXT.getPrefix(FedoraNamespaces.RDF_NAMESPACE_URI)
                    );
        }

        public static String save(Document relsExt, RelationModel model) throws XPathExpressionException, IOException {
            Saver saver = new Saver(relsExt, model);
            return saver.doSave();
        }

        // XXX move this to XMLUtils
        private static DOMImplementationLS initDOMLS()
                throws ClassCastException, ClassNotFoundException,
                IllegalAccessException, InstantiationException {

            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS domLS = (DOMImplementationLS) registry.getDOMImplementation("LS");
            return domLS;
        }

        private static DOMImplementationLS initDOMLS(Document doc)
                throws ClassCastException, ClassNotFoundException,
                IllegalAccessException, InstantiationException {
            DOMImplementation domImpl = doc.getImplementation();
            DOMImplementationLS domLS = null;
            if (domImpl.hasFeature("Core", "3.0") && domImpl.hasFeature("LS", "3.0")) {
                domLS = (DOMImplementationLS) domImpl.getFeature("LS", "3.0");
            }
            if (domLS == null) {
                domLS = initDOMLS();
            }
            return domLS;
        }
        
        private static LSSerializer initDOMSerializer(Document doc) throws IOException {
            try {
                DOMImplementationLS domLS = initDOMLS(doc);
                LSSerializer serializer = domLS.createLSSerializer();
                DOMConfiguration domConfig = serializer.getDomConfig();
                domConfig.setParameter("xml-declaration", false);
                if (domConfig.canSetParameter("format-pretty-print", true)) {
                    domConfig.setParameter("format-pretty-print", true);
                }
                return serializer;
            } catch (Exception ex) {
                Logger.getLogger(RelationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw new IOException("Cannot initialize LSSerializer", ex);
            }
        }

        private String doSave() throws XPathExpressionException, IOException {
            LSSerializer serializer = initDOMSerializer(relsExt);
            if (LOGGER.isLoggable(Level.FINE)) {
                String input = serializer.writeToString(relsExt);
                LOGGER.fine(input);
            }
            for (KrameriusModels kind : model.getRelationKinds()) {
                applyRelations(kind, model.getRelations(kind));
            }
            String output = serializer.writeToString(relsExt);
            LOGGER.log(Level.FINE, "modified stream:\n{0}", output);
            return output;
        }

        private void applyRelations(KrameriusModels kind, List<Relation> rels) throws XPathExpressionException {
            String relationElmName = String.format("%s:%s", krameriusPrefix, RDFModels.convertToRdf(kind));
            NodeList relationNodes = findRelationNodes(relationElmName);
            Node afterNode = getAfterNode(relationNodes);
            appendNodes(afterNode, relationElmName, rels);
            deleteNodes(relationNodes);
        }

        /** gets node or null */
        private Node getAfterNode(NodeList nl) throws XPathExpressionException {
            if (nl.getLength() > 0) {
                return nl.item(nl.getLength() - 1);
            } else {
                return findRelationNodes("oai:itemID").item(0).getNextSibling();
            }
        }

        private NodeList findRelationNodes(String relationElmName) throws XPathExpressionException {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(FEDORA_NAMESPACE_CONTEXT);
            String xPathStr = String.format("/rdf:RDF/rdf:Description/%s", relationElmName);
            NodeList nodeList = (NodeList) xpath.evaluate(xPathStr, relsExt, XPathConstants.NODESET);
            return nodeList;
        }

        private void appendNodes(Node afterNode, String relationElmName, List<Relation> relations) {
            for (Relation relation : relations) {
                Element elm = relsExt.createElementNS(FedoraNamespaces.KRAMERIUS_URI, relationElmName);
                String relationURI = FEDORA_URI_PREFIX + relation.getPID();
                elm.setAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, rdfResourceAttrName, relationURI);
                appendNode(afterNode, elm);
            }
        }

        private void appendNode(Node afterNode, Node newNode) {
            Node parentNode = afterNode.getParentNode();
            if (afterNode != null) {
                parentNode.insertBefore(newNode, afterNode);
            } else {
                parentNode.appendChild(newNode);
            }
        }

        private void deleteNodes(NodeList relationNodes) {
            for(int i = relationNodes.getLength() - 1; i >= 0; i--) {
                Node node = relationNodes.item(i);
                node.getParentNode().removeChild(node);
            }
        }
    }

    private static final class Loader {
        private Map<KrameriusModels, List<Relation>> krameriusRelations;
        private KrameriusModels krameriusModel;

        public static RelationModel load(String pid, Document doc) throws XPathExpressionException, LexerException {
            Loader loader = new Loader();
            loader.parse(doc);
            return new RelationModelImpl(pid, loader.krameriusRelations, loader.krameriusModel);
        }

        private void parse(Document doc) throws XPathExpressionException, LexerException {
            krameriusRelations = new EnumMap<KrameriusModels, List<Relation>>(KrameriusModels.class);
            krameriusModel = null;

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(FEDORA_NAMESPACE_CONTEXT);
            String xPathStr = "/rdf:RDF/rdf:Description/*";
            NodeList nodeList = (NodeList) xpath.evaluate(xPathStr, doc, XPathConstants.NODESET);
            for(int i = 0, length = nodeList.getLength(); i < length; i++) {
                Node node = nodeList.item(i);
                if (Node.ELEMENT_NODE == node.getNodeType()) {
                    Element elm = (Element) node;
                    String nodeName = elm.getNodeName();
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Node: name:{0}, local:{1}, prefix:{2}, URI:{3}",
                                new Object[]{nodeName, elm.getLocalName(), elm.getPrefix(), elm.getNamespaceURI()});
                    }
                    processElement(elm);
                }
            }
        }

        private void processElement(Element elm) throws LexerException {
            String prefix = elm.getNamespaceURI();
            if (FedoraNamespaces.FEDORA_MODELS_URI.equals(prefix)) {
                processFedoraModelElement(elm);
            } else if (FedoraNamespaces.KRAMERIUS_URI.equals(prefix)) {
                processKrameriusElement(elm);
            }
        }

        private void processKrameriusElement(Element elm) throws LexerException {
            String localName = elm.getLocalName();
            KrameriusModels elmModel = RDFModels.convertRDFToModel(localName);
            if (elmModel == null /*|| elmModel == KrameriusModels.DONATOR*/) {
                // unsupported relation; ignore
                return;
            }
            List<Relation> rels = krameriusRelations.get(elmModel);
            if (rels == null) {
                rels = new ArrayList<Relation>();
                krameriusRelations.put(elmModel, rels);
            }
            String resourcePID = resolveRDFResourceAsPID(elm);
            // XXX it could be optimized with lazy parsing of resource id
            rels.add(new Relation(resourcePID, elmModel));
        }

        private void processFedoraModelElement(Element elm) throws LexerException {
            String localName = elm.getLocalName();
            if ("hasModel".equals(localName)) {
                String resourceUUID = resolveRDFResourceAsUUID(elm);
                krameriusModel = KrameriusModels.parseString(resourceUUID);
            }
        }

        private static String resolveRDFResourceAsUUID(Element elm) throws LexerException {
            PIDParser parser = parseRDFResource(elm);
            return parser.getObjectId();
        }

        private static String resolveRDFResourceAsPID(Element elm) throws LexerException {
            PIDParser parser = parseRDFResource(elm);
            return String.format("%s:%s", parser.getNamespaceId(), parser.getObjectId());
        }

        private static PIDParser parseRDFResource(Element elm) throws LexerException {
            String resourceAttr = elm.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
            PIDParser pidParser = new PIDParser(resourceAttr);
            pidParser.disseminationURI();
            return pidParser;
        }
    }
}
