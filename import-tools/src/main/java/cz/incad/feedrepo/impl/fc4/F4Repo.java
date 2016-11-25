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

package cz.incad.feedrepo.impl.fc4;

import java.util.concurrent.atomic.AtomicInteger;

import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;
import org.fcrepo.client.impl.FedoraRepositoryImpl;

import cz.incad.feedrepo.RepoAbstraction;
import cz.incad.feedrepo.RepoAbstractionException;
import cz.incad.feedrepo.RepositoryObjectAbstraction;
import cz.incad.kramerius.repo.impl.FedoraRepoUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * @author pavels
 *
 */
public class F4Repo implements RepoAbstraction {

    static String FEDORA_CONTEXT = "";

    private FedoraRepository repo;

    /**
     * @param repo
     */
    public F4Repo() {
        super();
        repo = new FedoraRepositoryImpl(FedoraRepoUtils.getFedora4Host() + "/rest/");

    }

    /* (non-Javadoc)
     * @see cz.incad.fcrepo.RepoAbstraction#startAbstraction()
     */
    @Override
    public void startTransaction() throws RepoAbstractionException {
        try {
            this.repo.startTransaction();
        } catch (FedoraException e) {
            throw new RepoAbstractionException(e);
        }
    }

    /* (non-Javadoc)
     * @see cz.incad.fcrepo.RepoAbstraction#commitTransaction()
     */
    @Override
    public void commitTransaction() throws RepoAbstractionException {
        try {
            this.repo.commitTransaction();
        } catch (FedoraException e) {
            throw new RepoAbstractionException(e);
        }
        
    }

    /* (non-Javadoc)
     * @see cz.incad.fcrepo.RepoAbstraction#rollbackTransaction()
     */
    @Override
    public void rollbackTransaction() throws RepoAbstractionException {
        try {
            this.repo.rollbackTransaction();
        } catch (FedoraException e) {
            throw new RepoAbstractionException(e);
        }
    }

    /* (non-Javadoc)
     * @see cz.incad.fcrepo.RepoAbstraction#createObject(java.lang.String)
     */
    @Override
    public RepositoryObjectAbstraction createObject(String ident) throws RepoAbstractionException {
        try {
            boolean exists = this.repo.exists(ident);
            FedoraObject createObject = this.repo.findOrCreateObject(ident);
            return new F4RepoObject(createObject, this.repo);
        } catch (FedoraException e) {
            throw new RepoAbstractionException(e);
        }
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
    

    /* (non-Javadoc)
     * @see cz.incad.fcrepo.RepoAbstraction#open()
     */
    @Override
    public void open() throws RepoAbstractionException {
    }

    /* (non-Javadoc)
     * @see cz.incad.fcrepo.RepoAbstraction#close()
     */
    @Override
    public void close() throws RepoAbstractionException {
    }

}
