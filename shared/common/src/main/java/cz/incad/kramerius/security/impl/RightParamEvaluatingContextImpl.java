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
package cz.incad.kramerius.security.impl;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;
import cz.incad.kramerius.utils.solr.SolrUtils;

public class RightParamEvaluatingContextImpl implements RightCriteriumContext {

    private String requestedPID;
    private String requestedStream;
    
    private String associatedPID;
    private User user;
    private FedoraAccess fedoraAccess;
    private SolrAccess solrAccess;
    private UserManager userManager;
    
    private String remoteHost;
    private String remoteAddr;    
    
    public RightParamEvaluatingContextImpl(String reqPID, String reqStream, User user, FedoraAccess fedoraAccess, SolrAccess solrAccess, UserManager userManager, String remoteHost, String remoteAddr) {
        super();
        this.requestedPID = reqPID;
        this.requestedStream = reqStream;
        this.user = user;
        this.fedoraAccess = fedoraAccess;
        this.solrAccess = solrAccess;
        this.remoteHost = remoteHost;
        this.remoteAddr = remoteAddr;
        this.userManager = userManager;
    }

    @Override
    public String getRequestedPid() {
        return this.requestedPID;
    }

    
    @Override
    public String getAssociatedPid() {
        return this.associatedPID;
    }

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public FedoraAccess getFedoraAccess() {
        return this.fedoraAccess;
    }

    
    
    @Override
    public void setAssociatedPid(String uuid) {
        this.associatedPID = uuid;
    }

    @Override
    public ObjectPidsPath[] getPathsToRoot() {
        try {
            return this.solrAccess.getPath(getRequestedPid());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getRemoteHost() {
        return this.remoteHost;
    }

    @Override
    public String getRemoteAddr() {
        return this.remoteAddr;
    }

    @Override
    public UserManager getUserManager() {
        return this.userManager;
    }

    @Override
    public String getRequestedStream() {
        return this.requestedStream;
    }

    @Override
    public SolrAccess getSolrAccess() {
        return this.solrAccess;
    }
}
