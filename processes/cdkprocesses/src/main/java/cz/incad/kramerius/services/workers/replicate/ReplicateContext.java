package cz.incad.kramerius.services.workers.replicate;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rerpresents the replication context 
 */
public class ReplicateContext {
    
    // Already indexed documents:
    // retrieved information:
    //  - pid 
    // - cdk.leader
    // - composeId
    private List<Map<String,Object>> alreadyIndexed;
    
    // Not indexed documents - pids
    private List<String> notIndexed;

    public ReplicateContext(List<Map<String,Object>> alreadyIndexed, List<String> notIndexed) {
        this.alreadyIndexed = alreadyIndexed;
        this.notIndexed = notIndexed;
    }

    public List<Map<String, Object>> getAlreadyIndexed() {
        return alreadyIndexed;
    }

    public Map<String, Map<String,Object>> getAlreadyIndexedAsMap() {
        Map<String, Map<String,Object>> map = new HashMap<>();
        this.alreadyIndexed.stream().forEach(m-> {
            String pid = m.get("pid").toString();
            map.put(pid, m);
        });
        return map;
    }
    
    public List<String> getNotIndexed() {
        return notIndexed;
    }
}