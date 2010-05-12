package cz.incad.kramerius.service.impl;

import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.service.DeleteService;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class DeleteServiceImpl implements DeleteService {

    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    @Inject
    KConfiguration configuration;

    @Override
    public void deleteTree(String pid, String message) {
        Set<String> pids = fedoraAccess.getPids(pid);
        for (String s : pids) {
            fedoraAccess.getAPIM().purgeObject(s, message, true);
        }
    }

}
