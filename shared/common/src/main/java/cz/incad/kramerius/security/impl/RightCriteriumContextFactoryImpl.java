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
import cz.incad.kramerius.security.*;

public class RightCriteriumContextFactoryImpl implements RightCriteriumContextFactory {
    
    private FedoraAccess fedoraAccess;
    private SolrAccess solrAccessNewIndex;
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
    
    



    @Inject
    public void setSolrAccessNewIndex(@Named("new-index")SolrAccess newIndex) {
        this.solrAccessNewIndex = newIndex;
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
    public RightCriteriumContext create(String requestedPID, String requestedStream, User user, String remoteHost, String remoteAddr,  RightsResolver rightsResolver) {
        //RightCriteriumContext ctx = new RightParamEvaluatingContextImpl(requestedPID, requestedStream, user, this.fedoraAccess, this.solrAccess, this.userManager, remoteHost, remoteAddr);
        RightCriteriumContext ctx = new RightParamEvaluatingContextImpl.Builder()
                                        .setRequestedPid(requestedPID)
                                        .setRequestedStream(requestedStream)
                                        .setUser(user)
                                        .setFedoraAccess(this.fedoraAccess)
                                        //.setSolrAccess(this.solrAccess)
                                        .setSolrAccessNewIndex(this.solrAccessNewIndex)
                                        .setUserManager(this.userManager)
                                        .setRemoteHost(remoteHost)
                                        .setRemoteAddress(remoteAddr)
                                        .setRightsResolver(rightsResolver).build();
        return ctx;
    }
}
