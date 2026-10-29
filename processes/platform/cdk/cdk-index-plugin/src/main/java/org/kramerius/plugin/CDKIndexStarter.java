package org.kramerius.plugin;

import cz.incad.kramerius.plugin.CDKIndexCollection;
import cz.kramerius.searchIndex.NewIndexerProcessIndexObject;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.ceskaexpedice.processplatform.api.context.PluginContext;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;
import org.ceskaexpedice.processplatform.common.model.ScheduleSubProcess;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CDKIndexStarter {

    public static Logger LOGGER = Logger.getLogger(CDKIndexStarter.class.getName());

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
        CDKIndexCollection.indexCollectionMain();
/*
        Map<String, String> payload = new HashMap<>();
        ScheduleSubProcess subProcess = new ScheduleSubProcess("cdk-index-collection", payload);
        PluginContext pluginContext = PluginContextHolder.getContext();
        pluginContext.scheduleSubProcess(subProcess);

 */

    }
}
