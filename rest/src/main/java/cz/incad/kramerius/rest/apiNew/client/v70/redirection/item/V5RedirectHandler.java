package cz.incad.kramerius.rest.apiNew.client.v70.redirection.item;

import com.sun.jersey.api.client.*;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import org.apache.commons.lang3.tuple.Pair;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.core.repository.*;
import org.ceskaexpedice.akubra.relsext.FosterRelationsMapping;
import org.ceskaexpedice.akubra.relsext.KnownRelations;
import org.ceskaexpedice.akubra.relsext.OwnRelationsMapping;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.inovatika.cdk.cache.CDKRequestCacheSupport;
import cz.inovatika.monitoring.ApiCallEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.KrameriusRepositoryApi.FosterRelationsMapping;
import cz.incad.kramerius.repository.KrameriusRepositoryApi.KnownRelations;
import cz.incad.kramerius.repository.KrameriusRepositoryApi.OwnRelationsMapping;
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestManager;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.ProxyHandlerException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;

public class V5RedirectHandler extends ProxyItemHandler {

    public static final Logger LOGGER = Logger.getLogger(V5RedirectHandler.class.getName());


    public V5RedirectHandler(CDKRequestCacheSupport cacheSupport,
                             ReharvestManager reharvestManager,
                             Instances instances,
                             User user,
                             CloseableHttpClient closeableHttpClient,
                             DeleteTriggerSupport deleteTriggerSupport,
                             SolrAccess solrAccess,
                             String source,
                             String pid,
                             String remoteAddr) {
        super(cacheSupport,
                reharvestManager,
                instances,
                user,
                closeableHttpClient,
                deleteTriggerSupport,
                solrAccess,
                source,
                pid,
                remoteAddr);
    }

    @Override
    public Response image(RequestMethodName method, ApiCallEvent callEvent) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/IMG_FULL";
        return buildRedirectResponse(url);
    }

    @Override
    public Response imagePreview(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid
                + "/streams/IMG_PREVIEW";
        return buildRedirectResponse(url);
    }

    @Override
    public Response textOCR(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/TEXT_OCR";
        return buildRedirectResponse(url);
    }

    @Override
    public Response altoOCR(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/ALTO";
        return buildRedirectResponse(url);
    }

    @Override
    public Response zoomifyImageProperties(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "zoomify/" + this.pid + "/ImageProperties.xml";
        return buildRedirectResponse(url);
    }

    @Override
    public Response mods(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid
                + "/streams/BIBLIO_MODS";
        return buildRedirectResponse(url);
    }

    @Override
    public Response dc(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/DC";
        return buildRedirectResponse(url);
    }


    @Override
    public InputStream directStreamDC(ApiCallEvent event) throws ProxyHandlerException {
        return directStream("DC");
    }
    
    
    
    @Override
    public InputStream directStreamBiblioMods(ApiCallEvent event) throws ProxyHandlerException {
        return directStream("BIBLIO_MODS");
    }
    

    private InputStream directStream(String stream) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/"+stream;
        return inputStream(url);

    }

    private boolean isStreamAvailable(String stream) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/"+stream;
        return exists(url);
    }
    
    @Override
    public boolean isStreamDCAvaiable(ApiCallEvent event) throws ProxyHandlerException {
        return isStreamAvailable("DC");
    }

    @Override
    public boolean isStreamBiblioModsAvaiable(ApiCallEvent event) throws ProxyHandlerException {
        return isStreamAvailable("BIBLIO_MODS");
    }

    @Override
    public Response zoomifyTile(String tileGroupStr, String tileStr, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String formatted = String.format("zoomify/%s/%s/%s", this.pid, tileGroupStr, tileStr);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + formatted;
        return buildRedirectResponse(url);
    }

    
    
    @Override
    public Response iiifInfo(RequestMethodName method, String pid, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String formatted = String.format("iiif/%s/info.json", this.pid );
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + formatted;
        return buildRedirectResponse(url);
    }

    @Override
    public Response iiifTile(RequestMethodName method, String pid, String region, String size, String rotation, String qf, ApiCallEvent event)
            throws ProxyHandlerException {
        String baseurl = baseUrl();
        String postfix = String.format("%s/%s/%s/%s", region, size, rotation, qf);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + postfix;
        return buildRedirectResponse(url);
    }

    @Override
    public Response imageThumb(RequestMethodName method, ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        boolean streamsThumb = KConfiguration.getInstance().getConfiguration()
                .getBoolean("cdk.collections.sources." + this.source + ".thumb_streams", false);
        if (streamsThumb) {
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/IMG_THUMB";
            return buildRedirectResponse(url);
            
        } else {
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/thumb";
            return buildRedirectResponse(url);
        }
    }

    @Override
    public Response infoData(ApiCallEvent event) throws ProxyHandlerException {
        try {
            //TODO: Cache
            JSONObject streams = new JSONObject(retrieveStreams(event));
            JSONObject dataInfo = extractAvailableDataInfo(streams);
            // TODO: providedByLicense - pid, source, user
            // TODO: Store datainfo -> pid, source, user ??
            return Response.ok(dataInfo).build();
        } catch (JSONException | LexerException e) {
            throw new ProxyHandlerException(e);
        }
    }

    @Override
    public Response providedByLicenses(ApiCallEvent event) throws ProxyHandlerException {
        JSONObject responseJson = new JSONObject();
        responseJson.put("licenses", providedByLicense(event));
        return Response.ok(responseJson).build();
    }

    @Override
    public Response info(ApiCallEvent event) throws ProxyHandlerException {
        try {
            //TODO: Cache
            JSONObject basicDoc = retrieveBasicDoc(this.pid);
            if (basicDoc != null) {
                JSONObject streams = new JSONObject(retrieveStreams(event));
                JSONObject info = new JSONObject(retrieveInfo(event));
                JSONObject data = extractAvailableDataInfo(streams);
                JSONObject image = extractImageSourceInfo(info, streams, basicDoc);
                JSONObject struct = extractStructureInfo(this.source, info, basicDoc);

                JSONObject json = new JSONObject();
                json.put("data", data);
                json.put("structure", struct);
                json.put("image", image);
                json.put("providedByLicenses", providedByLicense(event));
                return Response.ok(json).build();
            } else {
                if (this.deleteTriggerSupport != null) {
                    this.deleteTriggerSupport.executeDeleteTrigger(pid);
                }
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            
 
        } catch (JSONException | LexerException | IOException | RepositoryException e) {
            throw new ProxyHandlerException(e);
        }
    }

    protected JSONArray providedByLicense(ApiCallEvent event) {
        return new JSONArray();
    }

    static JSONObject extractAvailableDataInfo(JSONObject streams) throws LexerException {

        JSONObject dataAvailable = new JSONObject();
        // metadata
        JSONObject metadata = new JSONObject();
        metadata.put("mods", streams.has(KnownDatastreams.BIBLIO_MODS.toString()));
        metadata.put("dc", streams.has(KnownDatastreams.BIBLIO_DC.toString()));
        dataAvailable.put("metadata", metadata);
        JSONObject ocr = new JSONObject();
        // ocr
        ocr.put("text", streams.has(KnownDatastreams.OCR_TEXT.toString()));
        ocr.put("alto", streams.has(KnownDatastreams.OCR_ALTO.toString()));
        dataAvailable.put("ocr", ocr);
        // images
        JSONObject image = new JSONObject();
        image.put("full", streams.has(KnownDatastreams.IMG_FULL.toString()));
        image.put("thumb", streams.has(KnownDatastreams.IMG_THUMB.toString()));
        image.put("preview", streams.has(KnownDatastreams.IMG_PREVIEW.toString()));
        dataAvailable.put("image", image);
        // audio
        JSONObject audio = new JSONObject();
        audio.put("mp3", streams.has(KnownDatastreams.AUDIO_MP3.toString()));
        audio.put("ogg", streams.has(KnownDatastreams.AUDIO_OGG.toString()));
        audio.put("wav", streams.has(KnownDatastreams.AUDIO_WAV.toString()));
        dataAvailable.put("audio", audio);
        return dataAvailable;
    }

    protected JSONObject extractImageSourceInfo(JSONObject info, JSONObject streams, JSONObject basicDoc)
            throws IOException, RepositoryException, LexerException {
        JSONObject json = new JSONObject();

        if (info.has("zoom")) {
            json.put("type", "tiles");
        } else {
            boolean imgFull = streams.has(KnownDatastreams.IMG_FULL.name());
            boolean imgPreview = streams.has(KnownDatastreams.IMG_PREVIEW.name());
            // String retrieveMimetype = retrieveBasicDoc(pid);
            if (imgFull || imgPreview) {
                if (basicDoc != null && basicDoc.has("img_full_mime")) {
                    json.put("type", basicDoc.getString("img_full_mime"));
                } else {
                    json.put("type", "image/jpeg");
                }
            } else {
                json.put("type", "none");
            }
        }
        return json;
    }

    protected JSONObject extractStructureChildren(JSONObject info, JSONObject basicSolrDoc,
            List<List<Pair<String, String>>> paths) throws ProxyHandlerException {
        JSONObject retval = new JSONObject();

        try {
            Pair<JSONArray, JSONArray> rChildren = retrieveChildren(info, basicSolrDoc, paths);
            if (rChildren.getRight().length() > 0) {
                JSONArray foster = new JSONArray();
                for (int i = 0; i < rChildren.getRight().length(); i++) {
                    JSONObject doc = rChildren.getRight().getJSONObject(i);
                    JSONObject nDoc = new JSONObject();
                    nDoc.put("pid", doc.optString("PID"));
                    nDoc.put("relation", "isOnPage");

                    foster.put(nDoc);
                }
                retval.put("foster", foster);
            } else {
                JSONArray own = new JSONArray();
                for (int i = 0; i < rChildren.getLeft().length(); i++) {
                    JSONObject doc = rChildren.getLeft().getJSONObject(i);
                    JSONObject nDoc = new JSONObject();
                    String pid = doc.optString("PID");
                    String model = doc.optString("fedora.model");
                    OwnRelationsMapping found = OwnRelationsMapping.find(model);
                    if (found != null) {
                        KnownRelations relation = found.relation();
                        nDoc.put("pid", pid);
                        nDoc.put("relation", relation.toString());
                        own.put(nDoc);
                    } else {
                        System.out.println("");
                    }
                }
                retval.put("own", own);
            }

            return retval;
        } catch (UnsupportedEncodingException e) {
            throw new ProxyHandlerException(e);
        }
    }

    protected JSONObject extractStructureInfo(String source, JSONObject info, JSONObject basicSolrDoc)
            throws ProxyHandlerException {
        JSONObject structure = new JSONObject();
        JSONObject parents = new JSONObject();

        List<List<Pair<String, String>>> paths = paths(info);
        // String pid = info.getString("pid");
        String model = info.optString("model");
        structure.put("model", model);

        List<String> parentsList = new ArrayList<>();
        paths.forEach(path -> {
            if (path.size() > 1) {

                Pair<String, String> butLast = path.get(path.size() - 2);
                // String collect =
                // path.stream().map(Pair::getRight).collect(Collectors.joining("/"));
                if (!butLast.getRight().equals("article")) {
                    parentsList.add(butLast.getLeft());

                    OwnRelationsMapping found = OwnRelationsMapping.find(model);
                    JSONObject ownParent = new JSONObject();
                    ownParent.put("pid", butLast.getLeft());
                    if (found != null) {
                        ownParent.put("relation", found.relation().toString());
                    }
                    parents.put("own", ownParent);
                } else {

                    parentsList.add(butLast.getLeft());

                    FosterRelationsMapping found = FosterRelationsMapping.find(model);
                    JSONObject oneParent = new JSONObject();
                    oneParent.put("pid", butLast.getLeft());
                    if (found != null) {
                        oneParent.put("relation", found.relation(butLast.getRight()).toString());
                    }
                    if (!parents.has("foster")) {
                        parents.put("foster", new JSONArray());
                    }
                    parents.getJSONArray("foster").put(oneParent);
                }
            }
        });

        JSONObject children = extractStructureChildren(info, basicSolrDoc, paths);
        structure.put("children", children);

        structure.put("parent", parents);
        structure.put("model", model);
        return structure;

    }



    protected String retrieveStreams(ApiCallEvent event) {
        String baseurl = super.baseUrl();
        List<Triple<String, Long, Long>> granularTimeSnapshots = event != null ? event.getGranularTimeSnapshots() : null;
        long start = System.currentTimeMillis();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams";
        HttpGet httpGet = apacheGet(url, false);
        httpGet.setHeader("Accept", "application/json");
        try (CloseableHttpResponse response = this.apacheClient.execute(httpGet)) {
            int code = response.getCode();
            if (code  == 200) {
                long stop = System.currentTimeMillis();
                if (granularTimeSnapshots != null) granularTimeSnapshots.add(Triple.of("http/v5/streams",start, stop));
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                return IOUtils.toString(is, Charset.forName("UTF-8"));
            } else {
                LOGGER.log(Level.SEVERE, String.format("Bad status code %d", code));
                return null;
            }
        } catch(IOException ex) {
            LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
            return null;
        }
    }

    protected String retrieveInfo(ApiCallEvent event) {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid;
        List<Triple<String, Long, Long>> granularTimeSnapshots = event != null ? event.getGranularTimeSnapshots() : null;

        long start = System.currentTimeMillis();
        HttpGet httpGet = apacheGet(url, false);
        httpGet.setHeader("Accept", "application/json");
        try (CloseableHttpResponse response = this.apacheClient.execute(httpGet)) {
            long stop = System.currentTimeMillis();
            if (granularTimeSnapshots != null) {
                granularTimeSnapshots.add(Triple.of("http/v5/item/pid", start, stop));
            }
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            return IOUtils.toString(is, Charset.forName("UTF-8"));
        } catch(IOException ex) {
            LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
        }
        return null;
    }

    //TODO Chache
    protected JSONObject retrieveBasicDoc(String pid) throws ProxyHandlerException {
        try {
            String baseurl = super.baseUrl();
            String query = URLEncoder.encode("PID:\"" + pid + "\"", "UTF-8");
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/search?q=" + query + "&wt=json&fl="
                    + URLEncoder.encode("img_full_mime fedora.model pid_path parent_pid", "UTF-8");



            HttpGet httpGet = apacheGet(url, false);
            httpGet.setHeader("Accept", "application/json");
            try (CloseableHttpResponse response = this.apacheClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                String t = IOUtils.toString(is, Charset.forName("UTF-8"));
                JSONObject solrResponse = new JSONObject(t);
                JSONArray docs = solrResponse.getJSONObject("response").getJSONArray("docs");
                if (docs.length() >= 1) {
                    return docs.getJSONObject(0);
                }

            } catch(IOException ex) {
                LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
            }
            return null;

        } catch (UnsupportedEncodingException | UniformInterfaceException | ClientHandlerException | JSONException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    private Pair<JSONArray, JSONArray> retrieveChildren(JSONObject info, JSONObject basicSolrDoc,
            List<List<Pair<String, String>>> paths) throws UnsupportedEncodingException {
        int maxRows = 4000;
        String baseUrl = super.baseUrl();
        String url = "";
        String pid = info.optString("pid");
        String model = info.optString("model");
        // JSONArray parentPid = basicSolrDoc.optJSONArray("parent_pid");
        // foster relations

        if (model.equals(OwnRelationsMapping.article.name())) {
            List<Pair<String, String>> list = paths.get(0);
            String path = list.stream().map(Pair::getLeft).collect(Collectors.joining("/")) + "*";

            String encoded = URLEncoder.encode("pid_path:" + path.replace(":", "\\:"), "UTF-8") + "*"
                    + URLEncoder.encode(" AND NOT PID:" + pid.replace(":", "\\:"), "UTF-8");
            url = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "api/v5.0/search?q=*&fq=" + encoded + "&wt=json&rows="
                    + maxRows + "&fl="
                    + URLEncoder.encode("img_full_mime model_path fedora.model pid_path PID rels_ext_index", "UTF-8");
            HttpGet httpGet = apacheGet(url, false);
            httpGet.setHeader("Accept", "application/json");
            try (CloseableHttpResponse response = this.apacheClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                String t = IOUtils.toString(is, Charset.forName("UTF-8"));
                JSONObject solrResponse = new JSONObject(t);
                JSONArray docs = solrResponse.getJSONObject("response").getJSONArray("docs");

                List<JSONObject> ll = new ArrayList<>();
                for (int i = 0; i < docs.length(); i++) {
                    ll.add(docs.getJSONObject(i));
                }

                ll.sort((JSONObject left, JSONObject right) -> {
                    JSONArray leftArr = left.optJSONArray("rels_ext_index");
                    JSONArray rightArr = right.optJSONArray("rels_ext_index");
                    if (leftArr != null && leftArr.length() > 0 && rightArr != null && rightArr.length() > 0) {
                        return Integer.valueOf(leftArr.getInt(0)).compareTo(Integer.valueOf(rightArr.getInt(0)));
                    } else {
                        return 0;
                    }
                });

                JSONArray sorted = new JSONArray();
                ll.forEach(sorted::put);
                return Pair.of(new JSONArray(), sorted);
            } catch(IOException ex) {
                LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
                return null;
            }

        } else {
            // String pPid = parentPid.getString(0);
            String query = URLEncoder.encode(
                    "parent_pid:" + pid.replace(":", "\\:") + " AND NOT PID:" + pid.replace(":", "\\:"), "UTF-8");
            url = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "api/v5.0/search?q=*&fq=" + query + "&wt=json&rows="
                    + maxRows + "&fl="
                    + URLEncoder.encode("img_full_mime model_path fedora.model pid_path PID rels_ext_index", "UTF-8");

            HttpGet httpGet = apacheGet(url, false);
            httpGet.setHeader("Accept", "application/json");
            try (CloseableHttpResponse response = this.apacheClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();
                String t = IOUtils.toString(is, Charset.forName("UTF-8"));

                JSONObject solrResponse = new JSONObject(t);
                JSONArray docs = solrResponse.getJSONObject("response").getJSONArray("docs");

                List<JSONObject> ll = new ArrayList<>();
                for (int i = 0; i < docs.length(); i++) {
                    ll.add(docs.getJSONObject(i));
                }

                ll.sort((JSONObject left, JSONObject right) -> {
                    JSONArray leftArr = left.optJSONArray("rels_ext_index");
                    JSONArray rightArr = right.optJSONArray("rels_ext_index");
                    if (leftArr != null && leftArr.length() > 0 && rightArr != null && rightArr.length() > 0) {
                        return Integer.valueOf(leftArr.getInt(0)).compareTo(Integer.valueOf(rightArr.getInt(0)));
                    } else {
                        return 0;
                    }
                });

                JSONArray sorted = new JSONArray();
                ll.forEach(sorted::put);

                return Pair.of(sorted, new JSONArray());

            } catch(IOException ex) {
                LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
                return null;
            }
        }
    }

    private List<List<Pair<String, String>>> paths(JSONObject info) {
        List<List<Pair<String, String>>> pidPaths = new ArrayList<>();
        JSONArray masterArray = info.getJSONArray("context");
        for (int i = 0; i < masterArray.length(); i++) {
            List<Pair<String, String>> onePath = new ArrayList<>();
            JSONArray path = masterArray.getJSONArray(i);
            for (int j = 0; j < path.length(); j++) {
                JSONObject pathObject = path.getJSONObject(j);
                Pair<String, String> pair = Pair.of(pathObject.getString("pid"), pathObject.getString("model"));
                onePath.add(pair);
            }
            pidPaths.add(onePath);
        }
        return pidPaths;
    }

    static JSONObject pidAndRelationToJson(String pid, String relation) {
        JSONObject json = new JSONObject();
        json.put("pid", pid);
        json.put("relation", relation);
        return json;
    }

    @Override
    public Response infoImage(ApiCallEvent event) throws ProxyHandlerException {
        try {
            JSONObject streams = new JSONObject(retrieveStreams(event));
            JSONObject info = new JSONObject(retrieveInfo(event));
            JSONObject basicDoc = retrieveBasicDoc(this.pid);
            JSONObject extractImageSourceInfo = extractImageSourceInfo(info, streams, basicDoc);
            return Response.ok(extractImageSourceInfo).build();
        } catch (JSONException | IOException | RepositoryException | LexerException e) {
            throw new ProxyHandlerException(e);
        }
    }

    @Override
    public Response infoStructure(ApiCallEvent event) throws ProxyHandlerException {
        long start = System.currentTimeMillis();
        JSONObject info = new JSONObject(retrieveInfo(event));
        JSONObject basicSolrDoc = retrieveBasicDoc(this.pid);
        JSONObject extractStructureInfo = extractStructureInfo(this.source, info, basicSolrDoc);
        return Response.ok(extractStructureInfo).build();
    }

    @Override
    public Response audioMP3(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/strams/MP3";
        return buildRedirectResponse(url);
    }

    @Override
    public Response audioOGG(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/strams/OGG";
        return buildRedirectResponse(url);
    }

    @Override
    public Response audioWAV(ApiCallEvent event) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/strams/WAV";
        return buildRedirectResponse(url);
    }
}
