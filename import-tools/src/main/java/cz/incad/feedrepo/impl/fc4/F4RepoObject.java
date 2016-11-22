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

import java.io.InputStream;

import org.fcrepo.client.FedoraContent;
import org.fcrepo.client.FedoraDatastream;
import org.fcrepo.client.FedoraException;
import org.fcrepo.client.FedoraObject;
import org.fcrepo.client.FedoraRepository;

import cz.incad.feedrepo.RepoAbstractionException;
import cz.incad.feedrepo.RepositoryObjectAbstraction;

/**
 * @author pavels
 *
 */
public class F4RepoObject implements RepositoryObjectAbstraction {

    private FedoraObject fObject;
    private FedoraRepository repo;

    /**
     * @param fObject
     */
    public F4RepoObject(FedoraObject fObject, FedoraRepository repo) {
        super();
        this.fObject = fObject;
        this.repo = repo;
    }
    
    
    /* (non-Javadoc)
     * @see cz.incad.fcrepo.RepositoryObjectAbstraction#getWrappedObject()
     */
    @Override
    public Object getWrappedObject() {
        return this.fObject;
    }
    


    @Override
    public void setModel(String model) throws RepoAbstractionException {
    }


    @Override
    public String getModel() throws RepoAbstractionException {
        return null;
    }


    /* (non-Javadoc)
     * @see cz.incad.fcrepo.RepositoryObjectAbstraction#createStream(java.lang.String, java.lang.String, java.io.InputStream)
     */
    @Override
    public RepositoryObjectAbstraction createStream(String streamId, String mimeType, InputStream input) throws RepoAbstractionException {
        try {
            FedoraContent content = new FedoraContent();
            content.setContent(input);
            if (!streamId.equals("RELS-EXT")) {
                content.setContentType(mimeType);
            } else {
                content.setContentType("text/xml");
            }
            FedoraDatastream datastream = this.repo.createDatastream(this.fObject.getPath()+"/"+streamId, content);
            if (streamId.equals("RELS-EXT")) {
                datastream.updateProperties(F4Repo.UPDATE_INDEXING_SPARQL());
            }
            return this;
        } catch (FedoraException e) {
            throw new RepoAbstractionException(e);
        }
    }
}
