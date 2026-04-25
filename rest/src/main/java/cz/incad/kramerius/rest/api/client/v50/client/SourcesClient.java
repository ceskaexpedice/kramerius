package cz.incad.kramerius.rest.api.client.v50.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Informace o zdrojich
 */
public class SourcesClient {

    private static final String BASE_URL = "http://cdk-test.lib.cas.cz/search/api/v5.0/sources";

    /**
     * Seznam vsech zdroju
     */
    public static String sources() {
        try (Client client = ClientBuilder.newBuilder().build()) {
            WebTarget target = client.target(BASE_URL);
            try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
                return response.readEntity(String.class);
            }
        }
    }

    /**
     * Jednotlivy zdroj podle ID
     */
    public static String source(String src) {
        try (Client client = ClientBuilder.newBuilder().build()) {
            WebTarget target = client.target(BASE_URL + "/" + src);
            try (Response response = target.request(MediaType.APPLICATION_JSON).get()) {
                return response.readEntity(String.class);
            }
        }
    }

    public static void main(String[] args) throws JSONException {
        String srcs = sources();
        JSONArray jsonArr = new JSONArray(srcs);
        for (int i = 0, ll = jsonArr.length(); i < ll; i++) {
            JSONObject jsonO = jsonArr.getJSONObject(i);
            String pid = jsonO.getString("pid");
            String url = jsonO.getString("url");
            System.out.println(pid + " - " + url);
        }

        // Example of getting a single source
        if (jsonArr.length() > 0) {
            String firstPid = jsonArr.getJSONObject(0).getString("pid");
            String singleSource = source(firstPid);
            System.out.println("Single source: " + singleSource);
        }
    }
}