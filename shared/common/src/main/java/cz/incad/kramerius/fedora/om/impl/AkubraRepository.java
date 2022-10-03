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

import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import com.qbizm.kramerius.imp.jaxb.ObjectPropertiesType;
import com.qbizm.kramerius.imp.jaxb.PropertyType;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.om.RepositoryObject;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import org.apache.solr.client.solrj.SolrServerException;
import org.fcrepo.client.FcrepoOperationFailedException;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * @author pavels
 */
public class AkubraRepository extends Repository {


    public static final Logger LOGGER = Logger.getLogger(AkubraRepository.class.getName());

    private AkubraDOManager manager;
    private ProcessingIndexFeeder feeder;


    private AkubraRepository(ProcessingIndexFeeder feeder, AkubraDOManager manager) throws RepositoryException {
        super();
        this.feeder = feeder;
        this.manager = manager;
    }

    /**
     * Create new repository object
     *
     * @param feeder  Feeder instance
     * @param manager
     * @return
     * @throws RepositoryException
     */
    public static final Repository build(ProcessingIndexFeeder feeder, AkubraDOManager manager) throws RepositoryException {
        return new AkubraRepository(feeder, manager);
    }


    /* (non-Javadoc)
     * @see cz.incad.fcrepo.Repository#commitTransaction()
     */
    @Override
    public void commitTransaction() throws RepositoryException {
        try {
            //to avoid temporary inconsistency between Akubra and Processing index
            this.feeder.commit();
        } catch (IOException | SolrServerException e) {
            throw new RepositoryException(e);
        }
    }

    /* (non-Javadoc)
     * @see cz.incad.fcrepo.Repository#rollbackTransaction()
     */
    @Override
    public void rollbackTransaction() throws RepositoryException {
        throw new RepositoryException("Transactions not supported in Akubra");
    }


    /* (non-Javadoc)
     * @see cz.incad.fcrepo.Repository#createOrFindObject(java.lang.String)
     */
    @Override
    public RepositoryObject createOrFindObject(String ident) throws RepositoryException {
        if (objectExists(ident)) {
            try {
                AkubraObject obj = new AkubraObject(this.manager, ident, this.manager.readObjectFromStorage(ident), this.feeder);
                return obj;
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        } else {
            try {
                AkubraObject obj = new AkubraObject(this.manager, ident, createEmptyDigitalObject(ident), this.feeder);
                manager.commit(obj.digitalObject, null);
                obj.deleteProcessingIndex();
                return obj;
            } catch (IOException e) {
                throw new RepositoryException(e);
            } catch (SolrServerException e) {
                throw new RepositoryException(e);
            }
        }
    }

    private DigitalObject createEmptyDigitalObject(String pid) {
        DigitalObject retval = new DigitalObject();
        retval.setPID(pid);
        retval.setVERSION("1.1");
        ObjectPropertiesType objectPropertiesType = new ObjectPropertiesType();
        List<PropertyType> propertyTypeList = objectPropertiesType.getProperty();
        propertyTypeList.add(AkubraUtils.createProperty("info:fedora/fedora-system:def/model#state", "Active"));
        propertyTypeList.add(AkubraUtils.createProperty("info:fedora/fedora-system:def/model#ownerId", "fedoraAdmin"));
        String currentTime = AkubraUtils.currentTimeString();
        propertyTypeList.add(AkubraUtils.createProperty("info:fedora/fedora-system:def/model#createdDate", currentTime));
        propertyTypeList.add(AkubraUtils.createProperty("info:fedora/fedora-system:def/view#lastModifiedDate", currentTime));
        retval.setObjectProperties(objectPropertiesType);
        return retval;
    }

    @Override
    public RepositoryObject ingestObject(DigitalObject contents) throws RepositoryException {
        if (objectExists(contents.getPID())) {
            throw new RepositoryException("Ingested object exists:" + contents.getPID());
        } else {
            try {
                AkubraObject obj = new AkubraObject(this.manager, contents.getPID(), contents, this.feeder);
                manager.commit(obj.digitalObject, null);
                // rebuild processing index
                obj.rebuildProcessingIndex();
                return obj;
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        }
    }


    @Override
	public RepositoryObject ingestObject(DigitalObject contents, String source) throws RepositoryException {
        if (objectExists(contents.getPID())) {
            throw new RepositoryException("Ingested object exists:" + contents.getPID());
        } else {
            try {
                AkubraObject obj = new AkubraObject(this.manager, contents.getPID(), contents, this.feeder);
                manager.commit(obj.digitalObject, null);
                obj.rebuildProcessingIndex(source);
                return obj;
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        }
	}

	@Override
    public void iterateObjects(Consumer<String> consumer) throws RepositoryException, FcrepoOperationFailedException, IOException {
        /*
        Stack<String> stack = new Stack<>();
        StringBuilder builder = new StringBuilder(endpoint()).append("/").append(Fedora4Utils.DATA_PREFIX_PATH);
        stack.push(builder.toString());
        while(!stack.isEmpty()) {
            String url = stack.pop();
            try (FcrepoResponse response = new GetBuilder(URI.create(url), client).accept("application/rdf+xml").perform()) {
                if (response.getStatusCode() == 200) {
                    InputStream body = response.getBody();
                    Document document = XMLUtils.parseDocument(body, true);
                    Element hasModel = XMLUtils.findElement(document.getDocumentElement(), "hasModel", FedoraNamespaces.FEDORA_MODELS_URI);
                    if (hasModel != null ) {
                        Element pidElm = XMLUtils.findElement(document.getDocumentElement(), "PID", FedoraNamespaces.FEDORA_FOXML_URI);
                        consumer.accept(pidElm.getTextContent());
                    } else {
                        List<String> ldp = XMLUtils.getElementsRecursive(document.getDocumentElement(), (element) -> {
                            String localName = element.getLocalName();
                            String namespace = element.getNamespaceURI();
                            if (localName.equals("contains") && namespace.equals(FedoraNamespaces.LDP_NAMESPACE_URI)) {
                                return true;
                            } else return false;
                        }).stream().map((element) -> {
                            return element.getAttributeNS(FedoraNamespaces.RDF_NAMESPACE_URI, "resource");
                        }).collect(Collectors.toList());
                        ldp.stream().forEach(u-> stack.push(u));
                    }
                }
            } catch (SAXException e) {
                throw new RepositoryException(e.getMessage());
            } catch (ParserConfigurationException e) {
                throw new RepositoryException(e.getMessage());
            }
        } */ //TODO iterovat cele repository a consumerovi predavat pid
    }

    @Override
    public void deleteObject(String pid, boolean deleteDataOfManagedDatastreams, boolean deleteRelationsWithThisAsTarget) throws RepositoryException {
        try {
            this.manager.deleteObject(pid, deleteDataOfManagedDatastreams);
            try {
                // delete relations with this object as a source
                this.feeder.deleteByRelationsForPid(pid);
                // possibly delete relations with this object as a target
                if (deleteRelationsWithThisAsTarget) {
                    this.feeder.deleteByTargetPid(pid);
                }
                // delete this object's description
                this.feeder.deleteDescriptionByPid(pid);
            } catch (SolrServerException e) {
                throw new RepositoryException("Cannot delete data from processing index for  " + pid + " please start processing index update");
            }
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    @Override
    public void deleteObject(String pid) throws RepositoryException {
        deleteObject(pid, true, true);
    }

    @Override
    public ProcessingIndexFeeder getProcessingIndexFeeder() throws RepositoryException {
        return this.feeder;
    }

    @Override
    public boolean objectExists(String ident) throws RepositoryException {
        try {
            return manager.readObjectFromStorage(ident) != null;
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }

    /*

    public static final String DELETE_LITERAL( String relation,String namespace, String value) throws IOException {
        StringTemplate deleteRelation = RELSEXTSPARQLBuilderImpl.SPARQL_TEMPLATES().getInstanceOf("deleteliteral_sparql");
        deleteRelation.setAttribute("namespace", namespace);
        deleteRelation.setAttribute("relation",relation);
        deleteRelation.setAttribute("value",value);
        return deleteRelation.toString();
    }

    public static final String DELETE_RELATION( String relation,String namespace, String target) throws IOException {
        StringTemplate deleteRelation = RELSEXTSPARQLBuilderImpl.SPARQL_TEMPLATES().getInstanceOf("deleterelation_sparql");
        deleteRelation.setAttribute("namespace", namespace);
        deleteRelation.setAttribute("relation",relation);
        deleteRelation.setAttribute("target",target);
        return deleteRelation.toString();
    }

    public static final String DELETE_RELATIONS(Collection<Triple<String,String,String>> triples) throws IOException {
        StringTemplate deleteRelation = RELSEXTSPARQLBuilderImpl.SPARQL_TEMPLATES().getInstanceOf("delete_general");
        deleteRelation.setAttribute("triples", triples);
        return deleteRelation.toString();
    }

    public static final String UPDATE_PID(String pid ) throws IOException {
        StringTemplate updatePid = RELSEXTSPARQLBuilderImpl.SPARQL_TEMPLATES().getInstanceOf("updatepid_sparql");
        updatePid.setAttribute("pid",pid);
        return updatePid.toString();
    }

    public static final String UPDATE_INDEXING_SPARQL() throws IOException {
        StringTemplate indexPid = RELSEXTSPARQLBuilderImpl.SPARQL_TEMPLATES().getInstanceOf("indexable_sparql");
        return indexPid.toString();
    }
    */

    @Override
    public String getBoundContext() throws RepositoryException {
        throw new RepositoryException("BOUND CONTEXT not supported in Akubra");
    }

    @Override
    public RepositoryObject getObject(String ident) throws RepositoryException {
        try {
            DigitalObject digitalObject = this.manager.readObjectFromStorage(ident);
            if (digitalObject == null) {
                //otherwise later causes NPE at places like AkubraUtils.streamExists(DigitalObject object, String streamID)
                throw new RepositoryException("object not consistently found in storage: " + ident);
            }
            AkubraObject obj = new AkubraObject(this.manager, ident, digitalObject, this.feeder);
            return obj;
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }
}
