package org.kramerius.plugin;

import cz.kramerius.searchIndex.NewIndexerProcessFromProcessingIndex;
import org.ceskaexpedice.processplatform.api.annotations.IsRequired;
import org.ceskaexpedice.processplatform.api.annotations.ParameterName;
import org.ceskaexpedice.processplatform.api.annotations.ProcessMethod;

public class SearchIndexProcessingPlatformStarter {
    @ProcessMethod
    public static void indexFromProcessingIndex(
            @ParameterName("destinationUrl") @IsRequired String destinationUrl,
            @ParameterName("batchSize") String batchSize,
            @ParameterName("ignoreInconsistentObjects") Boolean ignoreInconsistentObjects,
            @ParameterName("topModels") String topModels) {
        NewIndexerProcessFromProcessingIndex.indexMain(destinationUrl, parseBatchSize(batchSize), ignoreInconsistentObjects, topModels);
    }

    private static Integer parseBatchSize(String batchSize) {
        if (batchSize == null || batchSize.trim().isEmpty()) return null;
        try {
            return Integer.valueOf(batchSize.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("batchSize must be an integer", e);
        }
    }
}
