package cz.cas.lib.knav.indexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.processes.utils.ProcessUtils;
import cz.incad.kramerius.service.impl.IndexerProcessStarter.TokensFilter;

/**
 * Collecting pids for indexing
 * @author pavels
 *
 */
public class CollectPidForIndexing {

    public static final int MAXIMUM_DOCUMENTS = 90;

    
    private List<String> collectedPids = new ArrayList<String>();
    private boolean inUse = false;
    
    public void enqueuePid(String pid) {
        collectedPids.add(pid);
        if (this.reachCapacity()) {
            planIndexerWithCollectedPids();
        }
        this.inUse = true;
    }
    
    boolean reachCapacity() {
        return this.collectedPids.size() == MAXIMUM_DOCUMENTS;
    }
    
    void clearList() {
        this.collectedPids.clear();
    }
    
    void planIndexerWithCollectedPids() {
        String[] batchIndexer = new String[this.collectedPids.size()+1];
        batchIndexer[0]="fromPid";
        System.arraycopy(this.collectedPids.toArray(new String[this.collectedPids.size()]), 0, batchIndexer, 1, this.collectedPids.size());
        planBatchIndexer(batchIndexer);
        this.clearList();
    }

    public static String planBatchIndexer(String...args) {
        Client c = Client.create();
        WebResource r = c.resource(ProcessUtils.getApiPoint()+"?def=batchindexer");
        r.addFilter(new TokensFilter());
   
        JSONObject object = new JSONObject();
        object.put("parameters", JSONArray.fromObject(Arrays.asList(args)));
   
        String t = r.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).entity(object.toString(), MediaType.APPLICATION_JSON)
                .post(String.class);
        return t;
    }
    
    
    public boolean hasBeenTouched() {
        return inUse;
    }

    public void close(){
        if (this.collectedPids.size() > 0) {
            this.planIndexerWithCollectedPids();
        }
    }
    
}
