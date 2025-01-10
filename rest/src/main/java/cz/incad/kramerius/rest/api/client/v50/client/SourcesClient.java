package cz.incad.kramerius.rest.api.client.v50.client;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class SourcesClient {

    /**
     * Informace o zrojich 
     */
    public static String sources() {
        Client c = Client.create();
        WebResource r = c
                .resource("http://cdk-test.lib.cas.cz/search/api/v5.0/sources");
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }
    
    public static String source(String src) {
        Client c = Client.create();
        WebResource r = c
                .resource("http://cdk-test.lib.cas.cz/search/api/v5.0/sources/"+src);
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }
    

    public static void main(String[] args) throws JSONException {
        String srcs = sources();
        JSONArray jsonArr = new JSONArray(srcs);
        for (int i = 0,ll=jsonArr.length(); i < ll; i++) {
            JSONObject jsonO = jsonArr.getJSONObject(i);
            String pid = jsonO.getString("pid");
            String url = jsonO.getString("url");
            System.out.println(""+pid +" - "+url);
        }
    }
}
