package org.kramerius.plugin;

import cz.inovatika.kramerius.services.SolrIndexCopyService;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.io.IOException;

public class SolrProcessingIndexCopyStarter {
    @ProcessMethod
    public static void copyProcessingIndex(@ParameterName("destinationUrl") @IsRequired String destinationUrl,
                                           @ParameterName("batchSize") Integer batchSize,
                                           @ParameterName("verifyAfterCopy") Boolean verifyAfterCopy) throws IOException {
        SolrIndexCopyService.copyProcessingIndex(destinationUrl, batchSize, verifyAfterCopy);
    }
}
