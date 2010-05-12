package cz.incad.kramerius.service.impl;

import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.service.ExportService;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ExportServiceImpl implements ExportService {

    
    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess fedoraAccess;
    @Inject
    KConfiguration configuration;

    @Override
    public void exportTree(String pid) {
        Set<String> pids = fedoraAccess.getPids(pid);
        for (String s : pids) {
            store(s, fedoraAccess.getAPIM().export(s, "info:fedora/fedora-system:FOXML-1.1", "archive"));
        }
    }

    private void store(String name, byte[] contents){
        //TODO
    }
}
