package org.kramerius.plugin;

import cz.incad.kramerius.processingindex.ProcessingIndexRebuildFromFoxmlByPid;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

public class ProcessingIndexObjectPlatformStarter {

    @ProcessMethod
    public static void processingMain(
            @ParameterName("pids") String pidsP
    ) {
        ProcessingIndexRebuildFromFoxmlByPid.processingMain(pidsP);
    }
}
