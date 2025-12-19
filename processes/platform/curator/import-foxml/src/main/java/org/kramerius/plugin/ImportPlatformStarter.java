package org.kramerius.plugin;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.kramerius.Import;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ImportPlatformStarter {

    public static final Logger LOGGER = Logger.getLogger(ImportPlatformStarter.class.getName());

    @ProcessMethod
    public static void importMain(
            @ParameterName("inputDataDir") @IsRequired String importDirFromArgs,
            @ParameterName("startIndexer") @IsRequired Boolean startIndexerFromArgs,
            @ParameterName("license") String license,
            @ParameterName("collections")String addCollection,
            @ParameterName("pathtype") String pathtype,
            @ParameterName("indexationType") String scheduleStrategy
    ) throws IOException, SolrServerException {


        File inputDataDir = null;
        if (pathtype == null || pathtype.equals("relative")) {
            inputDataDir = new File(KConfiguration.getInstance().getProperty( "import.directory")+File.separator+importDirFromArgs);
        } else {
            inputDataDir = new File(importDirFromArgs);
        }

        LOGGER.info("--- Starting method: importMain ---");
        LOGGER.info("Parameter 'inputDataDir': " + inputDataDir);
        LOGGER.info("Parameter 'startIndexer': " + startIndexerFromArgs);
        LOGGER.info("Parameter 'license': " + (license == null ? "N/A" : license));
        LOGGER.info("Parameter 'collections': " + (addCollection == null ? "N/A" : addCollection));
        LOGGER.info("Parameter 'pathtype': " + (pathtype == null ? "N/A" : pathtype));
        LOGGER.info("Parameter 'indexationType': " + (scheduleStrategy == null ? "N/A" : scheduleStrategy));
        LOGGER.info("----------------------------------");

        LOGGER.info("Process platform " + inputDataDir.getAbsolutePath()+"; start indexer " + startIndexerFromArgs);
        //TODO: Pathtype - check
        Import.importMain(inputDataDir.getAbsolutePath(), startIndexerFromArgs, license, addCollection, scheduleStrategy);
    }
}
