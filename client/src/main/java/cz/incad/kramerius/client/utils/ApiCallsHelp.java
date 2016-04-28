package cz.incad.kramerius.client.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

import cz.incad.kramerius.utils.jersey.BasicAuthenticationFilter;

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

    public static String postParams(String url, String userName,
            String pswd, String... params) throws UnsupportedEncodingException {
        String postfix = "";
        for (int i = 0; i < params.length; i++) {
            if (i > 0) {
                postfix += "&";
            }
            String[] dpars = params[i].split("=");
            if (dpars.length > 0) {
                String val = "";
                for (int j = 1; j < dpars.length; j++) {
                    val += URLEncoder.encode(dpars[1],"UTF-8");
                }
                postfix += dpars[0]+"="+val;
            }  else {
                postfix += URLEncoder.encode(params[i],"UTF-8");
            }
        }

        if (postfix.length() > 0) {
            url += "?"+postfix;
        }
        
        Client c = Client.create();
        WebResource r = c.resource(url);
        r.addFilter(new BasicAuthenticationFilter(userName, pswd));
        String t = r.accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(String.class);
        return t;
        
    }
}
