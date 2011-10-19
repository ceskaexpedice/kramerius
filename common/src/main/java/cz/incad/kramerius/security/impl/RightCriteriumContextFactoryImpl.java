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

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.AbstractUser;
import cz.incad.kramerius.security.RightCriteriumContext;
import cz.incad.kramerius.security.RightCriteriumContextFactory;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.UserManager;

public class RightCriteriumContextFactoryImpl implements RightCriteriumContextFactory {
    
    private FedoraAccess fedoraAccess;
    private SolrAccess solrAccess;
    private UserManager userManager;
    
    public RightCriteriumContextFactoryImpl() {
        super();
    }

    public FedoraAccess getFedoraAccess() {
        return fedoraAccess;
    }

    @Inject
    public void setFedoraAccess(@Named("securedFedoraAccess")FedoraAccess fedoraAccess) {
        this.fedoraAccess = fedoraAccess;
    }
    
    

    public SolrAccess getSolrAccess() {
        return solrAccess;
    }

    @Inject
    public void setSolrAccess(SolrAccess solrAccess) {
        this.solrAccess = solrAccess;
    }

    
    
    public UserManager getUserManager() {
        return userManager;
    }

    @Inject
    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public static synchronized RightCriteriumContextFactoryImpl newFactory() {
        return new RightCriteriumContextFactoryImpl();
    }
    
    @Override
    public RightCriteriumContext create(String requestedPID, String requestedStream,  User user, String remoteHost, String remoteAddr) {
        RightCriteriumContext ctx = new RightParamEvaluatingContextImpl(requestedPID, requestedStream, user, this.fedoraAccess, this.solrAccess, this.userManager, remoteHost, remoteAddr);
        return ctx;
    }
}
