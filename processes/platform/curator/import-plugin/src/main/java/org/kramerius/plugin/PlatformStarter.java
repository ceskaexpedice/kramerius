package org.kramerius.plugin;

import org.apache.solr.client.solrj.SolrServerException;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.kramerius.Import;

import java.io.IOException;
import java.util.logging.Logger;

public class PlatformStarter {

    public static final Logger LOGGER = Logger.getLogger(PlatformStarter.class.getName());

    @ProcessMethod
    public static void importMain(
            @ParameterName("inputDataDir") @IsRequired String importDirFromArgs,
            @ParameterName("startIndexer") @IsRequired Boolean startIndexerFromArgs,
            @ParameterName("license") @IsRequired String license,
            @ParameterName("collections") @IsRequired String addCollection,
            @ParameterName("pathtype") String pathtype,
            @ParameterName("indexationType") @IsRequired String scheduleStrategy
    ) throws IOException, SolrServerException {
        //TODO: Pathtype - check
        Import.importMain(importDirFromArgs, startIndexerFromArgs, license, addCollection, scheduleStrategy);
    }
}
