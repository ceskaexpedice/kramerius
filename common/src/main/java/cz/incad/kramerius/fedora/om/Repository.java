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

/**
 * The simple object model represents access to fedora 4 repository
 * It is basic tool for ingesting also it is basic point for FedoraAcces facade
 * @author pavels
 */
public interface Repository {

    /**
     * Returns true if object objectExists and if it is raw kramerius object
     * @param ident
     * @return
     * @throws RepositoryException
     */
    boolean objectExists(String ident) throws RepositoryException;


    public String getBoundContext() throws RepositoryException;

    /**
     * Start transaction
     * @throws RepositoryException
     */
    public void startTransaction() throws RepositoryException;

    /**
     * Commit transaction
     * @throws RepositoryException
     */
    public void commitTransaction() throws RepositoryException;
    
    /**
     * Rollback transaction
     * @throws RepositoryException
     */
    public void rollbackTransaction()throws RepositoryException;

    /**
     * Create or find object
     * @param ident Identification of the object
     * @return
     * @throws RepositoryException
     */
    public RepositoryObject createOrFindObject(String ident) throws RepositoryException;

    /**
     * Returns object
     * @param ident
     * @return
     * @throws RepositoryException
     */
    public RepositoryObject getObject(String ident) throws RepositoryException;


}
