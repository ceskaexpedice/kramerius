package cz.incad.kramerius.rest.apiNew.client.v60;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.Dom4jUtils;
import cz.incad.kramerius.utils.java.Pair;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Document;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * @see cz.incad.kramerius.rest.api.k5.client.item.ItemResource
 */
@Path("/client/v6.0/items")
public class ItemsResource extends ClientApiResource {

    //TODO: uklid
    //(ne-admin) client je neutentizovany, jenom cte data a mela by pred nim byt do urcite miry skryta implementece, takze:

    // {pid}/foxml                  -> zrusit tady, presunout do admin api - DONE
    // {pid}/streams                -> nahradit za {pid}/info/data  - DONE
    // {pid}/full                   -> nahradit za {pid}/image - DONE
    // {pid}/thumb                  -> nahradit za {pid}/image/thumb - DONE
    // {pid}/preview                -> nahradit za {pid}/image/preview - DONE
    // {pid}/streams/BIBLIO_MODS    -> nahradit za {pid}/metadata/mods - DONE
    // {pid}/streams/DC             -> nahradit za {pid}/metadata/dc - DONE
    // {pid}/streams/RELS_EXT       -> nahradit za {pid}/info/structure - DONE
    // {pid}/streams/OCR_TEXT       -> nahradit za {pid}/ocr/text  - DONE
    // {pid}/streams/OCR_ALTO       -> nahradit za {pid}/ocr/alto - DONE
    // {pid}/streams/MP3            -> nahradit za {pid}/audio/mp3 - DONE
    // {pid}/streams/OGG            -> nahradit za {pid}/audio/ogg - DONE
    // {pid}/streams/WAV            -> nahradit za {pid}/audio/wav - DONE


    //pripadne jen plochou strukturu ( {pid}/mods, {pid}/thumb {pid}/full, {pid}/children ...)
    //Výsledek:
    // HEAD     {pid}
    // GET      {pid}/info
    // GET      {pid}/info/structure
    // GET      {pid}/info/data
    // GET      {pid}/info/image
    // GET/HEAD {pid}/metadata/mods
    // GET/HEAD {pid}/metadata/dc
    // GET/HEAD {pid}/ocr/text
    // GET/HEAD {pid}/ocr/alto
    // GET/HEAD {pid}/image             - obsah IMG_FULL konkrétního objektu
    // GET      {pid}/image/thumb       - IMG_THUMB objektu nebo potomka
    // GET      {pid}/image/preview     - IMG_PREVIEW objektu nebo potomka
    // GET/HEAD {pid}/audio/mp3
    // GET/HEAD {pid}/audio/ogg
    // GET/HEAD {pid}/audio/wav


    public static final Logger LOGGER = Logger.getLogger(ItemsResource.class.getName());

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @HEAD
    @Path("{pid}")
    public Response checkItemExists(@PathParam("pid") String pid) {
        checkObjectExists(pid);
        return Response.ok().build();
    }

    @GET
    @Path("{pid}/info")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfo(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectExists(pid);
            JSONObject json = new JSONObject();
            json.put("data", extractAvailableDataInfo(pid));
            json.put("structure", extractStructureInfo(pid));
            json.put("image", extractImageSourceInfo(pid));
            return Response.ok(json).build();
        } catch (RepositoryException | SolrServerException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/info/data")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfoData(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectExists(pid);
            return Response.ok(extractAvailableDataInfo(pid)).build();
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Vrací jen přímou strukturu získanou okamžitě z resource-indexu. Tedy rodiče (vlastního, nevlastní), děti (vlastní, nevlastní).
     * Ale už ne věci, které by se musely dopočítávat přes několik dotazů (root v stromech rodičů, sourozenci),
     * tyto věci jsou dostupné z vyhledávacího indexu, kde se integrují v rámci procesu indexace.
     */
    @GET
    @Path("{pid}/info/structure")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfoStructure(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectExists(pid);
            return Response.ok(extractStructureInfo(pid)).build();
        } catch (RepositoryException | SolrServerException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    /***
     * Vrací informaci o tom, jaký zdroj pro obrazová data má objekt (stránka, monografie v jednom pdf, ...) k dispozici,
     * buď tiles (dlaždice přes zoomify/iiif), nebo none, nebo mimetype (image/jpeg, application/pdf, ...) datastreamu IMG_FULL
     */
    @GET
    @Path("{pid}/info/image")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfoImage(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectExists(pid);
            return Response.ok(extractImageSourceInfo(pid)).build();
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    private JSONObject extractAvailableDataInfo(String pid) throws IOException, RepositoryException {
        JSONObject dataAvailable = new JSONObject();
        //metadata
        JSONObject metadata = new JSONObject();
        metadata.put("mods", krameriusRepositoryApi.isModsAvailable(pid));
        metadata.put("dc", krameriusRepositoryApi.isDublinCoreAvailable(pid));
        dataAvailable.put("metadata", metadata);
        JSONObject ocr = new JSONObject();
        //ocr
        ocr.put("text", krameriusRepositoryApi.isOcrTextAvailable(pid));
        ocr.put("alto", krameriusRepositoryApi.isOcrAltoAvailable(pid));
        dataAvailable.put("ocr", ocr);
        //images
        JSONObject image = new JSONObject();
        image.put("full", krameriusRepositoryApi.isImgFullAvailable(pid));
        image.put("thumb", krameriusRepositoryApi.isImgThumbAvailable(pid));
        image.put("preview", krameriusRepositoryApi.isImgPreviewAvailable(pid));
        dataAvailable.put("image", image);
        //audio
        JSONObject audio = new JSONObject();
        audio.put("mp3", krameriusRepositoryApi.isAudioMp3Available(pid));
        audio.put("ogg", krameriusRepositoryApi.isAudioOggAvailable(pid));
        audio.put("wav", krameriusRepositoryApi.isAudioWavAvailable(pid));
        dataAvailable.put("audio", audio);
        return dataAvailable;
    }

    private JSONObject extractStructureInfo(String pid) throws RepositoryException, SolrServerException, IOException {
        JSONObject structure = new JSONObject();
        //parents
        JSONObject parents = new JSONObject();
        Pair<RepositoryApi.Triplet, List<RepositoryApi.Triplet>> parentsTpls = krameriusRepositoryApi.getParents(pid);
        if (parentsTpls.getFirst() != null) {
            parents.put("own", pidAndRelationToJson(parentsTpls.getFirst().source, parentsTpls.getFirst().relation));
        }
        JSONArray fosterParents = new JSONArray();
        for (RepositoryApi.Triplet fosterParentTpl : parentsTpls.getSecond()) {
            fosterParents.put(pidAndRelationToJson(fosterParentTpl.source, fosterParentTpl.relation));
        }
        parents.put("foster", fosterParents);
        structure.put("parents", parents);
        //children
        JSONObject children = new JSONObject();
        Pair<List<RepositoryApi.Triplet>, List<RepositoryApi.Triplet>> childrenTpls = krameriusRepositoryApi.getChildren(pid);
        JSONArray ownChildren = new JSONArray();
        for (RepositoryApi.Triplet ownChildTpl : childrenTpls.getFirst()) {
            ownChildren.put(pidAndRelationToJson(ownChildTpl.target, ownChildTpl.relation));
        }
        children.put("own", ownChildren);
        JSONArray fosterChildren = new JSONArray();
        for (RepositoryApi.Triplet fosterChildTpl : childrenTpls.getSecond()) {
            fosterChildren.put(pidAndRelationToJson(fosterChildTpl.target, fosterChildTpl.relation));
        }
        children.put("foster", fosterChildren);
        structure.put("children", children);
        //model
        String model = krameriusRepositoryApi.getModel(pid);
        structure.put("model", model);

        return structure;
    }

    private Object extractImageSourceInfo(String pid) throws IOException, RepositoryException {
        JSONObject json = new JSONObject();
        Document relsExt = krameriusRepositoryApi.getRelsExt(pid, false);
        String tilesUrl = Dom4jUtils.stringOrNullFromFirstElementByXpath(relsExt.getRootElement(), "//tiles-url");
        if (tilesUrl != null) {
            json.put("type", "tiles");
        } else if (!krameriusRepositoryApi.isImgFullAvailable(pid)) {
            json.put("type", "none");
        } else {
            String imgFullMimetype = krameriusRepositoryApi.getImgFullMimetype(pid);
            if (imgFullMimetype == null) {
                json.put("type", "none");
            } else {
                //jpeg, pdf, etc.
                json.put("type", imgFullMimetype);
            }
        }
        return json;
    }

    private JSONObject pidAndRelationToJson(String pid, String relation) {
        JSONObject json = new JSONObject();
        json.put("pid", pid);
        json.put("relation", relation);
        return json;
    }

    @HEAD
    @Path("{pid}/metadata/mods")
    public Response isMetadataModsAvailable(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.BIBLIO_MODS);
        return Response.ok().build();
    }

    @GET
    @Path("{pid}/metadata/mods")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getMetadataMods(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.BIBLIO_MODS);
            Document mods = krameriusRepositoryApi.getMods(pid, true);
            return Response.ok()
                    .entity(mods.asXML())
                    .build();
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/metadata/dc")
    public Response isMetadataDublinCoreAvailable(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.BIBLIO_DC);
        return Response.ok().build();
    }

    @GET
    @Path("{pid}/metadata/dc")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getMetadataDublinCore(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.BIBLIO_DC);
            Document dc = krameriusRepositoryApi.getDublinCore(pid, true);
            return Response.ok().entity(dc.asXML()).build();
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/ocr/text")
    public Response isOcrTextAvailable(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.OCR_TEXT);
        return Response.ok().build();
    }

    @GET
    @Path("{pid}/ocr/text")
    @Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
    public Response getOcrText(@PathParam("pid") String pid) {
        //TODO: pořádně otestovat:
        //managed from URL:
        //http://localhost:8080/search/api/admin/v1.0/items/uuid:d41a05bb-7ec7-474c-adeb-da4cdfeaab3a/foxml
        //http://localhost:8080/search/api/client/v6.0/items/uuid:d41a05bb-7ec7-474c-adeb-da4cdfeaab3a/ocr/text

        //managed form file://
        //http://localhost:8080/search/api/admin/v1.0/items/uuid:fc09d4ee-9937-4d46-8f09-d710e72b6425/foxml
        //http://localhost:8080/search/api/client/v6.0/items/uuid:fc09d4ee-9937-4d46-8f09-d710e72b6425/ocr/text

        //redirect, externally referenced

        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.OCR_TEXT);
            String ocrText = krameriusRepositoryApi.getOcrText(pid);
            return Response.ok().entity(ocrText).build();
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/ocr/alto")
    public Response isOcrAltoAvailable(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.OCR_ALTO);
        return Response.ok().build();
    }

    @GET
    @Path("{pid}/ocr/alto")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getDatastreamOcrAlto(@PathParam("pid") String pid) {
        //TODO: pořádně otestovat datastreamy s různými controlgroups (M,E,R) a s odkazy typu URL, path
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.OCR_ALTO);
            Document ocrAlto = krameriusRepositoryApi.getOcrAlto(pid, true);
            return Response.ok().entity(ocrAlto.asXML()).build();
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Zkontroluje existenci a právo čtení datastreamu IMG_FULL
     */
    @HEAD
    @Path("{pid}/image")
    public Response isImgFullAvailable(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.IMG_FULL);
        return Response.ok().build();
    }

    /***
     * Vrací obsah datastreamu IMG_FULL tohoto objektu
     * @see cz.incad.Kramerius.imaging.ImageStreamsServlet
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/image")
    public Response getImgFull(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.IMG_FULL);
            String mimeType = krameriusRepositoryApi.getImgFullMimetype(pid);
            InputStream is = krameriusRepositoryApi.getImgFull(pid);
            StreamingOutput stream = output -> {
                IOUtils.copy(is, output);
                IOUtils.closeQuietly(is);
            };
            return Response.ok().entity(stream).type(mimeType).build();
        } catch (RepositoryException |
                IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    /***
     * Vrací thumbnail buď tohoto objektu, nebo prvního potomka, který má IMG_THUMB
     */
    @GET
    @Path("{pid}/image/thumb")
    public Response getImgThumb(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectExists(pid);
            Pair<InputStream, String> imgThumb = getFirstAvailableImgThumb(pid);
            if (imgThumb == null) {
                throw new NotFoundException("no image/thumb available for object %s (and it's descendants)", pid);
            } else {
                StreamingOutput stream = output -> {
                    IOUtils.copy(imgThumb.getFirst(), output);
                    IOUtils.closeQuietly(imgThumb.getFirst());
                };
                return Response.ok().entity(stream).type(imgThumb.getSecond()).build();
            }
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    /***
     * Vrací preview buď tohoto objektu, nebo prvního potomka, který má IMG_PREVIEW
     */
    @GET
    @Path("{pid}/image/preview")
    public Response getImgPreview(@PathParam("pid") String pid) {
        try {
            checkObjectExists(pid);
            Pair<InputStream, String> imgPreview = getFirstAvailableImgPreview(pid);
            if (imgPreview == null) {
                throw new NotFoundException("no image/preview available for object %s (and it's descendants)", pid);
            } else {
                StreamingOutput stream = output -> {
                    IOUtils.copy(imgPreview.getFirst(), output);
                    IOUtils.closeQuietly(imgPreview.getFirst());
                };
                return Response.ok().entity(stream).type(imgPreview.getSecond()).build();
            }
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/audio/mp3")
    public Response isAudioMp3Available(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.AUDIO_MP3);
        return Response.ok().header("Accept-Ranges", "bytes").build();
    }

    /***
     * Vrací obsah datastreamu MP3 tohoto objektu
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/audio/mp3")
    public Response getAudioMp3(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.AUDIO_MP3);
            String mimeType = krameriusRepositoryApi.getAudioMp3Mimetype(pid);
            String headerRange = requestProvider.get().getHeader("Range");
            boolean rangeEmpty = headerRange == null || headerRange.isEmpty();
            boolean rangeSupported = !rangeEmpty && headerRange.matches("bytes=\\d*-\\d*");//|| hdrRange.matches("bytes=0-\\d+");
            if (rangeEmpty || !rangeSupported) { //without Range or Range ignored
                InputStream is = krameriusRepositoryApi.getAudioMp3(pid);
                StreamingOutput stream = output -> {
                    IOUtils.copy(is, output);
                    IOUtils.closeQuietly(is);
                };
                return Response.ok().entity(stream).type(mimeType)
                        .header("Accept-Ranges", "bytes")
                        .build();
            } else { //within Range
                InputStream is = krameriusRepositoryApi.getAudioMp3(pid);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int totalSize = IOUtils.copy(is, buffer);
                IOUtils.closeQuietly(is);
                //this should be cached (in Akubra?), following requests with Range will very probably follow
                byte[] dataComplete = buffer.toByteArray();

                Integer start = 0;
                Integer end = dataComplete.length;
                String[] rangeItems = headerRange.substring(("bytes=".length())).split("-");
                if (!rangeItems[0].equals("")) {
                    start = Integer.valueOf(rangeItems[0]);
                }
                if (rangeItems.length == 2 && !rangeItems[1].equals("")) {
                    start = Integer.valueOf(rangeItems[1]);
                }
                byte[] dataInRange = Arrays.copyOfRange(dataComplete, start, end);
                return Response.status(206).entity(dataInRange)
                        .header("Accept-Ranges", "bytes")
                        .header("Content-Range", String.format("bytes %d-%d/%d", start, end, totalSize))
                        .header("Content-Length", end - start)
                        .type(mimeType).build();
            }
        } catch (RepositoryException | IOException e) {
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/audio/ogg")
    public Response isAudioOggAvailable(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.AUDIO_OGG);
        return Response.ok().build();
    }

    /***
     * Vrací obsah datastreamu OGG tohoto objektu
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/audio/ogg")
    public Response getAudioOgg(@PathParam("pid") String pid) {
        //TODO: test Content-Range
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.AUDIO_OGG);
            String mimeType = krameriusRepositoryApi.getAudioOggMimetype(pid);
            InputStream is = krameriusRepositoryApi.getAudioOgg(pid);
            StreamingOutput stream = output -> {
                IOUtils.copy(is, output);
                IOUtils.closeQuietly(is);
            };
            return Response.ok().entity(stream).type(mimeType).build();
        } catch (RepositoryException |
                IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/audio/wav")
    public Response isAudioWavAvailable(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.AUDIO_WAV);
        return Response.ok().build();
    }

    /***
     * Vrací obsah datastreamu WAV tohoto objektu
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/audio/wav")
    public Response getAudioWav(@PathParam("pid") String pid) {
        //TODO: test Content-Range
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.AUDIO_WAV);
            String mimeType = krameriusRepositoryApi.getAudioWavMimetype(pid);
            InputStream is = krameriusRepositoryApi.getAudioWav(pid);
            StreamingOutput stream = output -> {
                IOUtils.copy(is, output);
                IOUtils.closeQuietly(is);
            };
            return Response.ok().entity(stream).type(mimeType).build();
        } catch (RepositoryException |
                IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    Pair<InputStream, String> getFirstAvailableImgFull(String pid) throws IOException, RepositoryException {
        InputStream is = krameriusRepositoryApi.getImgFull(pid);
        if (is != null) {
            String mimeType = krameriusRepositoryApi.getImgFullMimetype(pid);
            return new Pair<>(is, mimeType);
        } else {
            String pidOfFirstChild = getPidOfFirstChild(pid);
            if (pidOfFirstChild != null) {
                return getFirstAvailableImgFull(pidOfFirstChild);
            } else {
                return null;
            }
        }
    }

    Pair<InputStream, String> getFirstAvailableImgThumb(String pid) throws IOException, RepositoryException {
        InputStream is = krameriusRepositoryApi.getImgThumb(pid);
        if (is != null) {
            String mimeType = krameriusRepositoryApi.getImgThumbMimetype(pid);
            return new Pair<>(is, mimeType);
        } else {
            String pidOfFirstChild = getPidOfFirstChild(pid);
            if (pidOfFirstChild != null) {
                return getFirstAvailableImgThumb(pidOfFirstChild);
            } else {
                return null;
            }
        }
    }

    Pair<InputStream, String> getFirstAvailableImgPreview(String pid) throws IOException, RepositoryException {
        InputStream is = krameriusRepositoryApi.getImgPreview(pid);
        if (is != null) {
            String mimeType = krameriusRepositoryApi.getImgPreviewMimetype(pid);
            return new Pair<>(is, mimeType);
        } else {
            String pidOfFirstChild = getPidOfFirstChild(pid);
            if (pidOfFirstChild != null) {
                return getFirstAvailableImgPreview(pidOfFirstChild);
            } else {
                return null;
            }
        }
    }

    private String getPidOfFirstChild(String pid) throws IOException, RepositoryException {
        Document relsExt = krameriusRepositoryApi.getRelsExt(pid, false);
        String xpathExpr = "//hasPage|//hasUnit|//hasVolume|//hasItem|//hasSoundUnit|//hasTrack|//containsTrack|//hasIntCompPart|//isOnPage|//contains";
        Element element = Dom4jUtils.firstElementByXpath(relsExt.getRootElement(), xpathExpr);
        if (element != null) {
            String resource = Dom4jUtils.stringOrNullFromAttributeByName(element, "resource");
            if (resource != null) {
                return resource.substring("info:fedora/".length());
            }
        }
        return null;
    }

    private String getApiBaseUrl() {
        //return "http://localhost:8080/search/api";
        String appUrl = ApplicationURL.applicationURL(this.requestProvider.get());
        return appUrl + "/api";
    }

}
