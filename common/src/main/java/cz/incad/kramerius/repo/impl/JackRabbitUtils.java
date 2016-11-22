package cz.incad.kramerius.repo.impl;

import cz.incad.kramerius.utils.conf.KConfiguration;

public class JackRabbitUtils {
    
    private JackRabbitUtils() {}
    
    public static String getJackRabbitFolder() {
        return KConfiguration.getInstance().getConfiguration().getString("jackrabbit.folder");
    }

    public static String getUser() {
        String user = KConfiguration.getInstance().getConfiguration().getString("jackrabbit.user");
        return user;
    }

    public static String getPassword() {
        String user = KConfiguration.getInstance().getConfiguration().getString("jackrabbit.pswd");
        return user;
    }
}
