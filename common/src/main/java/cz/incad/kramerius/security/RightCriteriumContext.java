/*
 * Copyright (C) 2010 Pavel Stastny
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
package cz.incad.kramerius.security;

import java.net.Inet4Address;

import org.w3c.dom.Document;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;

/**
 * Implementation of this interface holds information 
 * necessary for interpreting rule (current uuid, current user, fed 
 * 
 * @author pavels
 */
public interface RightCriteriumContext {

    /**
     * Puvodne dotazovany objekt
     * @return
     */
    public String getRequestedPid();
    
    /**
     * Dotazovany stream
     * @return
     */
    public String getRequestedStream();
    
    /**
     * Objekt, se kterym je pravo asociovano
     * @return
     */
    public String getAssociatedPid();
    
    public void setAssociatedPid(String uuid);
    
    /**
     * Returns path from leaf to root tree
     * @return
     */
    public ObjectPidsPath[] getPathsToRoot();
    
    /**
     * Current logged user
     * @return
     * @see AbstractUser
     */
    public User getUser();
    
    /**
     * Fedora access
     * @return
     */
    public FedoraAccess getFedoraAccess();

    public SolrAccess getSolrAccess();
    
    public UserManager getUserManager();
    
    public String getRemoteHost();
    
    public String getRemoteAddr();
}
