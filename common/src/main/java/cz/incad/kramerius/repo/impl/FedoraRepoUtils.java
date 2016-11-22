package cz.incad.kramerius.repo.impl;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class FedoraRepoUtils {
    
    private FedoraRepoUtils() {}

    public static String getFedora4Host() {
        return KConfiguration.getInstance().getConfiguration().getString("fedora4Host");
    }
}
