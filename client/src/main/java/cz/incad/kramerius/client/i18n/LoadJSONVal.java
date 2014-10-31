package cz.incad.kramerius.client.i18n;

import javax.ws.rs.core.MediaType;

import org.json.JSONException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class LoadJSONVal {

    static String getJSONVal(String url) throws JSONException {
        Client c = Client.create();
        WebResource r = c.resource(url);
        Builder builder = r.accept(MediaType.APPLICATION_JSON);
        return builder.get(String.class);
    }

}
