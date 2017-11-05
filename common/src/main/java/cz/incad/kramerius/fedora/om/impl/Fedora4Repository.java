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

import static cz.incad.kramerius.fedora.utils.Fedora4Utils.*;

import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.fedora.utils.Fedora4Utils;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.utils.StringUtils;
import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.fcrepo.client.*;

import cz.incad.kramerius.fedora.om.RepositoryObject;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pavels
 *
 */
public class Fedora4Repository extends Repository {

    public static final Logger LOGGER = Logger.getLogger(Fedora4Repository.class.getName());

    private FcrepoClient client;
    private ProcessingIndexFeeder feeder;
    private String transactionId;
    private boolean inTransactionFlag = false;


    public Fedora4Repository(ProcessingIndexFeeder feeder, boolean inTransactionFlag) throws RepositoryException {
        super();
        client = FcrepoClient.client().build();
        this.feeder = feeder;
        this.inTransactionFlag = inTransactionFlag;

        if (this.inTransactionFlag) {
            this.startTransaction();
        }
    }


    private void startTransaction() throws RepositoryException {
        String endpoint = endpoint();
        try (FcrepoResponse response = new PostBuilder(URI.create(endpoint+"/fcr:tx"),client).perform()) {
            if (response.getStatusCode() == 201) {
                String location = response.getHeaderValue("Location");
                String transactionId = StringUtils.minus(location, endpoint+"/");
                this.transactionId = transactionId;
                LOGGER.info("Started transaction :"+this.transactionId);
            }
        } catch (FcrepoOperationFailedException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }


    /* (non-Javadoc)
     * @see cz.incad.fcrepo.Repository#commitTransaction()
     */
    @Override
    public void commitTransaction() throws RepositoryException {
        if (this.inTransactionFlag) {
            String endpoint = endpoint();
            try (FcrepoResponse response = new PostBuilder(URI.create(endpoint+(endpoint.endsWith("/") ? "":"/")+this.transactionId+"/fcr:tx/fcr:commit"),client).perform()) {
                if (response.getStatusCode() != 204) {
                    throw new RepositoryException("Cannot commit transaction :"+this.transactionId);
                }
            } catch (FcrepoOperationFailedException e) {
                throw new RepositoryException(e);
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        } else {
            throw new RepositoryException("Repository is not in the transaction");
        }
    }

    /* (non-Javadoc)
     * @see cz.incad.fcrepo.Repository#rollbackTransaction()
     */
    @Override
    public void rollbackTransaction() throws RepositoryException {
        if (this.inTransactionFlag) {
            String endpoint = endpoint();
            try (FcrepoResponse response = new PostBuilder(URI.create(endpoint+(endpoint.endsWith("/") ? "":"/")+this.transactionId+"/fcr:tx/fcr:rollback"),client).perform()) {
                if (response.getStatusCode() != 204) {
                    throw new RepositoryException("Cannot rollback transaction :"+this.transactionId);
                }
            } catch (FcrepoOperationFailedException e) {
                throw new RepositoryException(e);
            } catch (IOException e) {
                throw new RepositoryException(e);
            }
        } else {
            throw new RepositoryException("Repository is not in the transaction");
        }
    }


    /* (non-Javadoc)
     * @see cz.incad.fcrepo.Repository#createOrFindObject(java.lang.String)
     */
    @Override
    public RepositoryObject createOrFindObject(String ident) throws RepositoryException {
        try {
            List<String> normalized = Fedora4Utils.normalizePath(ident);
            if (objectExists(ident)) {
                Fedora4Object obj = new Fedora4Object(this, this.client,normalized, ident, this.feeder);
                if (this.inTransactionFlag && this.transactionId != null) {
                    obj.setTransactionId(this.transactionId);
                }
                obj.updateSPARQL(UPDATE_PID(ident));
                return obj;
            } else {
                try {
                    URI resources = createResources(normalized);
                    Fedora4Object obj =  new Fedora4Object(this, this.client, normalized, ident, this.feeder);
                    if (this.inTransactionFlag && this.transactionId != null) {
                        obj.setTransactionId(this.transactionId);
                    }
                    obj.deleteProcessingIndex();
                    obj.updateSPARQL(UPDATE_PID(ident));
                    return obj;
                } catch (FcrepoOperationFailedException e) {
                    throw new RepositoryException(e);
                } catch (IOException e) {
                    throw new RepositoryException(e);
                } catch (SolrServerException e) {
                    throw new RepositoryException(e);
                }
            }
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }


    private URI createResources(List<String> parts) throws FcrepoOperationFailedException, RepositoryException, IOException {
            URI processingURI = null;
            StringBuilder builder = new StringBuilder(endpoint());
            if (this.inTransactionFlag && this.transactionId != null) {
                builder.append("/").append(this.transactionId);
            }
            for (String p:  parts) {
                if (!parts.toString().endsWith("/")) {
                    builder.append('/');
                }
                builder.append(p);
                processingURI = URI.create(builder.toString());
                if (!this.exists(processingURI)) {
                    try (FcrepoResponse response = new PutBuilder(processingURI, client).perform()) {
                        if (response.getStatusCode()!= 201) {
                            throw new RepositoryException("cannot create object :"+response.getStatusCode());
                        }
                    }
                }
            }
            return processingURI;
    }


    @Override
    public void deleteobject(String pid) throws RepositoryException {
        try (FcrepoResponse response = new DeleteBuilder(URI.create(this.objectPath(Fedora4Utils.normalizePath(pid))), client).perform()) {
            if (response.getStatusCode() == 204) {
                try (FcrepoResponse thombStoneResponse = new DeleteBuilder(URI.create(this.objectPath(Fedora4Utils.normalizePath(pid))+"/fcr:tombstone"), client).perform()) {
                    if (thombStoneResponse.getStatusCode() != 204) {
                        throw new RepositoryException("Cannot delete tombstone for object "+pid);
                    }
                }
            }  else {
                throw new RepositoryException("Cannot delete  object "+pid);
            }
        } catch (FcrepoOperationFailedException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }

    }

    private String objectPath(List<String> path) {
        if (this.transactionId != null) {
            return endpoint() + (endpoint().endsWith("/") ? "" : "/") + this.transactionId +  Fedora4Utils.path(path);
        } else {
            return endpoint()  + Fedora4Utils.path(path);
        }
    }


    @Override
    public boolean objectExists(String ident) throws RepositoryException {
        return exists(URI.create(objectPath(Fedora4Utils.normalizePath(ident))));
    }

    boolean exists(URI uri) throws RepositoryException {
        try (FcrepoResponse response = new HeadBuilder(uri,client).perform()) {
                return response.getStatusCode() == 200;
        } catch (FcrepoOperationFailedException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

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

    public static final String UPDATE_PID(String pid ) throws IOException {
        StringTemplate updatePid = RELSEXTSPARQLBuilderImpl.SPARQL_TEMPLATES().getInstanceOf("updatepid_sparql");
        updatePid.setAttribute("pid",pid);
        return updatePid.toString();
    }

    public static final String UPDATE_INDEXING_SPARQL() throws IOException {
        StringTemplate indexPid = RELSEXTSPARQLBuilderImpl.SPARQL_TEMPLATES().getInstanceOf("indexable_sparql");
        return indexPid.toString();
    }

    @Override
    public String getBoundContext() throws RepositoryException {
        return Fedora4Utils.BOUND_CONTEXT;
    }

    @Override
    public RepositoryObject getObject(String ident) throws RepositoryException {
        List<String> normalized = Fedora4Utils.normalizePath(ident);
        Fedora4Object obj = new Fedora4Object(this, this.client, normalized,ident,this.feeder);
        if (this.inTransactionFlag && this.transactionId != null) {
            obj.setTransactionId(this.transactionId);
        }
        return obj;
    }
}
