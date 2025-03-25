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

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMaps;
import org.ceskaexpedice.akubra.AkubraRepository;

public class RightCriteriumContextFactoryImpl implements RightCriteriumContextFactory {
    
    private AkubraRepository akubraRepository;
    private SolrAccess solrAccessNewIndex;
    private UserManager userManager;
    
    private ExclusiveLockMaps lockMaps;
    
    
    public RightCriteriumContextFactoryImpl() {
        super();
    }

    public AkubraRepository getAkubraRepository() {
        return akubraRepository;
    }

    @Inject
    public void setAkubraRepository(SecuredAkubraRepository akubraRepository) {
        this.akubraRepository = akubraRepository;
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
    
    
    @Inject
    public void setLockMaps(ExclusiveLockMaps lockMaps) {
        this.lockMaps = lockMaps;
    }

    public RightCriteriumContextFactoryImpl(ExclusiveLockMaps lockMaps) {
        super();
        this.lockMaps = lockMaps;
    }

    @Override
    public RightCriteriumContext create(String requestedPID, String requestedStream, User user, String remoteHost, String remoteAddr,  RightsResolver rightsResolver, ExclusiveLockMaps exclusiveLocks) {
        RightCriteriumContext ctx = new RightParamEvaluatingContextImpl.Builder()
                                        .setRequestedPid(requestedPID)
                                        .setRequestedStream(requestedStream)
                                        .setUser(user)
                                        .setAkubraRepository(this.akubraRepository)
                                        //.setSolrAccess(this.solrAccess)
                                        .setSolrAccessNewIndex(this.solrAccessNewIndex)
                                        .setUserManager(this.userManager)
                                        .setRemoteHost(remoteHost)
                                        .setRemoteAddress(remoteAddr)
                                        .setExclusiveLockMaps(exclusiveLocks)
                                        .setRightsResolver(rightsResolver).build();
        return ctx;
    }
}
