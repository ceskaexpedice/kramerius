package cz.incad.kramerius.services.workers.replicate;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class PidsToReplicate {

    private List<Pair<String,String>> alreadyIndexed;

    private List<String> notIndexed;

    public PidsToReplicate(List<Pair<String,String>> alreadyIndexed, List<String> notIndexed) {
        this.alreadyIndexed = alreadyIndexed;
        this.notIndexed = notIndexed;
    }

    public List<Pair<String, String>> getAlreadyIndexed() {
        return alreadyIndexed;
    }

    public List<String> getNotIndexed() {
        return notIndexed;
    }
}