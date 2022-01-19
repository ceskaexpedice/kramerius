package cz.kramerius.searchIndex.repositoryAccessImpl.krameriusOldApi;

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
import java.util.*;

public class ResourceIndexImplByKrameriusOldApis extends ResourceIndexImplAbstract {

    public final String coreBaseUrl;

    public ResourceIndexImplByKrameriusOldApis(String coreBaseUrl) {
        this.coreBaseUrl = coreBaseUrl;
    }

    @Override
    public Pair<String, Set<String>> getPidsOfParents(String pid) throws ResourceIndexException {
        InputStream inputStream = null;
        try {
            //GET http://localhost:8080/search/api/v5.0/item/uuid:a8263737-eb03-4107-9723-7200d00036f5/parents
            URL url = new URL(coreBaseUrl + "/api/v5.0/item/" + pid + "/parents");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(3000);
            con.setReadTimeout(5000);
            //System.out.println("GET " + url.toString());
            int code = con.getResponseCode();
            if (code == 200) {
                inputStream = con.getInputStream();
                //JsonArray jsonArray = AdapterUtils.inputstreamToJsonArrayTmp(inputStream);
                JsonArray jsonArray = Utils.inputstreamToJsonArray(inputStream);
                Iterator<JsonElement> arrayIterator = jsonArray.iterator();
                List<String> parents = new ArrayList<>();
                while (arrayIterator.hasNext()) {
                    JsonObject parent = arrayIterator.next().getAsJsonObject();
                    String parentPid = parent.get("pid").getAsString();
                    parents.add(parentPid);
                }
                //split to own and foster (with an assumption that the own parent is always first)
                String ownParent = null;
                Set<String> fosterParents = new HashSet<>();
                if (parents != null && !parents.isEmpty()) {
                    ownParent = parents.get(0);
                    for (int i = 1; i < parents.size(); i++) {
                        fosterParents.add(parents.get(i));
                    }
                }
                return new Pair<>(ownParent, fosterParents);
            } else {
                throw new IOException("object " + pid + " not found or error reading it");
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
