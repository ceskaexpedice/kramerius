package cz.incad.kramerius.rest.apiNew;

import cz.incad.kramerius.rest.apiNew.exceptions.ApiException;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;
import org.ceskaexpedice.akubra.AkubraRepository;
import javax.inject.Inject;
import java.util.regex.Pattern;

public abstract class ApiResource {

    /**
     * převzato z Fedory
     *
     * Striktně pro PID nad UUID by mělo být toto: uuid:[a-f0-9]{8}(-[a-f0-9]{4}){3}-[a-f0-9]{12}
     * Nicméně historicky jsou v repozitářích i PIDy neplatných UUID, tudíž je tady tolerovat, omezení by mělo být jen u importu.
     * Toto rozvolnění způsobí přijetí i ne-objektových PIDů jako uuid:123e4567-e89b-12d3-a456-426655440000_0, uuid:123e4567-e89b-12d3-a456-426655440000_1, ... (stránky z pdf, jen ve vyhledávácím indexu, nemají foxml objekt)
     */
    protected static final Pattern PID_PATTERN = Pattern.compile("([A-Za-z0-9]|-|\\.)+:(([A-Za-z0-9])|-|\\.|~|_|(%[0-9A-F]{2}))+");

    @Inject
    public AkubraRepository akubraRepository;

    protected final void checkObjectExists(String pid) throws ApiException {
        if (!objectExists(pid)) {
            throw new NotFoundException("object %s not found in repository", pid);
        }
    }

    protected final boolean objectExists(String pid) throws ApiException {
        return akubraRepository.exists(pid);
    }

    protected final void checkObjectAndDatastreamExist(String pid, String dsId) throws ApiException {
        checkObjectExists(pid);
        boolean exists = akubraRepository.datastreamExists(pid, dsId);
        if (!exists) {
            throw new NotFoundException("datastream %s of object %s not found in repository", dsId, pid);
        }
    }

    protected final void checkSupportedObjectPid(String pid) {
        if (!isSupporetdObjectPid(pid)) {
            throw new BadRequestException("'%s' is not in supported PID format for this operation", pid);
        }
    }

    protected final boolean isSupporetdObjectPid(String pid) {
        return PID_PATTERN.matcher(pid).matches();
    }
}
