package cz.incad.kramerius.rest.apiNew.client.v60;

import com.google.inject.name.Named;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.rest.apiNew.ApiResource;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.UnauthorizedException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;

public abstract class ClientApiResource extends ApiResource {
    @Inject
    protected Provider<User> userProvider;

    // basic rights resolver  / images
    @Inject
    protected RightsResolver rightsResolver;

    @Inject
    @Named("cachedRightsResolver")
    protected RightsResolver cachedRightsResolver;

    @Inject
    @Named("new-index")
    protected SolrAccess solrAccess;

    public SolrAccess getSolrAccess() {
        return solrAccess;
    }

    public RightsResolver getRightsResolver() {
        return rightsResolver;
    }
    
    public Provider<User> getUserProvider() {
        return userProvider;
    }
    
    public void checkCurrentUserByJsessionidIsAllowedToPerformGlobalSecuredAction(SecuredActions action) {
        User user = this.userProvider.get();
        if (user == null || user.getLoginname().equals("not_logged")) {
            throw new UnauthorizedException(); //401
        } else {
            boolean allowed = this.rightsResolver.isActionAllowed(user, action.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH).flag();
            if (!allowed) {
                throw new ForbiddenException("user '%s' is not allowed to perform global action '%s'", user.getLoginname(), action.getFormalName()); //403
            }
        }
    }

    public void checkUserIsAllowedToReadDatastream(String pid, KrameriusRepositoryApi.KnownDatastreams datastreamId) {
        try {
            checkSupportedObjectPid(pid);
            String dsId = datastreamId.toString();
            User user = this.userProvider.get();
            boolean allowed = userIsAllowedToRead(user, pid);
            if (!allowed) {
                throw new ForbiddenException("user '%s' is not allowed to read datastream '%s' of object '%s'", user.getLoginname(), dsId, pid); //403
            }
        } catch (IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }


    public void checkUserIsAllowedToReadObject(String pid) {
        try {
            checkSupportedObjectPid(pid);
            User user = this.userProvider.get();
            boolean allowed = userIsAllowedToRead(user, pid);
            if (!allowed) {
                throw new ForbiddenException("user '%s' is not allowed to read  object '%s'", user.getLoginname(), pid); //403
            }
        } catch (IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    public void checkUserByJsessionidIsAllowedToReadIIPTile(String pid) {
        try {
            checkSupportedObjectPid(pid);
            User user = this.userProvider.get();
            boolean allowed = userIsAllowedToReadCachedVersion(user, pid);
            if (!allowed) {
                throw new ForbiddenException("user '%s' is not allowed to read tile of object '%s'", user.getLoginname(),  pid); //403
            }
        } catch (IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }



    //see cz.incad.kramerius.security.SecuredFedoraAccessImpl.getDataStream(String pid, String datastreamName)
    private boolean userIsAllowedToRead(User user, String pid) throws IOException {
        return userIsAllowedToRead(this.rightsResolver, user, pid);
    }
    private boolean userIsAllowedToReadCachedVersion(User user, String pid) throws IOException {
        return userIsAllowedToRead(this.cachedRightsResolver, user, pid);
    }

    private boolean userIsAllowedToRead(RightsResolver rightsResolver, User user, String pid) throws IOException {
        checkSupportedObjectPid(pid);
        ObjectPidsPath[] paths = this.solrAccess.getPidPaths(pid);
        if (paths.length == 0) {
            throw new InternalErrorException("illegal state: no paths for object %s found in search index", pid);
            //or maybe build paths from resource/processing index
            //but user should not access page before it is indexed anyway
            //so eventual consistency vs. "API doesn't (at least seemingly) depend on search index"
        }
        for (int i = 0; i < paths.length; i++) {
            ObjectPidsPath path = paths[i];
            if (rightsResolver.isActionAllowed(user, SecuredActions.A_READ.getFormalName(), pid, null, path.injectRepository()).flag()) {
                return true;
            }
        }
        return false;
    }

}
