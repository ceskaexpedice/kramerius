package cz.incad.kramerius.rest.apiNew;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.KrameriusRepositoryApiImpl;
import cz.incad.kramerius.rest.apiNew.exceptions.ApiException;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;

import javax.inject.Inject;
import java.io.IOException;
import java.util.regex.Pattern;

public abstract class ApiResource {

    //search index may also contain objects with PIDs like uuid:123e4567-e89b-12d3-a456-426655440000_0, uuid:123e4567-e89b-12d3-a456-426655440000_1, ... (pdf pages)
    private static final Pattern PID_UUID_PATTERN = Pattern.compile("uuid:[a-f0-9]{8}(-[a-f0-9]{4}){3}-[a-f0-9]{12}");

    @Inject
    //TODO should be interface, but then guice would need bind(KrameriusRepository.class).to(KrameriusRepositoryApiImpl) somewhere
    public KrameriusRepositoryApiImpl krameriusRepositoryApi;

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
                throw new NotFoundException("object %s not found in repository", pid);
            }
        } catch (RepositoryException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    protected final void checkObjectAndDatastreamExist(String pid, String dsId) throws ApiException {
        checkObjectExists(pid);
        try {
            boolean exists = krameriusRepositoryApi.getLowLevelApi().datastreamExists(pid, dsId);
            if (!exists) {
                throw new NotFoundException("datastream %s of object %s not found in repository", dsId, pid);
            }
        } catch (RepositoryException | IOException e) {
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }
    }

    protected final void checkObjectAndDatastreamExist(String pid, KrameriusRepositoryApi.KnownDatastreams ds) throws ApiException {
        checkObjectAndDatastreamExist(pid, ds.toString());
    }


    protected final void checkSupportedObjectPid(String pid) {
        if (!PID_UUID_PATTERN.matcher(pid).matches()) {
            throw new BadRequestException("'%s' is not in supported PID format for this operation", pid);
        }
    }
}
