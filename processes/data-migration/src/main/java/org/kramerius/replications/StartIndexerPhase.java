package org.kramerius.replications;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.resourceindex.ProcessingIndexFeeder;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.service.impl.IndexerProcessStarter;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.solr.client.solrj.SolrServerException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StartIndexerPhase extends  AbstractPhase{

    public static final Logger LOGGER = Logger.getLogger(StartIndexerPhase.class.getName());


    @Override
    public void start(String url, String userName, String pswd, String replicationCollections, String replicateImages) throws PhaseException {
        //fedora.topLevelModels=monograph,periodical,soundrecording,manuscript,map,sheetmusic
        List<String> models = models();
        IndexerProcessStarter.spawnIndexerForModel(models.toArray(new String[models.size()]));
        LOGGER.info("OBJECT SCHEDULED FOR INDEXING.");

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
