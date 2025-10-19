package org.kramerius.replications;

import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import static org.kramerius.replications.K4ReplicationProcess.*;


import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;



public class FedoraDataMigrationProcess {

    public static final Logger LOGGER = Logger.getLogger(FedoraDataMigrationProcess.class.getName());

    public static Phase[] PHASES = new Phase[] {
            new ZeroPhase(),
            new IterateThroughIndexPhase(),
            new SecondPhase(),
            new StartIndexerPhase()
    };


    @ProcessMethod
    public static void replicationsMain(@ParameterName("url") String url,
                                    @ParameterName("username") String userName,
                                    @ParameterName("pswd")String pswd,
                                    @ParameterName("replicateCollections")String replicateCollections,
                                    @ParameterName("replicateImages")String replicateImages,
                                    @ParameterName("previousProcess")String previousProcessUUID) throws IOException {
        if ((previousProcessUUID != null) && (!previousProcessUUID.equals(""))) {
            LOGGER.info("restarting ..");
            String muserDir = System.getProperty("user.dir");
            File previousProcessFolder = new File(new File(muserDir).getParentFile(), previousProcessUUID);
            if (previousProcessFolder.exists()) {
                restart(previousProcessUUID, previousProcessFolder, url, userName, pswd,replicateCollections,replicateImages,PHASES);
            } else throw new RuntimeException("expect of existing folder '"+previousProcessFolder.getAbsolutePath()+"'");
        } else {
            // start
            start(url, userName, pswd,replicateCollections, replicateImages,PHASES);
        }

    }
}
