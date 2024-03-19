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
package cz.incad.kramerius.security.impl.http;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.security.licenses.lock.ExclusiveLockMaps;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;

import static cz.incad.kramerius.security.impl.criteria.utils.CriteriaLicenseUtils.currentThreadReturnObject;

public class RightsResolverFromRequest implements RightsResolver {

    public static final Logger LOGGER = Logger.getLogger(RightsResolverFromRequest.class.getName());
    
    private Logger logger;
    private Provider<HttpServletRequest> provider;

    private RightsManager rightsManager;
    private RightCriteriumContextFactory ctxFactory;
    private Provider<User> currentLoggedUser;
    
    private ExclusiveLockMaps exclusiveLockMaps;


    @Inject
    public RightsResolverFromRequest(Logger logger, Provider<HttpServletRequest> provider, RightsManager rightsManager, RightCriteriumContextFactory contextFactory, Provider<User> currentUserProvider, ExclusiveLockMaps maps) {
        super();
        this.logger = logger;
        this.provider = provider;
        this.rightsManager = rightsManager;
        this.ctxFactory = contextFactory;
        this.currentLoggedUser = currentUserProvider;
        this.exclusiveLockMaps  = maps;
    }

    @Override
    public RightsReturnObject isActionAllowed(String actionName, String pid, String stream, ObjectPidsPath path) {
        try {
            User user = this.currentLoggedUser.get();
            return isAllowedInternalForFedoraDocuments(actionName, pid, stream, path, user);
        } catch (RightCriteriumException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return new RightsReturnObject(null, EvaluatingResultState.FALSE);
    }

    public RightsReturnObject isActionAllowed(User user, String actionName, String pid,String stream, ObjectPidsPath path) {
        try {
            return isAllowedInternalForFedoraDocuments(actionName, pid, stream, path, user);
        } catch (RightCriteriumException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return new RightsReturnObject(null, EvaluatingResultState.FALSE);
    }

    @Override
    public RightsReturnObject[] isActionAllowedForAllPath(String actionName, String pid, String stream, ObjectPidsPath path) {
        try {
            User user = this.currentLoggedUser.get();
            RightCriteriumContext ctx = this.ctxFactory.create(pid,stream, user, getRemoteHost(), IPAddressUtils.getRemoteAddress(this.provider.get()), this, this.exclusiveLockMaps);
            return this.rightsManager.resolveAllPath(ctx, pid, path, actionName, user);
        } catch (RightCriteriumException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            RightsReturnObject[] paths = new RightsReturnObject[path.getLength()];
            for (int i = 0; i < paths.length; i++) {
                paths[0] = new RightsReturnObject(null, EvaluatingResultState.FALSE);
            }
            return paths;
        }
    }

    private String getRemoteHost() {
        HttpServletRequest httpReq = this.provider.get();
        return httpReq.getRemoteHost();
    }

    public RightsReturnObject isAllowedInternalForFedoraDocuments(String actionName, String pid, String stream, ObjectPidsPath path, User user) throws RightCriteriumException {
        RightCriteriumContext ctx = this.ctxFactory.create(pid, stream, user, getRemoteHost(), IPAddressUtils.getRemoteAddress(this.provider.get()), this, this.exclusiveLockMaps);
        RightsReturnObject resolved = this.rightsManager.resolve(ctx, pid, path, actionName, user);
        // TODO: Change it in future
        currentThreadReturnObject.set(resolved);
        return resolved;
    }

    private boolean resultOfResult(EvaluatingResultState result) {
        return result == EvaluatingResultState.TRUE ? true : false;
    }

    @Override
    public boolean isActionAllowed(User user, String actionName) {
        throw new UnsupportedOperationException("still unsupported");
    }
    
}
