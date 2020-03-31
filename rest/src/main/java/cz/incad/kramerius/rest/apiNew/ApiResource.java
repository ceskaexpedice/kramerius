package cz.incad.kramerius.rest.apiNew;

import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.repository.KrameriusRepositoryAccessAdapter;
import cz.incad.kramerius.resourceindex.IResourceIndex;
import cz.incad.kramerius.rest.apiNew.exceptions.ApiException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;

import javax.inject.Inject;
import java.io.IOException;

public abstract class ApiResource {

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
    }
}
