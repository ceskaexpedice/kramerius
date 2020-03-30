package cz.incad.kramerius.rest.apiNew.client.v60;

import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.rest.apiNew.exceptions.ApiException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;

import javax.inject.Inject;
import java.io.IOException;

public abstract class ClientApiResource {

    @Inject
    @Named("securedFedoraAccess")
    protected FedoraAccess repositoryAccess;

    public final void checkObjectExists(String pid) throws ApiException {
        try {
            boolean objectExists = this.repositoryAccess.isObjectAvailable(pid);
            if (!objectExists) {
                throw new NotFoundException("object with pid %s not found in repository", pid);
            }
        } catch (IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }
}
