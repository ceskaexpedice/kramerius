package org.kramerius.plugin;

import cz.incad.kramerius.processingindex.ProcessingIndexRebuild;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;
import org.ceskaexpedice.processplatform.api.context.PluginContext;
import org.ceskaexpedice.processplatform.api.context.PluginContextHolder;

import java.io.IOException;

public class ProcessingIndexRepositoryPlatformStarter {

    @ProcessMethod
    public static void rebuildMain(
            @ParameterName("action") String action
    ) throws IOException {
        ProcessingIndexRebuild.rebuildMain(action);
    }
}
