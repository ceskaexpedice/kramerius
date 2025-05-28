package org.kramerius.replications;

import cz.incad.kramerius.service.impl.IndexerProcessStarter;
import cz.incad.kramerius.utils.conf.KConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StartIndexerPhase extends  AbstractPhase{

    public static final Logger LOGGER = Logger.getLogger(StartIndexerPhase.class.getName());


    @Override
    public void start(String url, String userName, String pswd, String replicationCollections, String replicateImages) throws PhaseException {
        //fedora.topLevelModels=monograph,periodical,soundrecording,manuscript,map,sheetmusic,graphic,archive
        try {
            String waitIndexerProperty = System.getProperties().containsKey("ingest.startIndexer.wait") ? System.getProperty("ingest.startIndexer.wait") : KConfiguration.getInstance().getConfiguration().getString("ingest.startIndexer.wait", "1000");
            LOGGER.info("Waiting for soft commit :"+waitIndexerProperty+" s");
            Thread.sleep(Integer.parseInt(waitIndexerProperty));

            List<String> models = models();
            IndexerProcessStarter.spawnIndexerForModel(models.toArray(new String[models.size()]));
            LOGGER.info("OBJECT SCHEDULED FOR INDEXING.");
        } catch (InterruptedException e) {
            throw new PhaseException(this,e);
        }
    }

    private List<String> models() {
        return Arrays.stream(KConfiguration.getInstance().getPropertyList("fedora.topLevelModels")).map((model)->{
                return model.startsWith("model:") ? model : "model:"+model;
            }).collect(Collectors.toList());
    }

    @Override
    public void restart(String previousProcessUUID, File previousProcessRoot, boolean phaseCompleted, String url, String userName, String pswd, String replicationCollections, String replicateImages) throws PhaseException {
        List<String> models = models();
        IndexerProcessStarter.spawnIndexerForModel(models.toArray(new String[models.size()]));
        LOGGER.info("OBJECT SCHEDULED FOR INDEXING.");
   }
}
