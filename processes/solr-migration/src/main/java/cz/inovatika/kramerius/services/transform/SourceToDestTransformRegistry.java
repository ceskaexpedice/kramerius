package cz.inovatika.kramerius.services.transform;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SourceToDestTransformRegistry {

    private Map<String, SourceToDestTransform> registry = new HashMap<>();

    @Inject
    public SourceToDestTransformRegistry( List<SourceToDestTransform> transforms) {
        transforms.forEach( t -> registry.put(t.getName(), t ) );
    }


    public SourceToDestTransform findTransform(String transformString) {
        return registry.get( transformString );
    }
}
