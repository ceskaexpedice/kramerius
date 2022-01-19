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
import java.util.Map;

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


    public Map<String, String> getEvaluateInfoMap();

    /**
     * Requested object
     * @return
     */
    public String getRequestedPid();
    
    /**
     * Requested stream
     * @return
     */
    public String getRequestedStream();
    
    /**
     * Object associated with right
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

    /**
     * Return solr access
     * @return
     */
    public SolrAccess getSolrAccess();

    /**
     * Return solr access
     * @return
     */
    public SolrAccess getSolrAccessNewIndex();


    /**
     * Returns user's manager
     * @return
     * @see UserManager
     */
    public UserManager getUserManager();

    /**
     * Returns remote host from the request
     * @return
     */
    public String getRemoteHost();

    /**
     * Returns remote address from the request
     * @return
     */
    public String getRemoteAddr();

    /**
     * Returns secured action
     * @return
     */
    public SecuredActions getAction();

    /**
     *
     * @return
     */
    public RightsResolver getRightsResolver();


}
