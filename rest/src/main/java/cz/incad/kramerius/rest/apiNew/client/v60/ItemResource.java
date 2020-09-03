package cz.incad.kramerius.rest.apiNew.client.v60;

import com.google.inject.Inject;
import com.google.inject.Provider;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.RepositoryApi;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.java.Pair;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @see cz.incad.kramerius.rest.api.k5.client.item.ItemResource
 */
@Path("/client/v6.0/item")
public class ItemResource extends ClientApiResource {

    //TODO: uklid
    //(ne-admin) client je neutentizovany, jenom cte data a mela by pred nim byt do urcite miry skryta implementece, takze:

    // {pid}/foxml                  -> zrusit tady, presunout do admin api - DONE
    // {pid}/streams                -> nahradit za {pid}/info/data  - DONE
    // {pid}/full                   -> nahradit za {pid}/image/full
    // {pid}/thumb                  -> nahradit za {pid}/image/thumb
    // {pid}/preview                -> nahradit za {pid}/image/preview, nebo uplne zrusit (nepouziva se bud thumb, nebo preview, nikdy nevim ktery)
    // {pid}/streams/BIBLIO_MODS    -> nahradit za {pid}/metadata/mods - DONE
    // {pid}/streams/DC             -> nahradit za {pid}/metadata/dc - DONE
    // {pid}/streams/RELS_EXT       -> nahradit za {pid}/info/structure - DONE
    // {pid}/streams/OCR_TEXT       -> nahradit za {pid}/ocr/text  - DONE
    // {pid}/streams/OCR_ALTO       -> nahradit za {pid}/ocr/alto - DONE
    // {pid}/streams/MP3            -> nahradit za {pid}/audio/mp3
    // {pid}/streams/OGG            -> nahradit za {pid}/audio/ogg
    // {pid}/streams/WAV            -> nahradit za {pid}/audio/wav


    //pripadne jen plochou strukturu ( {pid}/mods, {pid}/thumb {pid}/full, {pid}/children ...)
    //Výsledek:
    // HEAD     {pid}
    // GET      {pid}/info
    // GET      {pid}/info/data
    // GET      {pid}/info/structure
    // GET/HEAD {pid}/metadata/mods
    // GET/HEAD {pid}/metadata/dc
    // GET/HEAD {pid}/ocr/text
    // GET/HEAD {pid}/ocr/alto
    // TODO: obrazova, zvukova data

    public static final Logger LOGGER = Logger.getLogger(ItemResource.class.getName());

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
            json.put("data-available", extractAvailableDataInfo(pid));
            json.put("structure", extractStructureInfo(pid));
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

    @GET
    @Path("{pid}/info/structure")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    /**
     * Vrací jen přímou strukturu získanou okamžitě z resource-indexu. Tedy rodiče (vlastního, nevlastní), děti (vlastní, nevlastní).
     * Ale už ne věci, které by se musely dopočítávat přes několik dotazů (root v stromech rodičů, sourozenci),
     * tyto věci jsou dostupné z vyhledávacího indexu, kde se integrují v rámci procesu indexace.
     */
    public Response getInfoStructure(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectExists(pid);
            return Response.ok(extractStructureInfo(pid)).build();
        } catch (RepositoryException | SolrServerException | IOException e) {
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
        return structure;
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
        //http://localhost:8080/search/api/admin/v1.0/item/uuid:d41a05bb-7ec7-474c-adeb-da4cdfeaab3a/foxml
        //http://localhost:8080/search/api/client/v6.0/item/uuid:d41a05bb-7ec7-474c-adeb-da4cdfeaab3a/ocr/text

        //managed form file://
        //http://localhost:8080/search/api/admin/v1.0/item/uuid:fc09d4ee-9937-4d46-8f09-d710e72b6425/foxml
        //http://localhost:8080/search/api/client/v6.0/item/uuid:fc09d4ee-9937-4d46-8f09-d710e72b6425/ocr/text

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

    @GET
    @Path("{pid}/image/full")
    public Response getImgFull(@PathParam("pid") String pid, @QueryParam("asFile") String asFile) {
        //TODO: poradna implementace, namisto redirectu na api/v5.0
        try {
            checkObjectExists(pid);
            URI uri = new URI(String.format("%s/v5.0/item/%s/full", getApiBaseUrl(), pid));
            return Response.temporaryRedirect(uri).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/image/thumb")
    public Response getImgThumb(@PathParam("pid") String pid) {
        //TODO: poradna implementace, namisto redirectu na api/v5.0
        try {
            checkObjectExists(pid);
            URI uri = new URI(String.format("%s/v5.0/item/%s/thumb", getApiBaseUrl(), pid));
            return Response.temporaryRedirect(uri).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/image/preview")
    public Response getImgPreview(@PathParam("pid") String pid) {
        //TODO: poradna implementace, namisto redirectu na api/v5.0
        try {
            checkObjectExists(pid);
            URI uri = new URI(String.format("%s/v5.0/item/%s/preview", getApiBaseUrl(), pid));
            return Response.temporaryRedirect(uri).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }
    }

    private String getApiBaseUrl() {
        //return "http://localhost:8080/search/api";
        String appUrl = ApplicationURL.applicationURL(this.requestProvider.get());
        return appUrl + "/api";
    }

}
