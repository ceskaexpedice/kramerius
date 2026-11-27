package org.kramerius.plugin;

import cz.inovatika.kramerius.services.SolrIndexCopyService;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

import java.io.IOException;

public class SolrIndexCopyStarter {

    @ProcessMethod
    public static void copyIndex(
            @ParameterName("sourceUrl") @IsRequired String sourceUrl,
            @ParameterName("destinationUrl") @IsRequired String destinationUrl,
            @ParameterName("identifier") String identifier,
            @ParameterName("fieldList") String fieldList,
            @ParameterName("filterQuery") String filterQuery,
            @ParameterName("batchSize") Integer batchSize,
            @ParameterName("verifyAfterCopy") Boolean verifyAfterCopy) throws IOException {
        SolrIndexCopyService.copyIndex(sourceUrl, destinationUrl, identifier, fieldList, filterQuery, batchSize, verifyAfterCopy);
    }
}
