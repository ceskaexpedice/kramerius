package cz.incad.kramerius.services.workers.cdk;

import cz.incad.kramerius.services.transform.K7SourceToDestTransform;
import cz.incad.kramerius.services.workers.replicate.records.ExistingConflictRecord;
import cz.incad.kramerius.utils.StringUtils;
import cz.inovatika.kramerius.services.iterators.IterationItem;
import cz.inovatika.kramerius.services.workers.WorkerContext;
import cz.inovatika.kramerius.services.workers.WorkerIndexedItem;
import cz.inovatika.kramerius.services.workers.batch.BatchTransformation;
import cz.inovatika.kramerius.services.workers.batch.impl.CopyTransformation;
import cz.inovatika.kramerius.services.workers.config.WorkerConfig;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CDKHarvestContext extends WorkerContext {

    protected WorkerConfig workerConfig;

    public CDKHarvestContext(WorkerConfig workerConfig, List<WorkerIndexedItem> alreadyIndexed, List<IterationItem> notIndexed) {
        super(alreadyIndexed, notIndexed);
        this.workerConfig = workerConfig;
    }

    private List<ExistingConflictRecord> findIndexConflict(List<Map<String,Object>> docs) {
        String childOfComposite = this.workerConfig.getRequestConfig().getChildOfComposite();

        Map<String, List<String>> pidToCompositeIds = docs.stream()
                .filter(map -> {
                    String pid = (String) map.get(getTransform(this.workerConfig).getField(childOfComposite));
                    String compositeId = (String) map.get("compositeId");
                    return StringUtils.isAnyString(pid) && StringUtils.isAnyString(compositeId);
                })
                .collect(Collectors.groupingBy(
                        map -> (String) map.get(getTransform(this.workerConfig).getField(childOfComposite)),
                        Collectors.mapping(map -> (String) map.get("compositeId"), Collectors.toList())
                ));


        return pidToCompositeIds.entrySet().stream()
                .map(entry -> new ExistingConflictRecord(entry.getKey(),
                        entry.getValue().stream().distinct().collect(Collectors.toList())))
                .filter(ExistingConflictRecord::isConflict)
                .collect(Collectors.toList());


    }

    private static BatchTransformation getTransform(WorkerConfig config) {
        String transform = config.getRequestConfig().getTransform();
        if (transform != null) {
            switch (transform.toLowerCase()) {
                case "copy": return new CopyTransformation();
                case "k7": return new K7SourceToDestTransform();
                default: return new CopyTransformation();
            }
        }
        return new CopyTransformation();
    }


}
