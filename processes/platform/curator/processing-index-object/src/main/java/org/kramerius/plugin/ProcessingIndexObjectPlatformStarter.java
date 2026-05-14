package org.kramerius.plugin;

import cz.incad.kramerius.processingindex.ProcessingIndexRebuildFromFoxmlByPid;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.ceskaexpedice.processplatform.api.context.PluginContext;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;
import org.ceskaexpedice.processplatform.common.model.ScheduleSubProcess;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ProcessingIndexObjectPlatformStarter {

    public static final Logger LOGGER = Logger.getLogger(ProcessingIndexObjectPlatformStarter.class.getName());

    @ProcessMethod
    public static void processingMain(
            @ParameterName("target") @IsRequired String target,
            @ParameterName("indexer") Boolean searchIndex
    ) {

        LOGGER.info("Processing Main");
        LOGGER.info("target: " + target);
        LOGGER.info("indexer: " + searchIndex);
        ProcessingIndexRebuildFromFoxmlByPid.processingMain(target);
        if (searchIndex) {
            Map<String, String> payload = new HashMap<>();
            payload.put("pid", target);
            payload.put("title", "Building search index");
            payload.put("type", "object");
            payload.put("ignoreInconsistentObjects", "true");

            ScheduleSubProcess subProcess = new ScheduleSubProcess("new_indexer_index_object", payload);
            PluginContext pluginContext = PluginContextHolder.getContext();
            pluginContext.scheduleSubProcess(subProcess);
        }
    }
}
