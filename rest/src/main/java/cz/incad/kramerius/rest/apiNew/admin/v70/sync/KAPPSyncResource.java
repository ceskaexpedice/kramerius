package cz.incad.kramerius.rest.apiNew.admin.v70.sync;

import com.google.inject.Provider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.processes.utils.APIProcessScheduler;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.kapp.KAPPFetch;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Provides endpoints for KAPP synchronization snapshot.
 */
@Path("/admin/v7.0/kapp")
public class KAPPSyncResource {

    public static final Logger LOGGER = Logger.getLogger(KAPPSyncResource.class.getName());

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_ROWS = 15;

    private static final String CONFIG_SOLR_HOST = "kapp.solrHost";
    private static final int DEFAULT_BATCH_SIZE = 5000;

    private enum KappSyncActionEnum {
        add_public(Arrays.asList("public"), Arrays.asList("add_license")),
        add_onsite(Arrays.asList("onsite"), Arrays.asList("add_license")),
        remove_public(Arrays.asList("public"), Arrays.asList("remove_license")),
        remove_onsite(Arrays.asList("onsite"), Arrays.asList("remove_license")),
        change_public_onsite(Arrays.asList("onsite", "public"), Arrays.asList("add_license", "remove_license")),
        change_onsite_public(Arrays.asList("public", "onsite"), Arrays.asList("add_license", "remove_license")),
        partial_change(new ArrayList<>(), new ArrayList<>());

        private final List<String> licenses;
        private final List<String> defids;

        KappSyncActionEnum(List<String> licenses, List<String> defids) {
            this.licenses = licenses;
            this.defids = defids;
        }

        public List<String> getLicenses() {
            return licenses;
        }

        public List<String> getDefids() {
            return defids;
        }
    }

    @Inject
    Provider<User> userProvider;

    @Inject
    @javax.inject.Named("forward-client")
    private CloseableHttpClient apacheClient;

    @GET
    @Path("info")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response info() {
        JSONObject infoObject = new JSONObject();
        infoObject.put("kramerius", KConfiguration.getInstance().getConfiguration().getString(
                KAPPFetch.CONFIG_CHECK_LOCAL_API,
                KConfiguration.getInstance().getConfiguration().getString("api.point")));
        infoObject.put("acronym", KConfiguration.getInstance().getConfiguration().getString(KAPPFetch.CONFIG_ACRONYM));
        infoObject.put("endpoint", KConfiguration.getInstance().getConfiguration().getString(KAPPFetch.CONFIG_ENDPOINT));
        infoObject.put("version", KConfiguration.getInstance().getConfiguration().getString(
                KAPPFetch.CONFIG_CHECK_VERSION, KAPPFetch.DEFAULT_VERSION));

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

    @GET
    @Path("sync/actions")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response syncActions(
            @DefaultValue("0") @QueryParam("page") String spage,
            @DefaultValue("15") @QueryParam("rows") String srows,
            @QueryParam("action") String action) {
        try {
            int page = parseInt(spage, DEFAULT_PAGE);
            int rows = parseInt(srows, DEFAULT_ROWS);
            int start = page * rows;

            String query = "source:kapp AND type:main";
            if (action != null && !action.trim().isEmpty()) {
                query = query + " AND sync_actions:\"" + escapeSolr(action.trim()) + "\"";
            } else {
                query = query + " AND sync_actions:*";
            }
            String sort = URLEncoder.encode("sync_sort asc,pid asc", "UTF-8");

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
    @Path("sync/actions/children/{rootPid}")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response syncActionChildren(@PathParam("rootPid") String rootPid) {
        try {
            String query = "source:kapp AND parent_id:\"" + escapeSolr(rootPid) + "\" AND sync_actions:*";
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

    @GET
    @Path("sync/batches")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response planBatches() {
        Client client = Client.create();
        JSONArray response = new JSONArray();

        try {
            for (KappSyncActionEnum action : KappSyncActionEnum.values()) {
                List<String> defids = action.getDefids();
                if (!defids.isEmpty()) {
                    for (int j = 0; j < defids.size(); j++) {
                        String defid = defids.get(j);
                        String license = action.getLicenses().get(j);

                        List<Pair<String, String>> pairs = pidsFromSolr(action.name());
                        int numberOfBatches = pairs.size() / DEFAULT_BATCH_SIZE;
                        numberOfBatches = numberOfBatches + ((pairs.size() % DEFAULT_BATCH_SIZE) == 0 ? 0 : 1);

                        for (int i = 0; i < numberOfBatches; i++) {
                            int from = i * DEFAULT_BATCH_SIZE;
                            int to = Math.min((i + 1) * DEFAULT_BATCH_SIZE, pairs.size());
                            List<Pair<String, String>> sublist = pairs.subList(from, to);

                            String batchToken = UUID.randomUUID().toString();
                            User user = this.userProvider.get();

                            List<String> pids = sublist.stream().map(Pair::getRight).collect(Collectors.toList());

                            File pidlistFile = File.createTempFile(
                                    String.format("batch_%s_%d_%s", action.name(), j, defid), ".txt");
                            IOUtils.writeLines(pids, "\n", new FileOutputStream(pidlistFile), Charset.forName("UTF-8"));

                            List<String> paramsList = Arrays.asList(license,
                                    "pidlist_file:" + pidlistFile.getAbsolutePath());

                            String prefix = action.name().startsWith("add") ? "Přidání licence" : "Odebrání licence";
                            String name = String.format("%s '%s' pro %s", prefix, paramsList.get(0), paramsList.get(1));
                            if (name.toCharArray().length > 1024) {
                                name = name.substring(0, 1019) + "...";
                            }

                            JSONObject kappSyncPar = getKAPPSyncProcess(defid, pidlistFile, license, user.getLoginname());
                            LOGGER.info(String.format("Schedule reindexation of %s and payload %s", name,
                                    kappSyncPar.toString(2)));
                            JSONObject jsonObject = APIProcessScheduler.scheduleMainProcess(this.apacheClient, kappSyncPar);
                            String processIdVal = jsonObject.optString("processId");

                            Document add = XMLUtils.crateDocument("add");
                            sublist.stream().forEach(pair -> {
                                Element doc = add.createElement("doc");

                                Element idField = add.createElement("field");
                                idField.setAttribute("name", "id");
                                idField.setTextContent(pair.getLeft());
                                doc.appendChild(idField);

                                Element processId = add.createElement("field");
                                processId.setAttribute("name", "process_id");
                                processId.setAttribute("update", "add-distinct");
                                processId.setTextContent(processIdVal);
                                doc.appendChild(processId);

                                Element processUuid = add.createElement("field");
                                processUuid.setAttribute("name", "process_uuid");
                                processUuid.setAttribute("update", "add-distinct");
                                processUuid.setTextContent(processIdVal);

                                doc.appendChild(processUuid);

                                add.getDocumentElement().appendChild(doc);
                            });

                            StringWriter writer = new StringWriter();
                            XMLUtils.print(add, writer);
                            WebResource resource = client.resource(kappSolrHost() + "/update?commitWithin=7000");
                            ClientResponse resp = resource.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML)
                                    .entity(writer.toString(), MediaType.TEXT_XML).post(ClientResponse.class);
                            if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
                                throw new IllegalStateException("Exiting with staus:" + resp.getStatus());
                            }

                            JSONObject retobject = new JSONObject();
                            retobject.put("sync_actions", action.name());
                            retobject.put("defid", defid);
                            retobject.put("license", license);
                            retobject.put("number_of_objects", sublist.size());
                            retobject.put("batch_number", i);
                            response.put(retobject);
                        }
                    }
                }
            }

            return Response.ok().entity(response.toString(2)).build();
        } catch (JSONException | IOException | ParserConfigurationException | TransformerException e) {
            throw new WebApplicationException(e);
        }
    }

    protected JSONObject getKAPPSyncProcess(String defid, File pidlistFile, String license, String userid) {
        Map<String, String> payload = new HashMap<>();
        payload.put("pid", "pidlist_file:" + pidlistFile.getAbsolutePath());
        payload.put("license", license);
        return APIProcessScheduler.createScheduleProcess(defid, payload, userid);
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

    private static List<Pair<String, String>> pidsFromSolr(String action) throws IOException {
        List<Pair<String, String>> pids = new ArrayList<>();
        String mainQuery = URLEncoder.encode(String.format("source:kapp AND type:main AND sync_actions:\"%s\"",
                escapeSolr(action)), "UTF-8");
        String cursor = "*";
        String nextCursor = "*";
        do {
            cursor = nextCursor;

            String url = kappSolrHost() + String.format("/select?q=%s&wt=json&rows=%d&sort=%s&cursorMark=%s&fl=%s",
                    mainQuery,
                    DEFAULT_ROWS,
                    URLEncoder.encode("id asc", "UTF-8"),
                    cursor,
                    URLEncoder.encode("pid id", "UTF-8"));

            JSONObject result = solrResponse(url);
            JSONArray docs = result.getJSONObject("response").getJSONArray("docs");
            for (int i = 0; i < docs.length(); i++) {
                JSONObject doc = docs.getJSONObject(i);
                String pid = doc.optString("pid", null);
                String id = doc.optString("id", null);
                if (pid != null && !pid.trim().isEmpty() && id != null && !id.trim().isEmpty()) {
                    pids.add(Pair.of(id, pid));
                }
            }

            nextCursor = result.getString("nextCursorMark");
        } while (!nextCursor.equals(cursor));

        return pids;
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
