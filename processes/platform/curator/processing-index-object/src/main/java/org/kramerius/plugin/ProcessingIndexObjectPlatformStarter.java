package org.kramerius.plugin;

import cz.incad.kramerius.processingindex.ProcessingIndexRebuildFromFoxmlByPid;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

public class ProcessingIndexObjectPlatformStarter {

    @ProcessMethod
    public static void processingMain(
            @ParameterName("target") @IsRequired String target
    ) {

        ProcessingIndexRebuildFromFoxmlByPid.processingMain(target);
    }
}
