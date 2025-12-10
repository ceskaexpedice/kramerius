package org.kramerius.plugin;

import cz.kramerius.searchIndex.NewIndexerProcessIndexObject;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.util.logging.Logger;

public class SearchIndexObjectPlatformStarter {

    public static Logger LOGGER = Logger.getLogger(SearchIndexObjectPlatformStarter.class.getName());

    @ProcessMethod
    public static void indexerMain(
            @ParameterName("type") @IsRequired String type,
            @ParameterName("pid")  String pidsP,
            @ParameterName("pidlist")  String pidsList,
            @ParameterName("pidlist_file")  String pidListFile,
            @ParameterName("ignoreInconsistentObjects") Boolean ignoreInconsistentObjects,
            @ParameterName("title") String title
    ) {

        LOGGER.info("--- Starting method: indexerMain ---");
        LOGGER.info("Parameter 'type': " + type);
        LOGGER.info("Parameter 'pid': " + pidsP);
        // Non-required parameters check for null
        LOGGER.info("Parameter 'ignoreInconsistentObjects': " + (ignoreInconsistentObjects == null ? "N/A" : ignoreInconsistentObjects));
        LOGGER.info("Parameter 'title': " + (title == null ? "N/A" : title));
        LOGGER.info("-----------------------------------");

        NewIndexerProcessIndexObject.indexerMain(type.toUpperCase(), pidsP, ignoreInconsistentObjects, title);
    }
}
