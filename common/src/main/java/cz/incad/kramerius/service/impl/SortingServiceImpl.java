package cz.incad.kramerius.service.impl;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.service.SortingService;

import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @ author vlahoda
 */
public class SortingServiceImpl implements SortingService {

    public static final Logger LOGGER = Logger.getLogger(SortingServiceImpl.class.getName());

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;

    @Override
    public void sortRelations(String pid) {
        //TODO implement
    }

    @Override
    public List<String> sortObjects(List<String> pids, String xpath, boolean numeric) {
        return null;  //TODO implement
    }
}
