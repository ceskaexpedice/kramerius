package cz.kramerius.searchIndex.repositoryAccessImpl.krameriusNewApi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.ExtractStructureHelper;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.KrameriusRepositoryApiImpl;
import cz.incad.kramerius.resourceindex.ResourceIndexException;
import cz.kramerius.searchIndex.repositoryAccess.Utils;
import cz.kramerius.searchIndex.repositoryAccessImpl.ResourceIndexImplAbstract;
import cz.kramerius.shared.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServerException;
import org.json.JSONObject;

public class ResourceIndexImplByKrameriusNewApis extends ResourceIndexImplAbstract {
    
    public static final Logger LOGGER = Logger.getLogger(ResourceIndexImplByKrameriusNewApis.class.getName());
    
    //public KrameriusRepositoryApiImpl krameriusRepositoryApi;

    private final KrameriusRepositoryApi krameriusRepositoryApi;
    public final String coreBaseUrl;

//    public ResourceIndexImplByKrameriusNewApis(String coreBaseUrl) {
//        this.coreBaseUrl = coreBaseUrl;
//    }

    public ResourceIndexImplByKrameriusNewApis(KrameriusRepositoryApi repositoryApi, String coreBaseUrl) {
        this.coreBaseUrl = coreBaseUrl;
        this.krameriusRepositoryApi = repositoryApi;
    }

    //cache (TODO: just temporary, we don't want to break api (FedoraAccess) now)
    private boolean cachingEndabled = true;
    String pidOfCachedStructure = null;
    JsonObject cachedStructure = null;

    // ok, vrati model
    @Override
    public String getModel(String pid) throws ResourceIndexException {
        JsonObject structure = getStructure(pid);
        return structure.get("model").getAsString();
    }
    // strukturovane deti - pids o
    @Override
    public Pair<String, Set<String>> getPidsOfParents(String pid) throws ResourceIndexException {
        JsonObject structure = getStructure(pid);
        JsonObject parentsJson = structure.getAsJsonObject("parents");
        //own
        String ownParent = null;
        if (parentsJson.has("own")) {
            ownParent = parentsJson.getAsJsonObject("own").get("pid").getAsString();
        }
        //foster
        JsonArray fosterParentsJson = parentsJson.getAsJsonArray("foster");
        Set<String> fosterParents = new HashSet<>();
        Iterator<JsonElement> fosterParentsIt = fosterParentsJson.iterator();
        while (fosterParentsIt.hasNext()) {
            fosterParents.add(fosterParentsIt.next().getAsJsonObject().get("pid").getAsString());
        }
        return new Pair<>(ownParent, fosterParents);
    }

    @Override
    public Pair<List<String>, List<String>> getPidsOfChildren(String pid) throws ResourceIndexException {
        JsonObject structure = getStructure(pid);
        JsonObject childrenJson = structure.getAsJsonObject("children");
        //own
        JsonArray ownChildrenJson = childrenJson.getAsJsonArray("own");
        List<String> ownChildren = new ArrayList<>();
        Iterator<JsonElement> ownChildrenIt = ownChildrenJson.iterator();
        while (ownChildrenIt.hasNext()) {
            ownChildren.add(ownChildrenIt.next().getAsJsonObject().get("pid").getAsString());
        }
        //foster
        JsonArray fosterChildrenJson = childrenJson.getAsJsonArray("foster");
        List<String> fosterChildren = new ArrayList<>();
        Iterator<JsonElement> fosterParentsIt = fosterChildrenJson.iterator();
        while (fosterParentsIt.hasNext()) {
            fosterChildren.add(fosterParentsIt.next().getAsJsonObject().get("pid").getAsString());
        }
        return new Pair<>(ownChildren, fosterChildren);
    }

    
    
    private JsonObject getStructure(String pid) throws ResourceIndexException {
        if (cachingEndabled) {
            if (pidOfCachedStructure != null && pidOfCachedStructure.equals(pid)) {
                return cachedStructure;
            } else {
                JsonObject structure = fetchStructure(pid);
                if (structure != null) {
                    pidOfCachedStructure = pid;
                    cachedStructure = structure;
                    return structure;
                } else return null;
            }
        } else {
            return fetchStructure(pid);
        }
    }
    
    
    private JsonObject fetchStructure(String pid) throws ResourceIndexException {
        try {
            JSONObject extractStructureInfo = ExtractStructureHelper.extractStructureInfo(this.krameriusRepositoryApi, pid);
            return Utils.stringToJsonObject(extractStructureInfo.toString());
        } catch (RepositoryException  | SolrServerException | IOException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return null;
        }
    }    
    
    
    
}
