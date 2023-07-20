package cz.incad.kramerius.rest.apiNew.client.v70;

import com.google.inject.Inject;
import com.google.inject.Provider;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.audio.AudioFormat;
import cz.incad.kramerius.audio.AudioStreamForwardingHelper;
import cz.incad.kramerius.audio.AudioStreamId;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.ExtractStructureHelper;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.repository.utils.Utils;
import cz.incad.kramerius.rest.apiNew.client.v70.epub.EPubFileTypes;
import cz.incad.kramerius.rest.apiNew.client.v70.utils.ProvidedLicensesUtils;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.service.replication.FormatType;
import cz.incad.kramerius.utils.ApplicationURL;
import cz.incad.kramerius.utils.Dom4jUtils;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.java.Pair;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.json.JSONObject;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * @see cz.incad.kramerius.rest.api.k5.client.item.ItemResource
 */
@Path("/client/v7.0/items")
public class ItemsResource extends ClientApiResource {

    //TODO: uklid
    //(ne-admin) client je neautentizovany, jenom cte data a mela by pred nim byt do urcite miry skryta implementece, takze:

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
    // GET      {pid}/info/providedByLicenses              - information about licenses that allow access in current setting (network, user, etc)
    // GET/HEAD {pid}/metadata/mods
    // GET/HEAD {pid}/metadata/dc
    // GET/HEAD {pid}/ocr/text
    // GET/HEAD {pid}/ocr/alto
    // GET/HEAD {pid}/image                                 - obsah IMG_FULL konkrétního objektu
    // GET      {pid}/image/thumb                           - IMG_THUMB objektu nebo potomka
    // GET      {pid}/image/preview                         - IMG_PREVIEW objektu nebo potomka
    // GET      {pid}/image/zoomify/ImageProperties.xml     - ImageProperties.xml pro zoomify
    // GET      {pid}/image/zoomify/{tileGroup}/{tile}.jpg  - dlaždice zoomify
    // GET/HEAD {pid}/audio/mp3
    // GET/HEAD {pid}/audio/ogg
    // GET/HEAD {pid}/audio/wav

    // Specificke endpointy; funguji pouze pro konkretni mimethype 
    // GET/HEAD {pid}/specific/epub
    

    public static final Logger LOGGER = Logger.getLogger(ItemsResource.class.getName());
    /**
     * Serve audio data through proxy from Audio repository, not through Kramerius Repository (Akubra).
     * Byte serving this way is more efficient and better tested.
     */
    private static final boolean AUDIO_SERVE_WITH_FORWARDING = true;
    /**
     * Only relevant with AUDIO_SERVE_WITH_FORWARDING=false
     * Disable byte-serving when audio data is serverd through Kramerius Repository (Akubra).
     * It would be inefficient to use byte-serving this way. Since Kramerius Repository (Akubra) has to fetch whole audio file again for every byte-serving request
     */
    private static final boolean AUDIO_SERVED_BY_AKUBRA_IGNORE_RANGE = true;

    private static final int SEARCH_INDEX_BATCH_SIZE = 98;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    ZoomifyHelper zoomifyHelper;

    @Inject
    AudioStreamForwardingHelper audioHelper;

    /**
     * Because of rights and licenses
     */
    @Inject
    @Named("new-index")
    private SolrAccess solrAccess;

    @Inject
    RightsResolver rightsResolver;
    

    @HEAD
    @Path("{pid}")
    public Response checkItemExists(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/info")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfo(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            JSONObject json = new JSONObject();
            json.put("data", extractAvailableDataInfo(pid));
            
            //json.put("structure", extractStructureInfo(pid));
            json.put("image", extractImageSourceInfo(pid));
            json.put(ProvidedLicensesUtils.PROVIDED_BY_LICENSES, ProvidedLicensesUtils.extractLicensesProvidingAccess(this.rightsResolver, this.solrAccess, pid));
            return Response.ok(json).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/info/data")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfoData(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            return Response.ok(extractAvailableDataInfo(pid)).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/info/providedByLicenses")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getProvidingLicenses(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            JSONObject responseJson = new JSONObject();
            responseJson.put("licenses", ProvidedLicensesUtils.extractLicensesProvidingAccess(this.rightsResolver,this.solrAccess,pid));
            return Response.ok(responseJson).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
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
        try {
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            return Response.ok(ExtractStructureHelper.extractStructureInfo(this.krameriusRepositoryApi, pid)).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
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
        try {
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            return Response.ok(extractImageSourceInfo(pid)).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
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

    

//    private void childrenInSearchIndex(Map<String, Integer> relsExtSort, List<String> childrenPids)
//            throws IOException, UnsupportedEncodingException {
//
//        int numberOfBatches = childrenPids.size() / SEARCH_INDEX_BATCH_SIZE;
//        numberOfBatches += childrenPids.size() % SEARCH_INDEX_BATCH_SIZE == 0 ? 0 : 1;
//        
//        for (int i = 0; i < numberOfBatches; i++) {
//            int start = i*SEARCH_INDEX_BATCH_SIZE;
//            int stop = Math.min( (i+1)*SEARCH_INDEX_BATCH_SIZE, childrenPids.size());
//            
//            List<String> ipids = childrenPids.subList(start, stop);
//
//            String collect = ipids.stream().map(p-> {
//                String retVal = "\"" + p + "\"";
//                return retVal;
//            }).collect(Collectors.joining(" OR "));
//
//            JSONObject solrResponseJson = solrAccess.requestWithSelectReturningJson("q=pid:("+URLEncoder.encode(collect, "UTF-8")+")"+ "&fl="+URLEncoder.encode("pid rels_ext_index.sort","UTF-8"));
//            JSONArray docs = solrResponseJson.getJSONObject("response").getJSONArray("docs");
//            for (int j = 0; j < docs.length(); j++) {
//                JSONObject hit = docs.getJSONObject(j);
//                String hitPid = hit.optString("pid");
//                int hitRelsExt = hit.optInt("rels_ext_index.sort");
//                relsExtSort.put(hitPid, hitRelsExt);
//            }
//        }
//    }

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

    @HEAD
    @Path("{pid}/metadata/mods")
    public Response isMetadataModsAvailable(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.BIBLIO_MODS);
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/metadata/mods")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getMetadataMods(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.BIBLIO_MODS);
            Document mods = krameriusRepositoryApi.getMods(pid, true);
            return Response.ok()
                    .entity(mods.asXML())
                    .build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/metadata/dc")
    public Response isMetadataDublinCoreAvailable(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.BIBLIO_DC);
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/metadata/dc")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getMetadataDublinCore(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.BIBLIO_DC);
            Document dc = krameriusRepositoryApi.getDublinCore(pid, true);
            return Response.ok().entity(dc.asXML()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/ocr/text")
    public Response isOcrTextAvailable(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.OCR_TEXT;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/ocr/text")
    @Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
    public Response getOcrText(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.OCR_TEXT;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            String ocrText = krameriusRepositoryApi.getOcrText(pid);
            return Response.ok().entity(ocrText).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/ocr/alto")
    public Response isOcrAltoAvailable(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.OCR_ALTO;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/ocr/alto")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getDatastreamOcrAlto(@PathParam("pid") String pid) {
        //TODO: pořádně otestovat datastreamy s různými controlgroups (M,E,R) a s odkazy typu URL, path
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.OCR_ALTO;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            Document ocrAlto = krameriusRepositoryApi.getOcrAlto(pid, true);
            return Response.ok().entity(ocrAlto.asXML()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Zkontroluje existenci a právo čtení datastreamu IMG_FULL
     */
    @HEAD
    @Path("{pid}/image")
    public Response isImgFullAvailable(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.IMG_FULL;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /***
     * Vrací obsah datastreamu IMG_FULL tohoto objektu
     * @see cz.incad.Kramerius.imaging.ImageStreamsServlet
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/image")
    public Response getImgFull(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.IMG_FULL;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            String mimeType = krameriusRepositoryApi.getImgFullMimetype(pid);
            InputStream is = krameriusRepositoryApi.getImgFull(pid);
            StreamingOutput stream = output -> {
                IOUtils.copy(is, output);
                IOUtils.closeQuietly(is);
            };
            return Response.ok().entity(stream).type(mimeType).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }
    
    @GET
    @Path("{pid}/foxml")
    @Produces(MediaType.APPLICATION_XML)
    public Response getFoxml(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            //authentication
            // zrusit autentizaci a vratit k
            //checkUserIsAllowedToReadObject(pid); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            checkObjectExists(pid);
            Document foxml = krameriusRepositoryApi.getLowLevelApi().getFoxml(pid);
            
            // remove streams 
            Document modifiedFoxml = removeSecuredDatastreams(foxml);
            if (modifiedFoxml != null) {
                return Response.ok().entity(modifiedFoxml.asXML()).build();
            } else {
                throw new InternalErrorException("I cannot return the foxml object => Not all protected datastreams could be removed.");
            }
            
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    
    private static Document removeSecuredDatastreams(Document foxmlDoc) {
        synchronized(foxmlDoc) {
            List<String> allSecuredStreams = FedoraUtils.getSecuredStreams();
            List<Node> toRemove = new ArrayList<>();
            
            for (String dsId : allSecuredStreams) {
                String xpath = String.format("//foxml:datastream[@ID='%s']", dsId);
                //String xpath = String.format("//foxml:datastream");
                List<Node> streamsEls = Dom4jUtils.buildXpath(xpath).selectNodes(foxmlDoc);
                toRemove.addAll(streamsEls);
            }

            toRemove.stream().forEach(it-> {
                Node detach = it.detach();
                
            });
            
            return foxmlDoc;
        }
    }

    

    /***
     * Vrací zoomify ImageProperties.xml tohoto objektu
     * @see cz.incad.Kramerius.imaging.ZoomifyServlet
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/image/zoomify/ImageProperties.xml")
    public Response getZoomifyImageProperties(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.IMG_FULL;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            return zoomifyHelper.buildImagePropertiesResponse(pid, requestProvider.get());
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /***
     * Vrací zoomify dlaždice obrázku tohoto objektu
     * @see cz.incad.Kramerius.imaging.ZoomifyServlet
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/image/zoomify/{tileGroup}/{tile}")
    public Response getZoomifyTile(@PathParam("pid") String pid, @PathParam("tileGroup") String tileGroupStr, @PathParam("tile") String tileStr) {
        try {
            checkSupportedObjectPid(pid);
            if (!tileGroupStr.matches("TileGroup[0-9]+")) {
                throw new BadRequestException("invalid TileGroup: " + tileGroupStr);
            }
            int tileGroup = Integer.valueOf(tileGroupStr.substring("TileGroup".length()));
            if (!tileStr.matches("[0-9]+-[0-9]+-[0-9]+\\.jpg")) {
                throw new BadRequestException("invalid tile: " + tileStr);
            }
            String[] tileTokens = tileStr.split("\\.")[0].split("-");
            //KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.IMG_FULL;
            //checkObjectAndDatastreamExist(pid, dsId);
            //checkUserByJsessionidIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            checkUserByJsessionidIsAllowedToReadIIPTile(pid);

            return zoomifyHelper.buildTileResponse(pid, requestProvider.get(), tileGroup, Integer.valueOf(tileTokens[0]), Integer.valueOf(tileTokens[1]), Integer.valueOf(tileTokens[2]));
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


    /***
     * Vrací thumbnail buď tohoto objektu, nebo prvního potomka, který má IMG_THUMB
     */
    @GET
    @Path("{pid}/image/thumb")
    public Response getImgThumb(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
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
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
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
            checkSupportedObjectPid(pid);
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
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/audio/mp3")
    public Response isAudioMp3Available(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.AUDIO_MP3;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            if (AUDIO_SERVE_WITH_FORWARDING) {
                HttpServletRequest request = this.requestProvider.get();
                AudioStreamId audioStreamId = new AudioStreamId(pid, AudioFormat.MP3);
                Response.ResponseBuilder builder = Response.ok(); //status code will be replaced
                audioHelper.forwardHttpHEAD(audioStreamId, request, builder);
                return builder.build();
            } else {
                if (AUDIO_SERVED_BY_AKUBRA_IGNORE_RANGE) {
                    return Response.ok().build();
                } else {
                    return Response.ok().header("Accept-Ranges", "bytes").build();
                }
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /***
     * Vrací obsah datastreamu MP3 tohoto objektu
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/audio/mp3")
    public Response getAudioMp3(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.AUDIO_MP3;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            if (AUDIO_SERVE_WITH_FORWARDING) {
                HttpServletRequest request = this.requestProvider.get();
                AudioStreamId audioStreamId = new AudioStreamId(pid, AudioFormat.MP3);
                Response.ResponseBuilder builder = Response.ok(); //status code will be replaced
                audioHelper.forwardHttpGET(audioStreamId, request, builder);
                return builder.build();
            } else {
                String mimeType = krameriusRepositoryApi.getAudioWavMimetype(pid);
                InputStream is = krameriusRepositoryApi.getAudioWav(pid);
                return getAudioDataFromAkubra(mimeType, is, pid);
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private Response getAudioDataFromAkubra(String mimeType, InputStream is, String pid) throws IOException {
        String headerRange = requestProvider.get().getHeader("Range");
        boolean useRange = !AUDIO_SERVED_BY_AKUBRA_IGNORE_RANGE && //not disabled
                headerRange != null && !headerRange.isEmpty() && //Range present
                !"bytes=0-".equals(headerRange) && //Chrome uses this and expects 200 instead of 206
                headerRange.matches("bytes=\\d*-\\d*"); //ignoring different units or <unit>=<range-start>-<range-end>, <range-start>-<range-end>, <range-start>-<range-end>
        if (!useRange) { //request without header Range or header Range ignored
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int totalSize = IOUtils.copy(is, buffer);
            IOUtils.closeQuietly(is);
            byte[] dataComplete = buffer.toByteArray();
            Response.ResponseBuilder resp = Response.ok().entity(dataComplete).type(mimeType)
                    .header("Content-Length", totalSize);
            if (!AUDIO_SERVED_BY_AKUBRA_IGNORE_RANGE) {
                resp.header("Accept-Ranges", "bytes");
            }
            return resp.build();
        } else { //using header Range
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int totalSize = IOUtils.copy(is, buffer);
            IOUtils.closeQuietly(is);
            //this should be cached (in Akubra?), next requests with Range for this resource will very probably follow
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

            Response.ResponseBuilder resp = Response.status(206).entity(dataInRange).type(mimeType)
                    .header("Accept-Ranges", "bytes")
                    .header("Content-Length", totalSize);
            if (!(start == 0 && end == totalSize)) {
                resp.header("Content-Range", String.format("bytes %d-%d/%d", start, end - 1, totalSize));
            }
            return resp.build();
        }
    }

    @HEAD
    @Path("{pid}/audio/ogg")
    public Response isAudioOggAvailable(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.AUDIO_OGG;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            if (AUDIO_SERVE_WITH_FORWARDING) {
                HttpServletRequest request = this.requestProvider.get();
                AudioStreamId audioStreamId = new AudioStreamId(pid, AudioFormat.OGG);
                Response.ResponseBuilder builder = Response.ok(); //status code will be replaced
                audioHelper.forwardHttpHEAD(audioStreamId, request, builder);
                return builder.build();
            } else {
                if (AUDIO_SERVED_BY_AKUBRA_IGNORE_RANGE) {
                    return Response.ok().build();
                } else {
                    return Response.ok().header("Accept-Ranges", "bytes").build();
                }
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /***
     * Vrací obsah datastreamu OGG tohoto objektu
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/audio/ogg")
    public Response getAudioOgg(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.AUDIO_OGG;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            if (AUDIO_SERVE_WITH_FORWARDING) {
                HttpServletRequest request = this.requestProvider.get();
                AudioStreamId audioStreamId = new AudioStreamId(pid, AudioFormat.OGG);
                Response.ResponseBuilder builder = Response.ok(); //status code will be replaced
                audioHelper.forwardHttpGET(audioStreamId, request, builder);
                return builder.build();
            } else {
                String mimeType = krameriusRepositoryApi.getAudioWavMimetype(pid);
                InputStream is = krameriusRepositoryApi.getAudioWav(pid);
                return getAudioDataFromAkubra(mimeType, is, pid);
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/audio/wav")
    public Response isAudioWavAvailable(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.AUDIO_WAV;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            if (AUDIO_SERVE_WITH_FORWARDING) {
                HttpServletRequest request = this.requestProvider.get();
                AudioStreamId audioStreamId = new AudioStreamId(pid, AudioFormat.WAV);
                Response.ResponseBuilder builder = Response.ok(); //status code will be replaced
                audioHelper.forwardHttpHEAD(audioStreamId, request, builder);
                return builder.build();
            } else {
                if (AUDIO_SERVED_BY_AKUBRA_IGNORE_RANGE) {
                    return Response.ok().build();
                } else {
                    return Response.ok().header("Accept-Ranges", "bytes").build();
                }
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /***
     * Vrací obsah datastreamu WAV tohoto objektu
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/audio/wav")
    public Response getAudioWav(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.AUDIO_WAV;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            if (AUDIO_SERVE_WITH_FORWARDING) {
                HttpServletRequest request = this.requestProvider.get();
                AudioStreamId audioStreamId = new AudioStreamId(pid, AudioFormat.WAV);
                Response.ResponseBuilder builder = Response.ok(); //status code will be replaced
                audioHelper.forwardHttpGET(audioStreamId, request, builder);
                return builder.build();
            } else {
                String mimeType = krameriusRepositoryApi.getAudioWavMimetype(pid);
                InputStream is = krameriusRepositoryApi.getAudioWav(pid);
                return getAudioDataFromAkubra(mimeType, is, pid);
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    
    // =========== EPub specific endpoints

    @HEAD
    @Path("{pid}/epub")
    public Response isEpubAvailable(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.IMG_FULL;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); 
            checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.IMG_FULL);

            boolean epub = isEpubMimeType(pid, dsId);
            if (epub) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


    private boolean isEpubMimeType(String pid, KrameriusRepositoryApi.KnownDatastreams dsId)
            throws RepositoryException, IOException {
        String datastreamMimetype = krameriusRepositoryApi.getLowLevelApi().getDatastreamMimetype(pid, dsId.name());
        boolean epub = datastreamMimetype != null  && datastreamMimetype.equals(ImageMimeType.EPUB.getValue());
        return epub;
    }

    @GET
    @Path("{pid}/epub/{path: .*}")
    public Response getPaths(@PathParam("pid") String pid, @PathParam("path") PathSegment pathSegment,@Context UriInfo info ) {
        try {
            List<PathSegment> segments = info.getPathSegments();
            List<String> paths=  segments.stream().map(PathSegment::getPath).collect(Collectors.toList());
            int indexOf = paths.indexOf("epub");
            List<String> zipPath = paths.subList(indexOf+1, paths.size());
            return getEpubInternalPart(pid, zipPath);
            
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    
    
    private Response getEpubInternalPart(String pid, List<String> paths) {
        try {
            String path = paths.stream().collect(Collectors.joining("/"));
            LOGGER.fine("Reading zip path "+path);
            
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.IMG_FULL;
            checkObjectAndDatastreamExist(pid, dsId);
            checkUserIsAllowedToReadDatastream(pid, dsId); 
            checkObjectAndDatastreamExist(pid, KrameriusRepositoryApi.KnownDatastreams.IMG_FULL);

            boolean epub = isEpubMimeType(pid, dsId);
            if (epub) {
                InputStream is = krameriusRepositoryApi.getLowLevelApi().getLatestVersionOfDatastream(pid, dsId.name());
                
                ZipInputStream zipInputStream = new ZipInputStream(is);
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    if (entry.getName().equals(path)) {
                        break; 
                    }
                }
                
                if (entry != null) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                        bos.write(buffer,0, bytesRead);
                    }
                    byte[] bytes = bos.toByteArray();
                    return copyStreams(path,  bytes);
                    
                } else {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private Response copyStreams(String path, byte[] bytes) {
        StreamingOutput stream = new StreamingOutput() {
            public void write(OutputStream output)
                    throws IOException, WebApplicationException {
                try {
                    cz.incad.kramerius.utils.IOUtils.copyStreams(new ByteArrayInputStream(bytes), output);
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                }
            }
        };
        return Response.ok()
                .entity(stream)
                .type(EPubFileTypes.findMimetype(path))
                .header("Content-Length", bytes.length).build();
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

    
    
    

}
