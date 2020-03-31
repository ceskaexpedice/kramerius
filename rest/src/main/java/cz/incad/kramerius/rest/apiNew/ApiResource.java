package cz.incad.kramerius.rest.apiNew;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.AkubraKrameriusRepositoryApi;
import cz.incad.kramerius.rest.apiNew.exceptions.ApiException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;

import javax.inject.Inject;

public abstract class ApiResource {

    @Inject
    public AkubraKrameriusRepositoryApi krameriusRepositoryApi;

    /*
    @Inject
    @Named("securedFedoraAccess")
    private FedoraAccess repository;

    @Inject
    private IResourceIndex resourceIndex;

    private KrameriusRepositoryAccessAdapter repositoryAccess;

    protected final KrameriusRepositoryAccessAdapter getRepositoryAccess() {
        if (repositoryAccess == null) {
            repositoryAccess = new KrameriusRepositoryAccessAdapter(repository, resourceIndex);
        }
        return repositoryAccess;
    }

    protected final void checkObjectExists(String pid) throws ApiException {
        try {
            boolean objectExists = getRepositoryAccess().isObjectAvailable(pid);
            if (!objectExists) {
                throw new NotFoundException("object with pid %s not found in repository", pid);
            }
        } catch (IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }*/

    protected final void checkObjectExists(String pid) throws ApiException {
        try {
            boolean exists = krameriusRepositoryApi.getLowLevelApi().objectExists(pid);
            if (!exists) {
                throw new NotFoundException("object with pid %s not found in repository", pid);
            }
        } catch (RepositoryException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }
}
