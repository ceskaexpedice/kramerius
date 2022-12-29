package cz.incad.kramerius.rest.apiNew.client.v60.redirection.item;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.KrameriusRepositoryApi.FosterRelationsMapping;
import cz.incad.kramerius.repository.KrameriusRepositoryApi.KnownRelations;
import cz.incad.kramerius.repository.KrameriusRepositoryApi.OwnRelationsMapping;
import cz.incad.kramerius.rest.apiNew.client.v60.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v60.redirection.ProxyHandlerException;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.pid.LexerException;

public class V5RedirectHandler extends ProxyItemHandler {

    public static final Logger LOGGER = Logger.getLogger(V5RedirectHandler.class.getName());

    public V5RedirectHandler(Instances instances, User user, Client client, SolrAccess solrAccess, String source,
            String pid, String remoteAddr) {
        super(instances, user, client, solrAccess, source, pid, remoteAddr);
    }

    @Override
    public Response image(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/IMG_FULL";
        return buildRedirectResponse(url);
    }

    @Override
    public Response imagePreview(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid
                + "/streams/IMG_PREVIEW";
        return buildRedirectResponse(url);
    }

    @Override
    public Response textOCR(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/TEXT_OCR";
        return buildRedirectResponse(url);
    }

    @Override
    public Response altoOCR(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/ALTO";
        return buildRedirectResponse(url);
    }

    @Override
    public Response zoomifyImageProperties(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "zoomify/" + this.pid + "/ImageProperties.xml";
        return buildRedirectResponse(url);
    }

    @Override
    public Response mods(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid
                + "/streams/BIBLIO_MODS";
        return buildRedirectResponse(url);
    }

    @Override
    public Response dc(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams/DC";
        return buildRedirectResponse(url);
    }

    @Override
    public Response zoomifyTile(String tileGroupStr, String tileStr) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String formatted = String.format("zoomify/%s/%s/%s", this.pid, tileGroupStr, tileStr);
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + formatted;
        return buildRedirectResponse(url);
    }

    @Override
    public Response imageThumb(RequestMethodName method) throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/thumb";
        return buildRedirectResponse(url);
    }

    @Override
    public Response infoData() throws ProxyHandlerException {
        try {
            JSONObject streams = new JSONObject(retrieveStreams());
            JSONObject dataInfo = extractAvailableDataInfo(streams);
            return Response.ok(dataInfo).build();
        } catch (JSONException | LexerException e) {
            throw new ProxyHandlerException(e);
        }
    }

    @Override
    public Response providedByLicenses() throws ProxyHandlerException {
        JSONObject responseJson = new JSONObject();
        responseJson.put("licenses", providedByLicense());
        return Response.ok(responseJson).build();
    }

    @Override
    public Response info() throws ProxyHandlerException {
        try {
            JSONObject streams = new JSONObject(retrieveStreams());
            JSONObject info = new JSONObject(retrieveInfo());
            JSONObject basicDoc = retrieveBasicDoc(this.pid);

            JSONObject data = extractAvailableDataInfo(streams);
            JSONObject image = extractImageSourceInfo(info, streams, basicDoc);
            JSONObject struct = extractStructureInfo(this.source, info, basicDoc);

            JSONObject json = new JSONObject();
            json.put("data", data);
            json.put("structure", struct);
            json.put("image", image);
            // TODO: providedByLicenses
            json.put("providedByLicenses", providedByLicense());

            return Response.ok(json).build();
        } catch (JSONException | LexerException | IOException | RepositoryException e) {
            throw new ProxyHandlerException(e);
        }
    }

    protected JSONArray providedByLicense() {
        return new JSONArray();
    }

    static JSONObject extractAvailableDataInfo(JSONObject streams) throws LexerException {

        JSONObject dataAvailable = new JSONObject();
        // metadata
        JSONObject metadata = new JSONObject();
        metadata.put("mods", streams.has(KrameriusRepositoryApi.KnownDatastreams.BIBLIO_MODS.toString()));
        metadata.put("dc", streams.has(KrameriusRepositoryApi.KnownDatastreams.BIBLIO_DC.toString()));
        dataAvailable.put("metadata", metadata);
        JSONObject ocr = new JSONObject();
        // ocr
        ocr.put("text", streams.has(KrameriusRepositoryApi.KnownDatastreams.OCR_TEXT.toString()));
        ocr.put("alto", streams.has(KrameriusRepositoryApi.KnownDatastreams.OCR_ALTO.toString()));
        dataAvailable.put("ocr", ocr);
        // images
        JSONObject image = new JSONObject();
        image.put("full", streams.has(KrameriusRepositoryApi.KnownDatastreams.IMG_FULL.toString()));
        image.put("thumb", streams.has(KrameriusRepositoryApi.KnownDatastreams.IMG_THUMB.toString()));
        image.put("preview", streams.has(KrameriusRepositoryApi.KnownDatastreams.IMG_PREVIEW.toString()));
        dataAvailable.put("image", image);
        // audio
        JSONObject audio = new JSONObject();
        audio.put("mp3", streams.has(KrameriusRepositoryApi.KnownDatastreams.AUDIO_MP3.toString()));
        audio.put("ogg", streams.has(KrameriusRepositoryApi.KnownDatastreams.AUDIO_OGG.toString()));
        audio.put("wav", streams.has(KrameriusRepositoryApi.KnownDatastreams.AUDIO_WAV.toString()));
        dataAvailable.put("audio", audio);
        return dataAvailable;
    }

    protected JSONObject extractImageSourceInfo(JSONObject info, JSONObject streams, JSONObject basicDoc)
            throws IOException, RepositoryException, LexerException {
        JSONObject json = new JSONObject();

        if (info.has("zoom")) {
            json.put("type", "tiles");
        } else {
            boolean imgFull = streams.has(KrameriusRepositoryApi.KnownDatastreams.IMG_FULL.name());
            boolean imgPreview = streams.has(KrameriusRepositoryApi.KnownDatastreams.IMG_PREVIEW.name());
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

    protected String retrieveStreams() {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/streams";
        WebResource r = this.client.resource(url);
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    protected String retrieveInfo() {
        String baseurl = super.baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid;
        WebResource r = this.client.resource(url);
        String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
        return t;
    }

    protected JSONObject retrieveBasicDoc(String pid) throws ProxyHandlerException {
        try {
            String baseurl = super.baseUrl();
            String query = URLEncoder.encode("PID:\"" + pid + "\"", "UTF-8");
            String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/search?q=" + query + "&wt=json&fl="
                    + URLEncoder.encode("img_full_mime fedora.model pid_path parent_pid", "UTF-8");
            WebResource r = this.client.resource(url);
            String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
            JSONObject solrResponse = new JSONObject(t);
            JSONArray docs = solrResponse.getJSONObject("response").getJSONArray("docs");
            if (docs.length() >= 1) {
                return docs.getJSONObject(0);
            }
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

            WebResource r = client.resource(url);
            String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
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
        } else {
            // String pPid = parentPid.getString(0);
            String query = URLEncoder.encode(
                    "parent_pid:" + pid.replace(":", "\\:") + " AND NOT PID:" + pid.replace(":", "\\:"), "UTF-8");
            url = baseUrl + (baseUrl.endsWith("/") ? "" : "/") + "api/v5.0/search?q=*&fq=" + query + "&wt=json&rows="
                    + maxRows + "&fl="
                    + URLEncoder.encode("img_full_mime model_path fedora.model pid_path PID rels_ext_index", "UTF-8");

            WebResource r = client.resource(url);
            String t = r.accept(MediaType.APPLICATION_JSON).get(String.class);
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
    public Response infoImage() throws ProxyHandlerException {
        try {
            JSONObject streams = new JSONObject(retrieveStreams());
            JSONObject info = new JSONObject(retrieveInfo());
            JSONObject basicDoc = retrieveBasicDoc(this.pid);
            JSONObject extractImageSourceInfo = extractImageSourceInfo(info, streams, basicDoc);
            return Response.ok(extractImageSourceInfo).build();
        } catch (JSONException | IOException | RepositoryException | LexerException e) {
            throw new ProxyHandlerException(e);
        }
    }

    @Override
    public Response infoStructure() throws ProxyHandlerException {
        long start = System.currentTimeMillis();
        JSONObject info = new JSONObject(retrieveInfo());
        JSONObject basicSolrDoc = retrieveBasicDoc(this.pid);
        JSONObject extractStructureInfo = extractStructureInfo(this.source, info, basicSolrDoc);
        return Response.ok(extractStructureInfo).build();
    }

    @Override
    public Response audioMP3() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/strams/MP3";
        return buildRedirectResponse(url);
    }

    @Override
    public Response audioOGG() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/strams/OGG";
        return buildRedirectResponse(url);
    }

    @Override
    public Response audioWAV() throws ProxyHandlerException {
        String baseurl = baseUrl();
        String url = baseurl + (baseurl.endsWith("/") ? "" : "/") + "api/v5.0/item/" + this.pid + "/strams/WAV";
        return buildRedirectResponse(url);
    }
}
