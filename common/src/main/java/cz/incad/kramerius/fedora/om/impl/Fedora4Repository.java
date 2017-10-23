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
import org.apache.solr.client.solrj.SolrServerException;
import org.fcrepo.client.*;

import cz.incad.kramerius.fedora.om.RepositoryObject;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author pavels
 *
 */
public class Fedora4Repository implements Repository {

    public static final Logger LOGGER = Logger.getLogger(Fedora4Repository.class.getName());

    private FcrepoClient client;
    private ProcessingIndexFeeder feeder;

    public Fedora4Repository(ProcessingIndexFeeder feeder) {
        super();
        client = FcrepoClient.client().build();
        this.feeder = feeder;
    }

    /* (non-Javadoc)
     * @see cz.incad.fcrepo.Repository#startAbstraction()
     */
    @Override
    public void startTransaction() throws RepositoryException {
        /*
        try {
            this.repo.startTransaction();
        } catch (FedoraException e) {
            throw new RepositoryException(e);
        }*/
    }

    /* (non-Javadoc)
     * @see cz.incad.fcrepo.Repository#commitTransaction()
     */
    @Override
    public void commitTransaction() throws RepositoryException {
        /*
        try {
            this.repo.commitTransaction();
        } catch (FedoraException e) {
            throw new RepositoryException(e);
        }*/
        
    }

    /* (non-Javadoc)
     * @see cz.incad.fcrepo.Repository#rollbackTransaction()
     */
    @Override
    public void rollbackTransaction() throws RepositoryException {
//        try {
//            this.repo.rollbackTransaction();
//        } catch (FedoraException e) {
//            throw new RepositoryException(e);
//        }
    }


    /* (non-Javadoc)
     * @see cz.incad.fcrepo.Repository#createOrFindObject(java.lang.String)
     */
    @Override
    public RepositoryObject createOrFindObject(String ident) throws RepositoryException {

            List<String> normalized = Fedora4Utils.normalizePath(ident);
            if (objectExists(ident)) {
                Fedora4Object obj = new Fedora4Object(this, this.client,normalized, ident, this.feeder);
                obj.updateSPARQL(UPDATE_PID(ident));
                return obj;
            } else {
                try {

                    URI resources = createResources(normalized);
                    Fedora4Object obj =  new Fedora4Object(this, this.client, normalized, ident, this.feeder);
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


    }


    private URI createResources(List<String> parts) throws FcrepoOperationFailedException, RepositoryException, IOException {
            URI processingURI = null;
            StringBuilder builder = new StringBuilder(endpoint());
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
    public boolean objectExists(String ident) throws RepositoryException {
        List<String> parts = Fedora4Utils.normalizePath(ident);
        return (exists(URI.create(endpoint() + Fedora4Utils.path(parts))));
    }

    boolean exists(URI uri) throws RepositoryException {
        try (FcrepoResponse response = new HeadBuilder(uri,client).perform()) {
                return response.getStatusCode() != 404;
        } catch (FcrepoOperationFailedException e) {
            throw new RepositoryException(e);
        } catch (IOException e) {
            throw new RepositoryException(e);
        }
    }

    public static final String UPDATE_PID(String pid ) {
        StringBuilder builder = new StringBuilder();
        builder.append(" PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n ");
        builder.append(" PREFIX indexing: <http://fedora.info/definitions/v4/indexing#> \n ");
        builder.append(" INSERT { \n ");
        builder.append(" <> <info:fedora/fedora-system:def/foxml#PID> \""+pid+"\" } ");
        builder.append(" WHERE { } ");
        return builder.toString();
    }

    public static final String UPDATE_INDEXING_SPARQL() {
        StringBuilder builder = new StringBuilder();
        builder.append(" PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n ");
        builder.append(" PREFIX indexing: <http://fedora.info/definitions/v4/indexing#> \n ");
        builder.append(" INSERT { \n ");
        builder.append(" <> indexing:hasIndexingTransformation \"default\"; ");
        builder.append(" rdf:type indexing:Indexable } ");
        builder.append(" WHERE { } ");
        return builder.toString();
    }

    @Override
    public String getBoundContext() throws RepositoryException {
        return Fedora4Utils.BOUND_CONTEXT;
    }

    @Override
    public RepositoryObject getObject(String ident) throws RepositoryException {
        List<String> normalized = Fedora4Utils.normalizePath(ident);
        return new Fedora4Object(this, this.client, normalized,ident,this.feeder);
    }
}
