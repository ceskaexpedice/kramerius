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
import java.util.HashMap;
import java.util.Map;

import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMaps;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import org.ceskaexpedice.akubra.AkubraRepository;

public class RightParamEvaluatingContextImpl implements RightCriteriumContext {

    private ExclusiveLockMaps exclusiveLockMaps;
    
    private String requestedPID;
    private String requestedStream;
    private String associatedPID;
    private User user;
    private AkubraRepository akubraRepository;
    //private SolrAccess solrAccess;
    private SolrAccess solrAccessNewIndex;

    private UserManager userManager;
    private String remoteHost;
    private String remoteAddr;    
    

    private Map<String, String> map = new HashMap<>();

    private SecuredActions action;

    private RightsResolver rightsResolver;

    private RightParamEvaluatingContextImpl(Builder builder) {
        super();

        this.requestedPID = builder.requestedPID;
        this.requestedStream = builder.requestedStream;
        this.user = builder.user;
        this.akubraRepository = builder.akubraRepository;
        this.solrAccessNewIndex = builder.solrAccessNewIndex;
        this.remoteHost = builder.remoteHost;
        this.remoteAddr = builder.remoteAddr;
        this.userManager = builder. userManager;
        this.action = builder.action;
        this.rightsResolver = builder.rightsResolver;
        
        
        this.exclusiveLockMaps = builder.exclusiveLockMaps;
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
    public AkubraRepository getAkubraRepository() {
        return this.akubraRepository;
    }
    
    @Override
    public void setAssociatedPid(String uuid) {
        this.associatedPID = uuid;
    }

    @Override
    public ObjectPidsPath[] getPathsToRoot() {
        try {
            return this.getSolrAccessNewIndex().getPidPaths(getRequestedPid());
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
    public SolrAccess getSolrAccessNewIndex() {
        return this.solrAccessNewIndex;
    }

    @Override
    public SecuredActions getAction() {
        return this.action;
    }

    @Override
    public RightsResolver getRightsResolver() {
        return this.rightsResolver;
    }

    @Override
    public Map<String, String> getEvaluateInfoMap() {
        return this.map;
    }
    
    @Override
    public ExclusiveLockMaps getExclusiveLockMaps() {
        return exclusiveLockMaps;
    }

    public static class Builder {
        
        protected ExclusiveLockMaps exclusiveLockMaps;
        
        protected String requestedPID;
        protected String requestedStream;

        protected String associatedPID;
        protected User user;
        protected AkubraRepository akubraRepository;
        protected SolrAccess solrAccess;
        private SolrAccess solrAccessNewIndex;
        protected UserManager userManager;

        protected String remoteHost;
        protected String remoteAddr;

        protected SecuredActions action;

        protected RightsResolver rightsResolver;

        public Builder() {}

        public Builder setRequestedPid(String requestedPID) {
            this.requestedPID = requestedPID;
            return this;
        }
        public Builder setRequestedStream(String stream) {
            this.requestedStream = stream;
            return this;
        }
        public Builder setAssociatedPid(String assocPid) {
            this.associatedPID = assocPid;
            return this;
        }

        public Builder setUserManager(UserManager userManager) {
            this.userManager = userManager;
            return this;
        }

        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        public Builder setAkubraRepository(AkubraRepository akubraRepository) {
            this.akubraRepository = akubraRepository;
            return this;
        }

        public Builder setSolrAccess(SolrAccess sa) {
            this.solrAccess = sa;
            return this;
        }

        public Builder setSolrAccessNewIndex(SolrAccess newIndex) {
            this.solrAccessNewIndex = newIndex;
            return this;
        }

        public Builder setRemoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
            return this;
        }

        public Builder setRemoteAddress(String remoteAddr) {
            this.remoteAddr = remoteAddr;
            return this;
        }

        public Builder setAction(String action) {
            this.action = SecuredActions.findByFormalName(action);
            return this;
        }

        public Builder setRightsResolver(RightsResolver resolver) {
            this.rightsResolver = resolver;
            return this;
        }

        public RightParamEvaluatingContextImpl build() {
            return new RightParamEvaluatingContextImpl(this);
        }

        public Builder setExclusiveLockMaps(ExclusiveLockMaps exclusiveLockMaps) {
            this.exclusiveLockMaps = exclusiveLockMaps;
            return this;
        }
        
        

    }
}
