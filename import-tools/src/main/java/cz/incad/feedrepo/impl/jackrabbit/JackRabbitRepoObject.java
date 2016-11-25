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

package cz.incad.feedrepo.impl.jackrabbit;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.rmi.value.SerialValueFactory;
import org.apache.jackrabbit.value.BinaryImpl;

import cz.incad.feedrepo.RepoAbstractionException;
import cz.incad.feedrepo.RepositoryObjectAbstraction;

/**
 * @author pavels
 *
 */
public class JackRabbitRepoObject implements RepositoryObjectAbstraction {

    
    private Node node;
    
    
    /**
     * @param node
     */
    public JackRabbitRepoObject(Node node) {
        super();
        this.node = node;
    }

    /*
     * (non-Javadoc)
     * 
     * @see cz.incad.fcrepo.RepositoryObjectAbstraction#getWrappedObject()
     */
    @Override
    public Object getWrappedObject() {
        return this.node;
    }

    public void setModel(String model) throws RepoAbstractionException {
        try {
            this.node.setProperty("fedora-models:model", model);
        } catch (ValueFormatException e) {
            throw new RepoAbstractionException(e);
        } catch (VersionException e) {
            throw new RepoAbstractionException(e);
        } catch (LockException e) {
            throw new RepoAbstractionException(e);
        } catch (ConstraintViolationException e) {
            throw new RepoAbstractionException(e);
        } catch (RepositoryException e) {
            throw new RepoAbstractionException(e);
        }
    }

    public String getModel() throws RepoAbstractionException {
        try {
            Property property = this.node.getProperty("fedora-models:model");
            if (property != null) {
                return property.getString();
            } else
                return null;
        } catch (PathNotFoundException e) {
            throw new RepoAbstractionException(e);
        } catch (RepositoryException e) {
            throw new RepoAbstractionException(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see cz.incad.fcrepo.RepositoryObjectAbstraction#createStream(java.lang.
     * String, java.lang.String, java.io.InputStream)
     */
    @Override
    public RepositoryObjectAbstraction createStream(String streamId, String mimeType, InputStream input)
            throws RepoAbstractionException {
        try {
            String ident = this.node.getIdentifier();
            if (this.node.hasNode(ident)) {
                this.node.getNode(ident).remove();
            }
 
            Node kramerius = this.node.addNode(streamId);
            
            kramerius.addMixin("mix:lockable");
            kramerius.addMixin("kramerius:datastream");
            kramerius.setProperty(JcrConstants.JCR_MIMETYPE, mimeType);
           
            Binary binVal = SerialValueFactory.getInstance().createBinary(input);
            kramerius.setProperty(JcrConstants.JCR_DATA, binVal);
            Calendar instance = Calendar.getInstance();
            instance.setTime(new Date());
            kramerius.setProperty(JcrConstants.JCR_LASTMODIFIED, instance);           
            return new JackRabbitRepoObject(kramerius);
        } catch (Exception e) {
            throw new RepoAbstractionException(e);
        }
    }

}
