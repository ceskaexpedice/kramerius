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
            @ParameterName("pid")  @IsRequired String pidsP,
            @ParameterName("ignoreInconsistentObjects") Boolean ignoreInconsistentObjects,
            @ParameterName("title") String title
    ) {

        LOGGER.info("Spouštění indexerMain s parametry:");
        LOGGER.info(String.format("  type: %s", type.toUpperCase()));
        LOGGER.info(String.format("  pidsP: %s", pidsP));
        LOGGER.info(String.format("  ignoreInconsistentObjects: %b", ignoreInconsistentObjects));
        LOGGER.info(String.format("  title: %s", title));

        NewIndexerProcessIndexObject.indexerMain(type.toUpperCase(), pidsP, ignoreInconsistentObjects, title);
    }
}
