package cz.incad.kramerius.rest.apiNew.admin.v70.sync;

import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * Provides endpoints for KAPP synchronization snapshot.
 */
@Path("/admin/v7.0/kapp")
public class KAPPSyncResource {

    public static final Logger LOGGER = Logger.getLogger(KAPPSyncResource.class.getName());

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_ROWS = 15;

    private static final String CONFIG_ENDPOINT = "kapp.endpoint";
    private static final String CONFIG_SOLR_HOST = "kapp.solrHost";
    private static final String CONFIG_ROWS = "kapp.fetch.rows";

    @GET
    @Path("info")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response info() {
        JSONObject infoObject = new JSONObject();
        infoObject.put("endpoint", KConfiguration.getInstance().getConfiguration().getString(CONFIG_ENDPOINT));
        infoObject.put("solrHost", KConfiguration.getInstance().getConfiguration().getString(CONFIG_SOLR_HOST));
        infoObject.put("rows", KConfiguration.getInstance().getConfiguration().getInt(CONFIG_ROWS, 1000));

        return Response.ok().entity(infoObject.toString(2)).build();
    }

    @GET
    @Path("sync/timestamp")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response lastTimestamp() {
        try {
            String query = URLEncoder.encode("source:kapp", "UTF-8");
            String sort = URLEncoder.encode("fetched desc", "UTF-8");
            String fieldList = URLEncoder.encode("fetched", "UTF-8");

            String url = kappSolrHost()
                    + String.format("/select?q=%s&wt=json&rows=1&start=0&sort=%s&fl=%s", query, sort, fieldList);

            JSONObject response = solrResponse(url);
            JSONArray docs = response.getJSONObject("response").getJSONArray("docs");
            String fetched = docs.length() > 0 ? docs.getJSONObject(0).optString("fetched", null) : null;

            JSONObject result = new JSONObject();
            result.put("fetched", fetched);
            return Response.ok().entity(result.toString(2)).build();
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("sync")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response sync(
            @DefaultValue("0") @QueryParam("page") String spage,
            @DefaultValue("15") @QueryParam("rows") String srows,
            @QueryParam("model") String model,
            @QueryParam("state") String state,
            @QueryParam("license") String license,
            @QueryParam("digital_library") String digitalLibrary) {
        try {
            int page = parseInt(spage, DEFAULT_PAGE);
            int rows = parseInt(srows, DEFAULT_ROWS);
            int start = page * rows;

            String query = buildQuery(model, state, license, digitalLibrary, null);
            String sort = URLEncoder.encode("model asc,pid asc", "UTF-8");

            String url = kappSolrHost()
                    + String.format("/select?q=%s&wt=json&rows=%d&start=%d&sort=%s",
                    URLEncoder.encode(query, "UTF-8"), rows, start, sort);

            JSONObject response = solrResponse(url);
            removeVersion(response);
            return Response.ok().entity(response.getJSONObject("response").toString(2)).build();
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("sync/children/{rootPid}")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response syncChildren(@PathParam("rootPid") String rootPid) {
        try {
            String query = buildQuery(null, null, null, null, rootPid) + " AND -pid:\"" + escapeSolr(rootPid) + "\"";
            String sort = URLEncoder.encode("date_issued_year asc,pid asc", "UTF-8");

            String url = kappSolrHost()
                    + String.format("/select?q=%s&wt=json&rows=4000&sort=%s",
                    URLEncoder.encode(query, "UTF-8"), sort);

            JSONObject response = solrResponse(url);
            removeVersion(response);

            JSONObject result = new JSONObject();
            result.put(rootPid, response.getJSONObject("response").getJSONArray("docs"));
            return Response.ok().entity(result.toString(2)).build();
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    private static String buildQuery(String model, String state, String license, String digitalLibrary, String rootPid) {
        StringBuilder builder = new StringBuilder("source:kapp");
        appendExactQuery(builder, "model", model);
        appendExactQuery(builder, "state", state);
        appendExactQuery(builder, "assigned_licenses", license);
        appendExactQuery(builder, "digital_library", digitalLibrary);
        appendExactQuery(builder, "root_pid", rootPid);
        return builder.toString();
    }

    private static void appendExactQuery(StringBuilder builder, String field, String value) {
        if (value != null && !value.trim().isEmpty()) {
            builder.append(" AND ").append(field).append(":\"").append(escapeSolr(value.trim())).append("\"");
        }
    }

    private static String escapeSolr(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static String kappSolrHost() {
        String solrHost = KConfiguration.getInstance().getConfiguration().getString(CONFIG_SOLR_HOST);
        if (solrHost == null) {
            throw new IllegalStateException("Missing configuration key '" + CONFIG_SOLR_HOST + "'");
        }
        return solrHost;
    }

    private static JSONObject solrResponse(String url) throws IOException {
        InputStream is = RESTHelper.inputStream(url, null, null);
        String response = IOUtils.toString(is, Charset.forName("UTF-8"));
        return new JSONObject(response);
    }

    private static void removeVersion(JSONObject response) {
        JSONArray docs = response.getJSONObject("response").getJSONArray("docs");
        for (int i = 0; i < docs.length(); i++) {
            docs.getJSONObject(i).remove("_version_");
        }
    }
}
