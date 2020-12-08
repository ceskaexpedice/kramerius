package cz.incad.kramerius.rest.apiNew.client.v60;

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
    Provider<User> userProvider;

    @Inject
    RightsResolver rightsResolver;

    //TODO: use new search index instead
    @com.google.inject.Inject
    SolrAccess solrAccess;

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

    public void checkUserByJsessionidIsAllowedToReadDatastream(String pid, KrameriusRepositoryApi.KnownDatastreams datastreamId) {
        try {
            String dsId = datastreamId.toString();
            User user = this.userProvider.get();
            boolean allowed = userIsAllowedToReadDatastream(user, pid, dsId);
            if (!allowed) {
                throw new ForbiddenException("user '%s' is not allowed to read datastream '%s' of object '%s'", user.getLoginname(), dsId, pid); //403
            }
        } catch (IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    //see cz.incad.kramerius.security.SecuredFedoraAccessImpl.getDataStream(String pid, String datastreamName)
    private boolean userIsAllowedToReadDatastream(User user, String pid, String datastreamName) throws IOException {
        ObjectPidsPath[] paths = this.solrAccess.getPath(pid);
        if (paths.length == 0) {
            throw new InternalErrorException("illegal state: no paths for object %s found in search index", pid);
            //or maybe build paths from resource/processing index
            //but user should not access page before it is indexed anyway
            //so eventual consistency vs. "API doesn't (at least seemingly) depend on search index"
        }
        for (int i = 0; i < paths.length; i++) {
            ObjectPidsPath path = paths[i];
            if (this.rightsResolver.isActionAllowed(user, SecuredActions.READ.getFormalName(), pid, datastreamName, path).flag()) {
                return true;
            }
        }
        return false;
    }

}
