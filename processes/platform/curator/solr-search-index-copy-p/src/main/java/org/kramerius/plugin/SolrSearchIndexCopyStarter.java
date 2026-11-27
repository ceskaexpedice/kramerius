package org.kramerius.plugin;

import cz.inovatika.kramerius.services.SolrIndexCopyService;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.io.IOException;

public class SolrSearchIndexCopyStarter {
    @ProcessMethod
    public static void copySearchIndex(@ParameterName("destinationUrl") @IsRequired String destinationUrl,
                                       @ParameterName("batchSize") Integer batchSize,
                                       @ParameterName("verifyAfterCopy") Boolean verifyAfterCopy) throws IOException {
        SolrIndexCopyService.copySearchIndex(destinationUrl, batchSize, verifyAfterCopy);
    }
}
