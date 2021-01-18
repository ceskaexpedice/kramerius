package cz.kramerius.searchIndex.repositoryAccessImpl.krameriusNoApi;

import cz.incad.kramerius.FedoraAccess;
import cz.kramerius.searchIndex.repositoryAccessImpl.RepositoryAccessImplAbstract;

import java.io.IOException;
import java.io.InputStream;

/**
 * This implementation uses FedoraAccess, i.e. avoids overhead of the HTTPS stack.
 */
public class RepositoryAccessImplByKrameriusDirect extends RepositoryAccessImplAbstract {

    //@Inject
    //@Named("rawFedoraAccess")
    //@Named("securedFedoraAccess")
    private final FedoraAccess fedoraAccess;

    public RepositoryAccessImplByKrameriusDirect(FedoraAccess fedoraAccess) {
        this.fedoraAccess = fedoraAccess;
    }

    @Override
    public boolean isObjectAvailable(String pid) throws IOException {
        return fedoraAccess.isObjectAvailable(pid);
    }

    @Override
    public InputStream getFoxml(String pid) throws IOException {
        //TODO: znamena false, ze se nepribali datastreamy jako ocr, ktere internally referenced?
        //pro indexacni proces to neni potreba, ale melo by se to vyjasnit
        return fedoraAccess.getFoxml(pid, false);
    }

    @Override
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        if (isStreamAvailable(pid, datastreamName)) {
            return fedoraAccess.getDataStream(pid, datastreamName);
        } else {
            return null;
        }
    }

    @Override
    public boolean isStreamAvailable(String pid, String datastreamName) throws IOException {
        return fedoraAccess.isStreamAvailable(pid, datastreamName);
    }

    @Override
    public String getDatastreamMimeType(String pid, String datastreamName) throws IOException {
        if (isStreamAvailable(pid, datastreamName)) {
            return fedoraAccess.getMimeTypeForStream(pid, datastreamName);
        } else {
            return null;
        }
    }
}
