package cz.incad.kramerius.services.workers.replicate;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * Represents object which contains information what should be replicated
 */
public class ReplicateContext {

    private List<Map<String,String>> alreadyIndexed;

    private List<String> notIndexed;

    public ReplicateContext(List<Map<String,String>> alreadyIndexed, List<String> notIndexed) {
        this.alreadyIndexed = alreadyIndexed;
        this.notIndexed = notIndexed;
    }

    public List<Map<String, String>> getAlreadyIndexed() {
        return alreadyIndexed;
    }

    public List<String> getNotIndexed() {
        return notIndexed;
    }
}