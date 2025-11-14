package cz.inovatika.kramerius.services.transform;

import cz.inovatika.kramerius.services.workers.batch.BatchTransformation;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceToDestTransformRegistry {

    private Map<String, BatchTransformation> registry = new HashMap<>();

    @Inject
    public SourceToDestTransformRegistry( List<BatchTransformation> transforms) {
        transforms.forEach( t -> registry.put(t.getName(), t ) );
    }


    public BatchTransformation findTransform(String transformString) {
        return registry.get( transformString );
    }
}
