package cz.incad.kramerius.rest.apiNew.client.v60;

import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.repository.KrameriusRepositoryAccessAdapter;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.rest.apiNew.exceptions.ApiException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;

import javax.inject.Inject;
import java.io.IOException;

public abstract class ClientApiResource {

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess repository;

    @Inject
    IResourceIndex resourceIndex;

    private KrameriusRepositoryAccessAdapter repositoryAccessAdapter;

    //TODO: handle dependency injection properly instead of this method
    public KrameriusRepositoryAccessAdapter getRepositoryAccess() {
        if (repositoryAccessAdapter == null) {
            repositoryAccessAdapter = new KrameriusRepositoryAccessAdapter(repository, resourceIndex);
        }
        return repositoryAccessAdapter;
    }

    public final void checkObjectExists(String pid) throws ApiException {
        try {
            boolean objectExists = getRepositoryAccess().isObjectAvailable(pid);
            if (!objectExists) {
                throw new NotFoundException("object with pid %s not found in repository", pid);
            }
        } catch (IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }
}
