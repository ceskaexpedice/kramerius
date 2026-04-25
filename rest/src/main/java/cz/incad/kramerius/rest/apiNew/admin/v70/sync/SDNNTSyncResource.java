package cz.incad.kramerius.rest.apiNew.admin.v70.sync;

import com.google.inject.Provider;
import cz.incad.kramerius.processes.definition.ProcessDefinitionManager;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.inovatika.sdnnt.LicenseAPIFetcher;
import cz.inovatika.sdnnt.SyncConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Provides endpoints for synchronization between SDNNT and local kramerius
 */
@Path("/admin/v7.0/sdnnt")
public class SDNNTSyncResource {

    public static final Logger LOGGER = Logger.getLogger(SDNNTSyncResource.class.getName());

    public static enum SyncActionEnum {
        add_dnnto(Arrays.asList("dnnto"), Arrays.asList("add_license")),
        add_dnntt(Arrays.asList("dnntt"), Arrays.asList("add_license")),
        remove_dnnto(Arrays.asList("dnnto"), Arrays.asList("remove_license")),
        remove_dnntt(Arrays.asList("dnntt"), Arrays.asList("remove_license")),
        change_dnnto_dnntt(Arrays.asList("dnntt","dnnto"), Arrays.asList("add_license", "remove_license")),
        change_dnntt_dnnto(Arrays.asList("dnnto","dnntt"), Arrays.asList("add_license", "remove_license"));

        private List<String> licenses;
        private List<String> defids;

        private SyncActionEnum(List<String> licenses, List<String> defids) {
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

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_ROWS = 15;

    @Inject
    Provider<User> userProvider;

    @Inject
    ProcessDefinitionManager definitionManager;

    @GET
    @Path("info")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response info() {
        JSONObject infoObject = new JSONObject();
        infoObject.put("kramerius", KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.local.api", KConfiguration.getInstance().getConfiguration().getString("api.point")));
        infoObject.put("acronym", KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.acronym"));
        infoObject.put("endpoint", KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.endpoint", "https://sdnnt.nkp.cz/sdnnt/api/v1.0/lists/changes"));
        infoObject.put("version", KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.version", "v7"));
        return Response.ok(infoObject.toString(2)).build();
    }

    @GET
    @Path("sync/timestamp")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response lastTimestamp(String dd) {
        try {
            String mainQuery = URLEncoder.encode("sync_actions:*", "UTF-8");
            String sort = URLEncoder.encode("fetched asc", "UTF-8");
            String sdnntHost = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");
            String url = sdnntHost + String.format("/select?q=%s&wt=json&rows=%d&start=%d&sort=%s", mainQuery, 1, 0, sort);
            InputStream is = RESTHelper.inputStream(url, null, null);

            String res = IOUtils.toString(is, Charset.forName("UTF-8"));
            JSONObject responseJSON = new JSONObject(res);
            JSONObject response = responseJSON.getJSONObject("response");
            JSONArray docs = response.getJSONArray("docs");
            String fetched = null;
            if (docs.length() > 0) {
                JSONObject oneDoc = docs.getJSONObject(0);
                fetched = oneDoc.getString("fetched");
                docs.remove(0);
            }
            JSONObject newObject = new JSONObject();
            newObject.put("fetched", fetched);
            docs.put(newObject);

            return Response.ok(response.toString(2)).build();

        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("sync/batches")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response planBatches() {
        Client client = ClientBuilder.newClient();
        JSONArray response = new JSONArray();

        try {
            SyncActionEnum[] values = SyncActionEnum.values();
            for (SyncActionEnum action : values) {
                List<String> defids = action.getDefids();
                if (!defids.isEmpty()) {
                    for (int j = 0; j < defids.size(); j++) {
                        String defid = defids.get(j);
                        String license = action.getLicenses().get(j);
                        int batchSize = 5000;

                        List<Pair<String, String>> pairs = pidsFromSolr(action.name());
                        int numberOfBatches = pairs.size() / batchSize;
                        numberOfBatches += (pairs.size() % batchSize == 0 ? 0 : 1);

                        for (int i = 0; i < numberOfBatches; i++) {
                            int from = i * batchSize;
                            int to = Math.min((i + 1) * batchSize, pairs.size());
                            List<Pair<String, String>> sublist = pairs.subList(from, to);

                            String batchToken = UUID.randomUUID().toString();
                            User user = this.userProvider.get();

                            List<String> pids = sublist.stream().map(Pair::getRight).collect(Collectors.toList());

                            File pidlistFile = File.createTempFile(String.format("batch_%s_%d_%s", action.name(), j, defid), ".txt");
                            IOUtils.writeLines(pids, "\n", new FileOutputStream(pidlistFile), Charset.forName("UTF-8"));

                            List<String> paramsList = Arrays.asList(license, "pidlist_file:" + pidlistFile.getAbsolutePath());

                            String prefix = action.name().startsWith("add") ? "Přidání licence" : "Odebrání licence";
                            String name = String.format("%s '%s' pro %s", prefix, paramsList.get(0), paramsList.get(1));
                            if (name.length() > 1024) {
                                name = name.substring(0, 1019) + "...";
                            }

                            // Build XML and send using Jersey 3 client
                            Document add = XMLUtils.crateDocument("add");
                            sublist.forEach(pair -> {
                                Element doc = add.createElement("doc");

                                Element idField = add.createElement("field");
                                idField.setAttribute("name", "id");
                                idField.setTextContent(pair.getLeft());
                                doc.appendChild(idField);

                                Element processId = add.createElement("field");
                                processId.setAttribute("name", "process_id");
                                processId.setAttribute("update", "add-distinct");
                                doc.appendChild(processId);

                                Element processUuid = add.createElement("field");
                                processUuid.setAttribute("name", "process_uuid");
                                processUuid.setAttribute("update", "add-distinct");
                                doc.appendChild(processUuid);

                                add.getDocumentElement().appendChild(doc);
                            });

                            StringWriter writer = new StringWriter();
                            XMLUtils.print(add, writer);
                            String sdnntHost = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");
                            WebTarget target = client.target(sdnntHost + "/update?commitWithin=7000");
                            Invocation.Builder invocationBuilder = target.request(MediaType.TEXT_XML);
                            Response resp = invocationBuilder.post(Entity.entity(writer.toString(), MediaType.TEXT_XML));

                            if (resp.getStatus() != Response.Status.OK.getStatusCode()) {
                                throw new IllegalStateException("Exiting with status:" + resp.getStatus());
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

            return Response.ok(response.toString(2)).build();
        } catch (JSONException | IOException | ParserConfigurationException | TransformerException e) {
            throw new WebApplicationException(e);
        } finally {
            client.close();
        }
    }

    public static List<Pair<String, String>> pidsFromSolr(String action) throws IOException {
        List<Pair<String, String>> pids = new ArrayList<>();
        String sdnntHost = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");
        String mainQuery = URLEncoder.encode(String.format("sync_actions:%s", action), "UTF-8");
        String cursor = "*";
        String nextCursor;

        do {
            nextCursor = cursor;
            String url = sdnntHost + String.format("/select?q=%s&wt=json&sort=%s&cursorMark=%s&fl=%s",
                    mainQuery, URLEncoder.encode("id asc", "UTF-8"), cursor, URLEncoder.encode("pid id", "UTF-8"));
            InputStream is = RESTHelper.inputStream(url, null, null);
            String string = IOUtils.toString(is, Charset.forName("UTF-8"));

            JSONObject result = new JSONObject(string);
            JSONObject responseObject = result.getJSONObject("response");
            JSONArray array = responseObject.getJSONArray("docs");

            for (int i = 0; i < array.length(); i++) {
                JSONObject doc = array.getJSONObject(i);
                pids.add(Pair.of(doc.getString("id"), doc.getString("pid")));
            }

            cursor = result.getString("nextCursorMark");
        } while (!cursor.equals(nextCursor));

        return pids;
    }
}