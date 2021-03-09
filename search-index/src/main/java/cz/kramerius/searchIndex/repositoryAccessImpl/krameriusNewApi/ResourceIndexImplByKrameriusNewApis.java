package cz.kramerius.searchIndex.repositoryAccessImpl.krameriusNewApi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cz.incad.kramerius.resourceindex.ResourceIndexException;
import cz.kramerius.searchIndex.repositoryAccess.Utils;
import cz.kramerius.searchIndex.repositoryAccessImpl.ResourceIndexImplAbstract;
import cz.kramerius.shared.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ResourceIndexImplByKrameriusNewApis extends ResourceIndexImplAbstract {

    public final String coreBaseUrl;

    public ResourceIndexImplByKrameriusNewApis(String coreBaseUrl) {
        this.coreBaseUrl = coreBaseUrl;
    }

    //cache (TODO: just temporary, we don't want to break api (FedoraAccess) now)
    private boolean cachingEndabled = true;
    String pidOfCachedStructure = null;
    JsonObject cachedStructure = null;

    @Override
    public String getModel(String pid) throws ResourceIndexException {
        JsonObject structure = getStructure(pid);
        return structure.get("model").getAsString();
    }

    @Override
    public Pair<String, List<String>> getPidsOfParents(String pid) throws ResourceIndexException {
        JsonObject structure = getStructure(pid);
        JsonObject parentsJson = structure.getAsJsonObject("parents");
        //own
        String ownParent = null;
        if (parentsJson.has("own")) {
            ownParent = parentsJson.getAsJsonObject("own").get("pid").getAsString();
        }
        //foster
        JsonArray fosterParentsJson = parentsJson.getAsJsonArray("foster");
        List<String> fosterParents = new ArrayList<>();
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
                pidOfCachedStructure = pid;
                cachedStructure = structure;
                return structure;
            }
        } else {
            return fetchStructure(pid);
        }
    }

    private JsonObject fetchStructure(String pid) throws ResourceIndexException {
        InputStream inputStream = null;
        try {
            //GET http://localhost:8080/search/api/client/v6.0/items/uuid:db886a43-93cd-48a1-86db-a96c5b15b2b2/info/structure
            URL url = new URL(coreBaseUrl + "/api/client/v6.0/items/" + pid + "/info/structure");
            //System.out.println(url);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(10000);
            con.setReadTimeout(15000);
            //System.out.println("GET " + url.toString());
            int code = con.getResponseCode();
            if (code == 200) {
                inputStream = con.getInputStream();
                JsonObject structure = Utils.inputstreamToJsonObject(inputStream);
                return structure;
            } else {
                String errorMessage = Utils.inputstreamToString(con.getErrorStream());
                throw new IOException("object " + pid + " not found or error reading it: " + errorMessage);
            }
        } catch (IOException e) {
            throw new ResourceIndexException(e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new ResourceIndexException(e);
                }
            }
        }
    }
}
