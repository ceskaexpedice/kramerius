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

package cz.incad.feedrepo;

import org.fcrepo.client.FedoraObject;



/**
 * This is basic repo abstraction dedicated for ONLY feeding repo
 * @author pavels
 */
public interface RepoAbstraction {
    
    /**
     * Open repository
     * @throws RepoAbstractionException
     */
    public void open() throws RepoAbstractionException;
    
    /**
     * Close repository
     * @throws RepoAbstractionException
     */
    public void close() throws RepoAbstractionException;
    
    
    /**
     * Start transaction
     * @throws RepoAbstractionException
     */
    public void startTransaction() throws RepoAbstractionException;

    /**
     * Commit transaction
     * @throws RepoAbstractionException
     */
    public void commitTransaction() throws RepoAbstractionException;
    
    /**
     * Rollback transaction
     * @throws RepoAbstractionException
     */
    public void rollbackTransaction()throws RepoAbstractionException;

    /**
     * Create new item
     * @param ident Identification of the object
     * @return
     * @throws RepoAbstractionException
     */
    public RepositoryObjectAbstraction createObject(String ident) throws RepoAbstractionException;


}
