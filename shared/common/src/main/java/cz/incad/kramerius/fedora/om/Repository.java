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

package cz.incad.kramerius.fedora.om;

import com.qbizm.kramerius.imp.jaxb.DigitalObject;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import org.fcrepo.client.FcrepoOperationFailedException;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * The simple object model represents access to fedora 4 repository
 * It is basic tool for ingesting also it is basic point for FedoraAccess facade
 * @author pavels
 */
public abstract class Repository {

    /**
     * Returns true if object objectExists and if it is raw kramerius object
     * @param ident
     * @return
     * @throws RepositoryException
     */
    public abstract boolean  objectExists(String ident) throws RepositoryException;


    public abstract String getBoundContext() throws RepositoryException;

    /**
     * Commits current transaction
     * @throws RepositoryException
     */
    public abstract void commitTransaction() throws RepositoryException;

    /**
     * Rolls back current transaction
     * @throws RepositoryException
     */
    public abstract void rollbackTransaction()throws RepositoryException;

    /**
     * Creates an empty object or finds existing object
     * @param ident Identification of the object
     * @return
     * @throws RepositoryException
     */
    public abstract RepositoryObject createOrFindObject(String ident) throws RepositoryException;

    /**
     * Ingest new digital object from the provided object representation
     * @param contents
     * @return
     * @throws RepositoryException
     */
    public abstract RepositoryObject ingestObject(DigitalObject contents) throws RepositoryException;


    /**
     * Returns object
     * @param ident
     * @return
     * @throws RepositoryException
     */
    public abstract RepositoryObject getObject(String ident) throws RepositoryException;

    /**
     * Deletes object
     * @param pid
     * @throws RepositoryException
     */
    public abstract void deleteobject(String pid) throws RepositoryException;

    /**
     * Deletes object, possibly without removing relations pointing at this object (from Resource index)
     * @param pid
     * @param includingRelationsWithItAsTarget if true, also relations with this object as a target will be removed from Resource index.
     *                                         Which might not be desirable, for example if you want to replace the object with newer version, but keep relations pointing at it.
     * @throws RepositoryException
     */
    public abstract void deleteobject(String pid, boolean includingRelationsWithItAsTarget) throws RepositoryException;

    /**
     * Returns processing index feeder
     * @return
     * @throws RepositoryException
     */
    public abstract ProcessingIndexFeeder getProcessingIndexFeeder() throws RepositoryException;




    public abstract void iterateObjects(Consumer<String> consumer ) throws RepositoryException, FcrepoOperationFailedException, IOException;

}
