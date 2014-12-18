package cz.incad.kramerius.client.utils;

import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import cz.incad.kramerius.client.tools.BasicAuthenticationFilter;

public class ApiCallsHelp {


    public static String getXML(String url, String userName, String pswd)
            throws JSONException {
        Client c = Client.create();
        WebResource r = c.resource(url);
        if (userName != null && pswd != null) {
            r.addFilter(new BasicAuthenticationFilter(userName, pswd));
        }
        Builder builder = r.accept(MediaType.APPLICATION_XML);
        return builder.get(String.class);
    }

    public static String getJSON(String url)
            throws JSONException {
        Client c = Client.create();
        WebResource r = c.resource(url);
        Builder builder = r.accept(MediaType.APPLICATION_JSON);
        return builder.get(String.class);
    }
    
    public static String getJSON(String url, String userName, String pswd)
            throws JSONException {
        Client c = Client.create();
        WebResource r = c.resource(url);
        if (userName != null && pswd != null) {
            r.addFilter(new BasicAuthenticationFilter(userName, pswd));
        }
        Builder builder = r.accept(MediaType.APPLICATION_JSON);
        return builder.get(String.class);
    }

    public static String postJSON(String url, JSONObject object, String userName,
            String pswd) {
        Client c = Client.create();
        WebResource r = c.resource(url);
        r.addFilter(new BasicAuthenticationFilter(userName, pswd));
        String t = r.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .entity(object.toString(), MediaType.APPLICATION_JSON)
                .post(String.class);
        return t;
    }

}
