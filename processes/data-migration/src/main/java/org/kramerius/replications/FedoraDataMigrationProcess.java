package org.kramerius.replications;

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


    /* TODO pepo
    @Process
    public static void replications(@ParameterName("url") String url,
                                    @ParameterName("username") String userName,
                                    @ParameterName("pswd")String pswd,
                                    @ParameterName("replicateCollections")String replicateCollections,
                                    @ParameterName("replicateImages")String replicateImages,
                                    @ParameterName("previousProcess")String previousProcessUUID) throws IOException {
*/
    public static void replications( String url,
                                    String userName,
                                    String pswd,
                                    String replicateCollections,
                                    String replicateImages,
                                    String previousProcessUUID) throws IOException {
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
