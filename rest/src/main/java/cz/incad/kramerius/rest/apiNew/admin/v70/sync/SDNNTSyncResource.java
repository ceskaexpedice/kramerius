package cz.incad.kramerius.rest.apiNew.admin.v70.sync;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.security.licenses.LicensesManagerException;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;

@Path("/admin/v7.0/sdnnt")
public class SDNNTSyncResource {
    
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_ROWS = 15;

    @GET
    @Path("info")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response info() {
        
        JSONObject infoObject = new JSONObject();
        infoObject.put("acronym", KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.acronym"));
        infoObject.put("endpoint", KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.endpoint","https://sdnnt.nkp.cz/sdnnt/api/v1.0/lists/changes"));
        infoObject.put("version", KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.version","v7"));
        
        return Response.ok().entity(infoObject.toString(2)).build();
        
    }

    
    @GET
    @Path("sync/timestamp")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response lastTimestamp(String dd) {
        try {

            String mainQuery = URLEncoder.encode("type:main AND sync_actions:*","UTF-8");
            String sort = URLEncoder.encode("fetched asc","UTF-8");
            
            String sdnntHost  = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");
            String url = sdnntHost+String.format("/select?q=%s&wt=json&rows=%d&start=%d&sort=%s", mainQuery, 1, 0, sort);
            InputStream is = RESTHelper.inputStream(url, null,null);
            
            String res = IOUtils.toString(is, Charset.forName("UTF-8"));
            JSONObject responseJSON = new JSONObject(res);
            JSONObject response = responseJSON.getJSONObject("response");
            JSONArray docs = response.getJSONArray("docs");
            String fetched = null;
            for (int i = 0; i < docs.length(); i++) {
                JSONObject oneDoc = docs.getJSONObject(i);
                fetched = oneDoc.getString("fetched");
            }
            docs.remove(0);
            
            JSONObject newObject = new JSONObject();
            newObject.put("fetched", fetched);
            docs.put(newObject);
            
            return Response.ok().entity(response.toString(2)).build();

        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("sync")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response sync(@DefaultValue("0") @QueryParam("page") String spage,  @DefaultValue("15") @QueryParam("rows") String srows) {
        try {
            int page = DEFAULT_PAGE;
            int rows = DEFAULT_ROWS;
            try {
                page = Integer.parseInt(spage);
            } catch (NumberFormatException e) {
            }
            try {
                rows = Integer.parseInt(srows);
            } catch (NumberFormatException e) {
            }
            
            String sdnntHost  = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");

            int start = (page * rows)+rows;
            String mainQuery = URLEncoder.encode("type:main AND sync_actions:*","UTF-8");
            String sort = URLEncoder.encode("sync_sort asc","UTF-8");

            String url = sdnntHost+String.format("/select?q=%s&wt=json&rows=%d&start=%d&sort=%s", mainQuery, rows, start, sort);
            InputStream is = RESTHelper.inputStream(url, null,null);
            
            String res = IOUtils.toString(is, Charset.forName("UTF-8"));
            JSONObject responseJSON = new JSONObject(res);
            JSONObject response = responseJSON.getJSONObject("response");
            JSONArray docs = response.getJSONArray("docs");
            for (int i = 0; i < docs.length(); i++) {
                JSONObject oneDoc = docs.getJSONObject(i);
                oneDoc.remove("_version_");
            }
            return Response.ok().entity(response.toString(2)).build();
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("sync/granularity/{id}")
    @Produces({MediaType.APPLICATION_JSON+";charset=utf-8"})
    public Response syncChildren(@PathParam("id") String id) {
        try {
            String sdnntHost  = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");

            String mainQuery = URLEncoder.encode(String.format("parent_id:\"%s\" AND sync_actions:*",id),"UTF-8");

            String url = sdnntHost+String.format("/select?q=%s&wt=json&rows=4000", mainQuery);
            InputStream is = RESTHelper.inputStream(url, null,null);
            String res = IOUtils.toString(is, Charset.forName("UTF-8"));
            JSONObject responseJSON = new JSONObject(res);
            JSONArray docs = responseJSON.getJSONObject("response").getJSONArray("docs");

            for (int i = 0; i < docs.length(); i++) {
                JSONObject oneDoc = docs.getJSONObject(i);
                oneDoc.remove("_version_");
            }
            
            JSONObject retObject = new JSONObject();
            retObject.put(id, docs);
            
            return Response.ok().entity(retObject.toString(2)).build();
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }
    
//    public static void main(String[] args) throws IOException {
//        String id = "oai:aleph-nkp.cz:SKC01-000092568_2";
//        String sdnntHost  = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");
//
//        String mainQuery = URLEncoder.encode(String.format("parent_id:\"%s\" AND sync_actions:*",id),"UTF-8");
//
//        String url = sdnntHost+String.format("/select?q=%s&wt=json&rows=4000", mainQuery);
//        InputStream is = RESTHelper.inputStream(url, null,null);
//        
//        String string = IOUtils.toString(is, "UTF-8");
//        System.out.println(string);
//        
//    }
}
