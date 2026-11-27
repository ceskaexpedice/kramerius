package org.kramerius.plugin;

import cz.inovatika.kramerius.services.SolrIndexCopyService;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.io.IOException;

public class SolrLogsIndexCopyStarter {
    @ProcessMethod
    public static void copyLogsIndex(@ParameterName("destinationUrl") @IsRequired String destinationUrl,
                                     @ParameterName("batchSize") Integer batchSize,
                                     @ParameterName("verifyAfterCopy") Boolean verifyAfterCopy) throws IOException {
        SolrIndexCopyService.copyLogsIndex(destinationUrl, batchSize, verifyAfterCopy);
    }
}
