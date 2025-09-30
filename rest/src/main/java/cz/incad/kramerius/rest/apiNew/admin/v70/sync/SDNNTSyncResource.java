package cz.incad.kramerius.rest.apiNew.admin.v70.sync;

import com.google.inject.Provider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import cz.incad.kramerius.processes.DefinitionManager;
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

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
 * @author happy
 * TODO: Consider; move to another package
 */
@Path("/admin/v7.0/sdnnt")
public class SDNNTSyncResource {

    public static final Logger LOGGER = Logger.getLogger(SDNNTSyncResource.class.getName());
    
    /** Enum represents basic sync operations */
    public static enum SyncActionEnum {
        
        /** The title needs to be updated by adding dnnto license  */
        add_dnnto(Arrays.asList("dnnto"), Arrays.asList("add_license")),
        
        /** The title needs to be updated by adding dnntt license */
        add_dnntt(Arrays.asList("dnntt"), Arrays.asList("add_license")),
        
        /** The title needs to be udpated by removing dnnto license */
        remove_dnnto(Arrays.asList("dnnto"), Arrays.asList("remove_license")),
        
        /** The title needs to be updated by removing dnntt license */
        remove_dnntt(Arrays.asList("dnntt"), Arrays.asList("remove_license")),
        
        /** The title needs to be udpated by changing owning license from dnnto to dnntt */
        change_dnnto_dnntt(Arrays.asList("dnntt","dnnto"), Arrays.asList("add_license", "remove_license")),

        /** The title needs to be udpated by changing owning license from dnntt to dnnto */
        change_dnntt_dnnto(Arrays.asList("dnnto","dnntt"), Arrays.asList("add_license", "remove_license"));
        // partial_change(new ArrayList<String>(), new ArrayList<String>());

        private List<String> licenses;
        private List<String> defids;

        private SyncActionEnum(List<String> licenses, List<String> defids) {
            this.licenses = licenses;
            this.defids = defids;
        }
        
        /**
         * Returns licenses
         * @return
         */
        public List<String> getLicenses() {
            return licenses;
        }
        
        /**
         * Returns process identifiers 
         * @return
         */
        public List<String> getDefids() {
            return defids;
        }
    }

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_ROWS = 15;

    @Inject
    Provider<User> userProvider;

    @Inject
    DefinitionManager definitionManager;
    
    /**
     * Basic inforamtion endpoints
     * @return
     */
    @GET
    @Path("info")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response info() {

        JSONObject infoObject = new JSONObject();
        // acronym of library; it must correspond with register
        infoObject.put("kramerius", KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.local.api", KConfiguration.getInstance().getConfiguration().getString("api.point")));
        infoObject.put("acronym", KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.acronym"));
        infoObject.put("endpoint", KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.endpoint",
                "https://sdnnt.nkp.cz/sdnnt/api/v1.0/lists/changes"));
        infoObject.put("version",
                KConfiguration.getInstance().getConfiguration().getString("sdnnt.check.version", "v7"));

        return Response.ok().entity(infoObject.toString(2)).build();
    }

    /**
     * Returns the timestamp of the last run of sychrronization process
     * @param dd
     * @return
     */
    @GET
    @Path("sync/timestamp")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response lastTimestamp(String dd) {
        try {

            String mainQuery = URLEncoder.encode("sync_actions:*", "UTF-8");
            String sort = URLEncoder.encode("fetched asc", "UTF-8");

            String sdnntHost = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");
            String url = sdnntHost
                    + String.format("/select?q=%s&wt=json&rows=%d&start=%d&sort=%s", mainQuery, 1, 0, sort);
            InputStream is = RESTHelper.inputStream(url, null, null);

            String res = IOUtils.toString(is, Charset.forName("UTF-8"));
            JSONObject responseJSON = new JSONObject(res);
            JSONObject response = responseJSON.getJSONObject("response");
            JSONArray docs = response.getJSONArray("docs");
            String fetched = null;
            for (int i = 0; i < docs.length(); i++) {
                JSONObject oneDoc = docs.getJSONObject(i);
                fetched = oneDoc.getString("fetched");
            }
            if (docs.length() >0) docs.remove(0);

            JSONObject newObject = new JSONObject();
            newObject.put("fetched", fetched);
            docs.put(newObject);

            return Response.ok().entity(response.toString(2)).build();

        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * 
     * @return
     */
    @GET
    @Path("sync/batches")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response planBatches() {
        Client client = Client.create();
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
                        numberOfBatches = numberOfBatches + ((pairs.size() % batchSize) == 0 ? 0 : 1);
                        for (int i = 0; i < numberOfBatches; i++) {
                            int from = i * batchSize;
                            int to = Math.min((i + 1) * batchSize, pairs.size());
                            List<Pair<String, String>> sublist = pairs.subList(from, to);// .stream().map(Pair::getRight).collect(Collectors.toList());

                            String batchToken = UUID.randomUUID().toString();
                            User user = this.userProvider.get();

                            List<String> pids = sublist.stream().map(p -> {
                                return p.getRight();
                            }).collect(Collectors.toList());

                            File pidlistFile = File.createTempFile(String.format("batch_%s_%d_%s",action.name(), j, defid), ".txt");
                            IOUtils.writeLines(pids,"\n", new FileOutputStream(pidlistFile), Charset.forName("UTF-8"));

                            // to file 
                            List<String> paramsList = Arrays.asList(license,
                                    "pidlist_file:"+pidlistFile.getAbsolutePath());

                            
                            String prefix = action.name().startsWith("add") ? "Přidání licence" : "Odebrání licence";
                            String name = String.format("%s '%s' pro %s", prefix, paramsList.get(0), paramsList.get(1));
                            if (name.toCharArray().length > 1024) {
                                name = name.substring(0, 1019) + "...";
                            }
                            

                            /* TODO pepo
                            LRProcess newProcess = processSchedulingHelper.scheduleProcess(defid, paramsList,
                                    user.getLoginname(), user.getLoginname(), batchToken, name);
                            ProcessInBatch batch = this.processManager
                                    .getProcessInBatchByProcessUUid(newProcess.getUUID());

                             */

                            String sdnntHost = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");
                            
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
                                // TODO pepo processId.setTextContent(batch.processId);
                                doc.appendChild(processId);
                                
                                Element processUuid = add.createElement("field");
                                processUuid.setAttribute("name", "process_uuid");
                                processUuid.setAttribute("update", "add-distinct");
                                // TODO pepo processUuid.setTextContent(batch.processUuid);
                                doc.appendChild(processUuid);
                                
                                add.getDocumentElement().appendChild(doc);
                            });
                            
                            StringWriter writer = new StringWriter();
                            XMLUtils.print(add, writer);
                            WebResource r = client.resource(sdnntHost+"/update?commitWithin=7000");
                            ClientResponse resp = r.accept(MediaType.TEXT_XML).type(MediaType.TEXT_XML).entity(writer.toString(), MediaType.TEXT_XML).post(ClientResponse.class);
                            if (resp.getStatus() != ClientResponse.Status.OK.getStatusCode()) {
                                throw new IllegalStateException("Exiting with staus:"+resp.getStatus());
                            }

                            JSONObject retobject = new JSONObject();
                            // TODO pepo retobject.put("processId", batch.processId);
                            // TODO pepo retobject.put("processUuid", batch.processUuid);
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
        } catch (JSONException | IOException /*
                                              * | ParserConfigurationException | SAXException | InterruptedException |
                                              * BrokenBarrierException
                                              */ | ParserConfigurationException | TransformerException e) {
            throw new WebApplicationException(e);
        }
    }

    public static List<Pair<String, String>> pidsFromSolr(String action)
            throws UnsupportedEncodingException, IOException {
        List<Pair<String, String>> pids = new ArrayList<>();
        String sdnntHost = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");
        String mainQuery = URLEncoder.encode(String.format("sync_actions:%s", action), "UTF-8");
        String cursor = "*";
        String nextCursor = "*";
        do {
            cursor = nextCursor;
            
            String url = sdnntHost + String.format("/select?q=%s&wt=json&sort=%s&cursorMark=%s&fl=%s", mainQuery,
                    URLEncoder.encode("id asc", "UTF-8"), cursor, URLEncoder.encode("pid id", "UTF-8"));
            InputStream is = RESTHelper.inputStream(url, null, null);
            String string = IOUtils.toString(is, Charset.forName("UTF-8"));

            JSONObject result = new JSONObject(string);
            JSONObject responseObject = result.getJSONObject("response");
            JSONArray array = responseObject.getJSONArray("docs");
            for (int i = 0; i < array.length(); i++) {
                JSONObject doc = array.getJSONObject(i);
                String pid = doc.getString("pid");
                String id = doc.getString("id");
                pids.add(Pair.of(id, pid));
            }

            nextCursor = result.getString("nextCursorMark");

        } while (!nextCursor.equals(cursor));
        return pids;
    }

    /**
     * Returns information about the last sychroniztation
     * @param spage Current page 
     * @param srows Number the rows in the page
     * @return
     */
    @GET
    @Path("sync")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response sync(@DefaultValue("0") @QueryParam("page") String spage,
            @DefaultValue("15") @QueryParam("rows") String srows) {
        try {
            
            List<String> pids = new ArrayList<>();
            Map<String, JSONObject> map = new HashMap<>();

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

            String sdnntHost = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");
            //String sdnntHost = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");

            int start = (page * rows);
            String mainQuery = URLEncoder.encode("type:main AND sync_actions:*", "UTF-8");
            String sort = URLEncoder.encode("sync_sort asc", "UTF-8");

            String url = sdnntHost
                    + String.format("/select?q=%s&wt=json&rows=%d&start=%d&sort=%s", mainQuery, rows, start, sort);
            InputStream is = RESTHelper.inputStream(url, null, null);

            String res = IOUtils.toString(is, Charset.forName("UTF-8"));
            JSONObject responseJSON = new JSONObject(res);
            JSONObject response = responseJSON.getJSONObject("response");
            JSONArray docs = response.getJSONArray("docs");
            for (int i = 0; i < docs.length(); i++) {
                JSONObject oneDoc = docs.getJSONObject(i);

                if (!oneDoc.has("real_kram_exists") && oneDoc.has("pid")) {
                    String pid = oneDoc.getString("pid");
                    pids.add(pid);
                    map.put(pid, oneDoc);
                }

                if (!pids.isEmpty()) {
                    SyncConfig config = new SyncConfig();
                    LicenseAPIFetcher apiFetcher = LicenseAPIFetcher.Versions.valueOf(config.getVersion()).build(config.getBaseUrl(), config.getVersion(), false);
                    Map<String, Map<String, Object>> checked = apiFetcher.check(new HashSet<>(pids));
                    checked.keySet().forEach(pid-> {
                        Map<String, Object> object = checked.get(pid);
                        Object model = object.get(LicenseAPIFetcher.FETCHER_MODEL_KEY);
                        Object date = object.get(LicenseAPIFetcher.FETCHER_DATE_KEY);
                        Object titles = object.get(LicenseAPIFetcher.FETCHER_TITLES_KEY);
                        if (map.containsKey(pid)) {
                            map.get(pid).put("real_kram_exists", true);
                            map.get(pid).put("real_kram_date", date);
                            map.get(pid).put("real_kram_model", model);
                            
                            if (titles != null) {
                                JSONArray titlesArray = new JSONArray();
                                ((List)titles).forEach(titlesArray::put);
                                map.get(pid).put("real_kram_titles_search", titlesArray);
                            }
                        }
                        
                    });
                }

                oneDoc.remove("_version_");
            }
            return Response.ok().entity(response.toString(2)).build();
        } catch (IOException e) {
            throw new WebApplicationException(e);
        }
    }

    /**
     * Returns granuarity; Information about structure
     * @param id
     * @return
     */
    @GET
    @Path("sync/granularity/{id}")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=utf-8" })
    public Response syncChildren(@PathParam("id") String id) {
        try {
            String sdnntHost = KConfiguration.getInstance().getConfiguration().getString("solrSdnntHost");

            String mainQuery = URLEncoder.encode(String.format("parent_id:\"%s\" AND sync_actions:*", id), "UTF-8");

            String url = sdnntHost + String.format("/select?q=%s&wt=json&rows=4000", mainQuery);
            InputStream is = RESTHelper.inputStream(url, null, null);
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

    
}
