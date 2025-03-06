package cz.incad.kramerius.rest.apiNew.client.v70;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.client.Client;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.audio.AudioFormat;
import cz.incad.kramerius.audio.AudioStreamForwardingHelper;
import cz.incad.kramerius.audio.AudioStreamId;
import cz.incad.kramerius.rest.apiNew.admin.v70.collections.CutItem;
import cz.incad.kramerius.rest.apiNew.client.v70.epub.EPubFileTypes;
import cz.incad.kramerius.rest.apiNew.client.v70.utils.RightRuntimeInformations;
import cz.incad.kramerius.rest.apiNew.client.v70.utils.RightRuntimeInformations.RuntimeInformation;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;
import cz.incad.kramerius.rest.apiNew.monitoring.APICallMonitor;
import cz.incad.kramerius.rest.apiNew.monitoring.ApiCallEvent;
import cz.incad.kramerius.rest.utils.IIIFUtils;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.licenses.LicensesManager;
import cz.incad.kramerius.statistics.accesslogs.AggregatedAccessLogs;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.RESTHelper;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.XMLUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.imgs.ImageMimeType;
import cz.incad.kramerius.utils.imgs.KrameriusImageSupport;
import cz.incad.kramerius.utils.java.Pair;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.async.HttpAsyncClient;
import org.ceskaexpedice.akubra.KnownDatastreams;
import org.ceskaexpedice.akubra.RepositoryException;
import org.ceskaexpedice.akubra.utils.Dom4jUtils;
import org.ceskaexpedice.akubra.utils.RelsExtStructureInfoUtils;
import org.codehaus.jettison.json.JSONArray;
import org.dom4j.*;
import org.json.JSONException;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.inject.Named;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * ItemsResource
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
    // GET      {pid}/info/providedByiLicenses              - information about licenses that allow access in current setting (network, user, etc)
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

    // pouze  vycet vsech streamu 
    // GET      {pid}/introspect - pouze metadatove streamy 

    // Specificke endpointy; funguji pouze pro konkretni mimethype 
    // GET/HEAD {pid}/specific/epub
    

    //private static final int MAX_TIME_SIZE = KConfiguration.getInstance().getConfiguration().getInt("iiif.tile.maxsize",512);
    
    
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
    Provider<HttpServletResponse> responseProvider;

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
    
    @Inject
    AggregatedAccessLogs accessLog;
    
    @Inject
    LicensesManager licensesManager;
    
    @Inject
    protected transient HttpAsyncClient client;

    @Inject
    APICallMonitor apiCallMonitor;

    public ItemsResource() {
        super();
    }

    public static Map<String, String> IIIF_SUPPORTED_MIMETYPES = new HashMap<>();
    static  {
        ItemsResource.IIIF_SUPPORTED_MIMETYPES.put("jpg", "image/jpeg");
        ItemsResource.IIIF_SUPPORTED_MIMETYPES.put("tif", "image/tiff");
        ItemsResource.IIIF_SUPPORTED_MIMETYPES.put("png", "image/png");
        ItemsResource.IIIF_SUPPORTED_MIMETYPES.put("jp2", "image/jp2");
        ItemsResource.IIIF_SUPPORTED_MIMETYPES.put("pdf", "application/pdf");
        ItemsResource.IIIF_SUPPORTED_MIMETYPES.put("webp", "image/webp");
    }


    @HEAD
    @Path("{pid}")
    public Response checkItemExists(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s",pid), "", "HEAD", pid);
        try {
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }

    @GET
    @Path("{pid}/info")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfo(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/info",pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            JSONObject json = new JSONObject();
            json.put("data", extractAvailableDataInfo(pid));
            
            //json.put("structure", extractStructureInfo(pid));
            json.put("image", extractImageSourceInfo(pid));
            
            
            RuntimeInformation extracrtedInformation = RightRuntimeInformations.extractInformations(this.rightsResolver, this.solrAccess, pid);
            
            json.put(RightRuntimeInformations.PROVIDED_BY_LICENSES, extracrtedInformation.getProvidingLicensesAsJSONArray());
            json.put(RightRuntimeInformations.ACCESSIBLE_LOCSK, extracrtedInformation.getLockAsJSONArray());
                       
            
            return Response.ok(json).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }

    @GET
    @Path("{pid}/info/data")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfoData(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/info/data", pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            return Response.ok(extractAvailableDataInfo(pid)).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }

    @GET
    @Path("{pid}/info/providedByLicenses")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getProvidingLicenses(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/info/providedByLicenses", pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            JSONObject responseJson = new JSONObject();
            RuntimeInformation extracrtedInformation = RightRuntimeInformations.extractInformations(this.rightsResolver, this.solrAccess, pid);
            responseJson.put("licenses", extracrtedInformation.getLockAsJSONArray());
            return Response.ok(responseJson).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
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
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/info/structure",pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            return Response.ok(RelsExtStructureInfoUtils.extractStructureInfo(this.akubraRepository, pid)).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
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
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/info/image",pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            checkObjectExists(pid);
            return Response.ok(extractImageSourceInfo(pid)).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }

    private JSONObject extractAvailableDataInfo(String pid) {
        JSONObject dataAvailable = new JSONObject();
        //metadata
        JSONObject metadata = new JSONObject();
        metadata.put("mods", akubraRepository.datastreamExists(pid, KnownDatastreams.BIBLIO_MODS));
        metadata.put("dc", akubraRepository.datastreamExists(pid, KnownDatastreams.BIBLIO_DC));
        dataAvailable.put("metadata", metadata);
        JSONObject ocr = new JSONObject();
        //ocr
        ocr.put("text", akubraRepository.datastreamExists(pid, KnownDatastreams.OCR_TEXT));
        ocr.put("alto", akubraRepository.datastreamExists(pid, KnownDatastreams.OCR_ALTO));
        dataAvailable.put("ocr", ocr);
        //images
        JSONObject image = new JSONObject();
        image.put("full", akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL));
        image.put("thumb", akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_THUMB));
        image.put("preview", akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_PREVIEW));
        dataAvailable.put("image", image);
        //audio
        JSONObject audio = new JSONObject();
        audio.put("mp3", akubraRepository.datastreamExists(pid, KnownDatastreams.AUDIO_MP3));
        audio.put("ogg", akubraRepository.datastreamExists(pid, KnownDatastreams.AUDIO_OGG));
        audio.put("wav", akubraRepository.datastreamExists(pid, KnownDatastreams.AUDIO_WAV));
        dataAvailable.put("audio", audio);
        return dataAvailable;
    }

    static boolean isChacheDirDisabledAndFromCache(boolean chacheDirDisable, String tilesUrl) {
        return chacheDirDisable && "kramerius4://deepZoomCache".equals(tilesUrl);
    }


    private Object extractImageSourceInfo(String pid) {
        JSONObject json = new JSONObject();
        Document relsExt = akubraRepository.re().get(pid).asDom4j(false);
        String tilesUrl = org.ceskaexpedice.akubra.utils.Dom4jUtils.stringOrNullFromFirstElementByXpath(relsExt.getRootElement(), "//tiles-url");
        boolean chacheDirDisable = KConfiguration.getInstance().getConfiguration().getBoolean("deepZoom.cachedir.disable", false);
        if (tilesUrl != null && (!isChacheDirDisabledAndFromCache(chacheDirDisable, tilesUrl))) {
            json.put("type", "tiles");
        } else if (!akubraRepository.datastreamExists(pid, KnownDatastreams.IMG_FULL)) {
            json.put("type", "none");
        } else {
            String imgFullMimetype = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.IMG_FULL).getMimetype();
            if (imgFullMimetype == null) {
                json.put("type", "none");
            } else {
                if (ImageMimeType.JPEG2000.getValue().equals(imgFullMimetype)) {
                    // convert to jpeg 
                    json.put("type", ImageMimeType.JPEG.getValue());
                } else  if (ImageMimeType.DJVU.getValue().equals(imgFullMimetype) || ImageMimeType.VNDDJVU.getValue().equals(imgFullMimetype) || ImageMimeType.XDJVU.getValue().equals(imgFullMimetype)) {
                    json.put("type", ImageMimeType.JPEG.getValue());
                } else {
                    // transform jp2 or djvu
                    //jpeg, pdf, etc.
                    json.put("type", imgFullMimetype);
                }
            }
        }
        return json;
    }

    @HEAD
    @Path("{pid}/metadata/mods")
    public Response isMetadataModsAvailable(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/metadata/mods",pid), "", "HEAD", pid);
        try {
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, KnownDatastreams.BIBLIO_MODS.toString());
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }

    @GET
    @Path("{pid}/metadata/mods")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getMetadataMods(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/metadata/mods",pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, KnownDatastreams.BIBLIO_MODS.toString());
            Document mods = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_MODS).asDom4j(true);
            return Response.ok()
                    .entity(mods.asXML())
                    .build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }

    @HEAD
    @Path("{pid}/metadata/dc")
    public Response isMetadataDublinCoreAvailable(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/metadata/dc",pid), "", "HEAD", pid);
        try {
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, KnownDatastreams.BIBLIO_DC.toString());
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }

    @GET
    @Path("{pid}/metadata/dc")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getMetadataDublinCore(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/metadata/dc",pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            checkObjectAndDatastreamExist(pid, KnownDatastreams.BIBLIO_DC.toString());
            Document dc = akubraRepository.getDatastreamContent(pid, KnownDatastreams.BIBLIO_DC).asDom4j(true);
            return Response.ok().entity(dc.asXML()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }

    @HEAD
    @Path("{pid}/ocr/text")
    public Response isOcrTextAvailable(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/ocr/text", pid), "", "HEAD", pid);
        try {
            checkSupportedObjectPid(pid);
            KnownDatastreams dsId = KnownDatastreams.OCR_TEXT;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }

    @GET
    @Path("{pid}/ocr/text")
    @Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
    public Response getOcrText(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/ocr/text", pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            KnownDatastreams dsId = KnownDatastreams.OCR_TEXT;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            String ocrText = akubraRepository.getDatastreamContent(pid, dsId.toString()).asString();
            return Response.ok().entity(ocrText).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }

    @HEAD
    @Path("{pid}/ocr/alto")
    public Response isOcrAltoAvailable(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/ocr/alto", pid), "", "HEAD", pid);
        try {
            checkSupportedObjectPid(pid);
            KnownDatastreams dsId = KnownDatastreams.OCR_ALTO;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }

    @GET
    @Path("{pid}/ocr/alto")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getDatastreamOcrAlto(@PathParam("pid") String pid) {
        //TODO: pořádně otestovat datastreamy s různými controlgroups (M,E,R) a s odkazy typu URL, path
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/ocr/alto", pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            KnownDatastreams dsId = KnownDatastreams.OCR_ALTO;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            Document ocrAlto = akubraRepository.getDatastreamContent(pid, KnownDatastreams.OCR_ALTO).asDom4j(true);
            return Response.ok().entity(ocrAlto.asXML()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }

    /**
     * Zkontroluje existenci a právo čtení datastreamu IMG_FULL
     */
    @HEAD
    @Path("{pid}/image")
    public Response isImgFullAvailable(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/image",  pid), "", "HEAD", pid);
        try {
            checkSupportedObjectPid(pid);
            KnownDatastreams dsId = KnownDatastreams.IMG_FULL;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
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
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/image",  pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            KnownDatastreams dsId = KnownDatastreams.IMG_FULL;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            String mimeType = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.IMG_FULL).getMimetype();
            if (ImageMimeType.JPEG2000.getValue().equals(mimeType)) {
                InputStream istream = akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_FULL).asInputStream();
                ImageIO.setUseCache(true);
                BufferedImage jpeg2000 = ImageIO.read(istream);
                StreamingOutput stream = output -> {
                  ImageIO.write(jpeg2000, "jpg", output);
                };
                return Response.ok().entity(stream).type(mimeType).build();
            } else  if (ImageMimeType.DJVU.getValue().equals(mimeType) || ImageMimeType.VNDDJVU.getValue().equals(mimeType) || ImageMimeType.XDJVU.getValue().equals(mimeType) ) {
                File tmpFile = File.createTempFile("djvu", "djvu");
                InputStream istream = akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_FULL).asInputStream();
                Files.copy(istream, tmpFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                BufferedImage djvu = KrameriusImageSupport.readImage(tmpFile.toURI().toURL(), ImageMimeType.DJVU, 0);
                StreamingOutput stream = output -> {
                    ImageIO.write(djvu, "jpeg", output);
                };
                return Response.ok().entity(stream).type(mimeType).build();
            } else {
                InputStream is = akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_FULL).asInputStream();
                StreamingOutput stream = output -> {
                    IOUtils.copy(is, output);
                    IOUtils.closeQuietly(is);
                };
                return Response.ok().entity(stream).type(mimeType).build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            if (event != null) {
                this.apiCallMonitor.stop(event, userProvider.get().getLoginname());
            }
        }
    }
    
    @GET
    @Path("{pid}/foxml")
    @Produces(MediaType.APPLICATION_XML)
    public Response getFoxml(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            //checkUserIsAllowedToReadObject(pid); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            checkObjectExists(pid);
            Document foxml = akubraRepository.get(pid).asDom4j(true);
            // remove streams
            Document modifiedFoxml = removeSecuredDatastreams(foxml);
            if (modifiedFoxml != null) {
                String model = model(modifiedFoxml);
                if (StringUtils.isAnyString(model) && model.equals("info:fedora/model:collection")) {
                    String streamName = "COLLECTION_CLIPS";
                    String collectionClipsContent = null;
                    // clipping_items
                    try(InputStream cutters = akubraRepository.getDatastreamContent(pid, streamName).asInputStream()) {
                        if (cutters != null) {
                            byte[] content = replaceLocationByBinaryContent(modifiedFoxml, cutters, streamName);
                            collectionClipsContent = new String(content, "UTF-8");
                        }
                    }
                    // thumbs from cutters
                    if (collectionClipsContent!= null) {
                        org.json.JSONArray jsonArray = new org.json.JSONArray(collectionClipsContent);
                        
                        List<CutItem> cutItems = CutItem.fromJSONArray(jsonArray);
                        for (CutItem cutItem : cutItems) {
                            try {
                                cutItem.initGeneratedThumbnail(akubraRepository, pid);
                            } catch (NoSuchAlgorithmException | IOException e) {
                                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                            }
                            
                            if (cutItem.containsGeneratedThumbnail()) {
                                String clipThumbName = cutItem.getThumbnailmd5();
                                // clipThumbName
                                try(InputStream cutters = akubraRepository.getDatastreamContent(pid, clipThumbName).asInputStream()) {
                                    if (cutters != null) {
                                       replaceLocationByBinaryContent(modifiedFoxml, cutters, clipThumbName);
                                    }
                                }
                                
                            }
                        }
                    }
                    // thumb
                    try(InputStream imgThumb = akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_THUMB).asInputStream()) {
                        if (imgThumb != null) {
                            String streamThumbName = "IMG_THUMB";
                            replaceLocationByBinaryContent(modifiedFoxml, imgThumb, streamThumbName);
                        }
                    }
                }
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

    private byte[] replaceLocationByBinaryContent(Document modifiedFoxml, InputStream imgThumb, String streamName)
            throws IOException {
        String datastreamXPath = String.format("/foxml:digitalObject/foxml:datastream[@ID='%s']/foxml:datastreamVersion", streamName);
        Node thumbNode = Dom4jUtils.buildXpath(datastreamXPath).selectSingleNode(modifiedFoxml.getRootElement());

        byte[] bytes = IOUtils.toByteArray(imgThumb);
        if (thumbNode != null) {
            List<Node> contentLocation = Dom4jUtils.buildXpath("//foxml:contentLocation").selectNodes(thumbNode);
            for (Node node : contentLocation) { node.detach(); }
            Element thumbElement = (Element) thumbNode;
            Element binaryContent = thumbElement.addElement("binaryContent", thumbElement.getNamespaceURI());
            binaryContent.setText(new String(Base64.encodeBase64(bytes)));
        }
        return bytes;
    }

    private String model(Document modifiedFoxml) {
        Element modelNode = (Element) Dom4jUtils.buildXpath("//model:hasModel").selectSingleNode(modifiedFoxml.getRootElement());
        if (modelNode != null) {
            Namespace ns = new Namespace("rdf", Dom4jUtils.NAMESPACE_URIS.get("rdf"));
            QName qname = new QName("resource", ns);
            Attribute attribute = modelNode.attribute(qname);
            if (attribute != null) {
                return attribute.getStringValue();
            }
        }
        return null;
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
    public void getZoomifyImageProperties(@PathParam("pid") String pid /*@Context HttpServletResponse resp*/) {
        try {
            checkSupportedObjectPid(pid);
            KnownDatastreams dsId = KnownDatastreams.IMG_FULL;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            RequestDispatcher requestDispatcher = this.requestProvider.get().getRequestDispatcher(String.format("/zoomify/%s/ImageProperties.xml", pid));
            requestDispatcher.forward(this.requestProvider.get(), this.responseProvider.get());
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
    public void getZoomifyTile(@PathParam("pid") String pid, @PathParam("tileGroup") String tileGroupStr, @PathParam("tile") String tileStr /*@Context HttpServletResponse resp*/) {
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

            RequestDispatcher requestDispatcher = this.requestProvider.get().getRequestDispatcher(String.format("/zoomify/%s/%s/%s", pid, tileGroupStr, tileStr));
            requestDispatcher.forward(this.requestProvider.get(), this.responseProvider.get());

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
            KnownDatastreams dsId = KnownDatastreams.AUDIO_MP3;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            if (shouldUseAudioServer(pid, AudioFormat.MP3)) {
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

    private boolean shouldUseAudioServer(String pid, AudioFormat audioFormat) {
        try {
            String type = akubraRepository.getDatastreamMetadata(pid, audioFormat.name()).getType().toString();
            return type != null && type.equals("INDIRECT");
        } catch (RepositoryException e) {
            LOGGER.log(Level.SEVERE,e.getMessage(),e);
            return false;
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
            KnownDatastreams dsId = KnownDatastreams.AUDIO_MP3;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            if (shouldUseAudioServer(pid, AudioFormat.MP3)) {
                HttpServletRequest request = this.requestProvider.get();
                AudioStreamId audioStreamId = new AudioStreamId(pid, AudioFormat.MP3);
                Response.ResponseBuilder builder = Response.ok(); //status code will be replaced
                audioHelper.forwardHttpGET(audioStreamId, request, builder);
                return builder.build();
            } else {
                String mimeType = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.AUDIO_MP3).getMimetype();
                InputStream is = akubraRepository.getDatastreamContent(pid, KnownDatastreams.AUDIO_MP3).asInputStream();
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
            KnownDatastreams dsId = KnownDatastreams.AUDIO_OGG;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            if (shouldUseAudioServer(pid, AudioFormat.OGG)) {
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
            KnownDatastreams dsId = KnownDatastreams.AUDIO_OGG;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            if (shouldUseAudioServer(pid, AudioFormat.OGG)) {
                HttpServletRequest request = this.requestProvider.get();
                AudioStreamId audioStreamId = new AudioStreamId(pid, AudioFormat.OGG);
                Response.ResponseBuilder builder = Response.ok(); //status code will be replaced
                audioHelper.forwardHttpGET(audioStreamId, request, builder);
                return builder.build();
            } else {
                String mimeType = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.AUDIO_OGG).getMimetype();
                InputStream is = akubraRepository.getDatastreamContent(pid, KnownDatastreams.AUDIO_OGG).asInputStream();
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
            KnownDatastreams dsId = KnownDatastreams.AUDIO_WAV;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            if (shouldUseAudioServer(pid, AudioFormat.WAV)) {
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
            KnownDatastreams dsId = KnownDatastreams.AUDIO_WAV;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); //autorizace podle zdroje přístupu, POLICY apod. (by JSESSIONID)
            if (shouldUseAudioServer(pid, AudioFormat.WAV)) {
                HttpServletRequest request = this.requestProvider.get();
                AudioStreamId audioStreamId = new AudioStreamId(pid, AudioFormat.WAV);
                Response.ResponseBuilder builder = Response.ok(); //status code will be replaced
                audioHelper.forwardHttpGET(audioStreamId, request, builder);
                return builder.build();
            } else {
                String mimeType = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.AUDIO_WAV).getMimetype();
                InputStream is = akubraRepository.getDatastreamContent(pid, KnownDatastreams.AUDIO_WAV).asInputStream();
                return getAudioDataFromAkubra(mimeType, is, pid);
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    //@Path("{pid}/image/zoomify/ImageProperties.xml")


    @GET
    //@Path("iiif/{pid}/info.json")
    @Path("{pid}/image/iiif/info.json")
    @Produces("application/ld+json")
    public Response iiiFManifest(@PathParam("pid") String pid) {
        try {
            pid = URLDecoder.decode(pid, "UTF-8");
            checkUserIsAllowedToReadObject(pid); 
            reportAccess( pid, null);
            org.w3c.dom.Document relsExt = akubraRepository.re().get(pid).asDom(true);
            String u = IIIFUtils.iiifImageEndpoint(relsExt);
            if (u != null) {
                if (!u.endsWith("/")) { u = u+"/"; }
                u = u +"info.json";

                HttpURLConnection con = (HttpURLConnection) RESTHelper.openConnection(u, "", "");
                InputStream inputStream = con.getInputStream();
                String json = IOUtils.toString(inputStream, Charset.defaultCharset());
                JSONObject object = new JSONObject(json);
                HttpServletRequest req = this.requestProvider.get();
                String urlRequest = req.getRequestURL().toString();
                object.put("@id", urlRequest.substring(0, urlRequest.lastIndexOf('/')));
                
                return Response.ok().entity(object.toString()).build();

            } else {
                throw new BadRequestException("bad request");
            }
        } catch (JSONException | RepositoryException | IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }
    
    
    @GET
    @Produces("image/jpeg")
    @Path("{pid}/image/iiif/{region}/{size}/{rotation}/{qualityformat}")
    public void tile(
            @PathParam("pid") String pid, 
            @PathParam("region") String region, 
            @PathParam("size") String size,
            @PathParam("rotation") String rotation,
            @PathParam("qualityformat") String qf
            /*,@Context HttpServletResponse response*/) throws IOException {
        try {
            
            if (region.toLowerCase().trim().equals("full") || region.toLowerCase().trim().equals("square") || region.toLowerCase().trim().contains("pct:")) {
                checkUserIsAllowedToReadObject(pid);
            } else {
                if (size.toLowerCase().trim().contains("max") || size.toLowerCase().trim().contains("pct:")) {
                    checkUserIsAllowedToReadObject(pid);
                } else {
                    checkIIIFSize(pid, size);
                }
            }
            org.w3c.dom.Document relsExt = akubraRepository.re().get(pid).asDom(true);
            String u = IIIFUtils.iiifImageEndpoint(relsExt);
            if(u != null) {
                // size can contain ^ or ! 
                if (size.contains("^") || size.contains("!")) {
                    size = URLEncoder.encode(size,"UTF-8");
                }
                String defaultMime = IIIF_SUPPORTED_MIMETYPES.get("jpg");

                StringBuilder url = new StringBuilder(u);
                if (!u.endsWith("/")) { url.append("/"); }
                
                url.append(String.format("%s/%s/%s/%s", region, size, rotation,qf));
     
                String mime = defaultMime;
                String[] splited = qf.split("\\.");
                if (splited.length > 1) {
                    mime =  IIIF_SUPPORTED_MIMETYPES.containsKey(splited[1]) ? IIIF_SUPPORTED_MIMETYPES.get(splited[1]) :  defaultMime;
                }
                LOGGER.fine(String.format("Copy tile from IIIF server %s", url.toString()));
                
                RequestDispatcher requestDispatcher = this.requestProvider.get().getRequestDispatcher(String.format("/iiif/%s/%s/%s/%s/%s", pid, region, size, rotation, qf));
                requestDispatcher.forward(this.requestProvider.get(), this.responseProvider.get());
                
           } else {
               throw new BadRequestException("bad request");
           }

        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }
    
    private void checkIIIFSize(String pid, String size) {
     
        org.apache.commons.lang3.tuple.Pair<String, String> iiifValues = iiifValues(size);
        if (iiifValues != null) {
            int maxSize = KConfiguration.getInstance().getConfiguration().getInt("iiif.tile.maxsize",512);
            
            int width = StringUtils.isAnyString(iiifValues.getLeft())  ? Integer.parseInt(iiifValues.getLeft()) : 0;
            int height = StringUtils.isAnyString(iiifValues.getRight()) ? Integer.parseInt(iiifValues.getRight()) :0;

            if (width > maxSize || height > maxSize) {
                checkUserIsAllowedToReadObject(pid);
            }
        }
    }

    static String iiifVal(String val) {
        String retval = val;
        if (retval.contains("^")) {
            retval = retval.replace("^","");
        }
        
        if (retval.contains("!")) {
            retval = retval.replace("!","");
        }
        return retval;
    }
    
    static org.apache.commons.lang3.tuple.Pair<String,String> iiifValues(String val) {
        String[] split = val.split(",");
        if (split.length >=2) {

            String firstVal = split[0];
            String secondVal = split[1];
            
            firstVal = ItemsResource.iiifVal(firstVal);
            secondVal = ItemsResource.iiifVal(secondVal);
            return org.apache.commons.lang3.tuple.Pair.of(firstVal, secondVal);
            
        } else  if (split.length >= 1) {

            String firstVal = split[0];
            firstVal = ItemsResource.iiifVal(firstVal);

            return org.apache.commons.lang3.tuple.Pair.of(firstVal, null);
        }
        return null;
      }


    @GET
    @Path("{pid}/introspect")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response introspect(@PathParam("pid") String pid) {
        try {
            List<String> knownDataStreams = Arrays.stream(KnownDatastreams.values()).map(KnownDatastreams::toString).collect(Collectors.toList());
            JSONArray result = new JSONArray();
            List<String> datastreamNames = akubraRepository.getDatastreamNames(pid);
            for (String dataStreamName : datastreamNames) {
                String mimeType = akubraRepository.getDatastreamMetadata(pid, dataStreamName).getMimetype();
                boolean knownDatastream = knownDataStreams.contains(dataStreamName);
                JSONObject val = new JSONObject();
                val.put("name", dataStreamName);
                val.put("mimetype", mimeType);
                val.put("knowndatastream", knownDatastream);
                result.put(val);
            }
            return Response.ok().entity(result.toString()).build(); 
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }
    
    @GET
    @Path("{pid}/introspect/{data}")
    public Response introspect(@PathParam("pid") String pid,@PathParam("data") String data) {
        try {
            List<String> knownDataStreams = Arrays.stream(KnownDatastreams.values()).map(KnownDatastreams::toString).collect(Collectors.toList());
            boolean knownDatastream = knownDataStreams.contains(data);
            if (knownDatastream) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            } else {
                String mimeType = akubraRepository.getDatastreamMetadata(pid,data).getMimetype();
                InputStream dataStream = akubraRepository.getDatastreamContent(pid, data).asInputStream();
                StreamingOutput stream = output -> {
                    IOUtils.copy(dataStream, output);
                    IOUtils.closeQuietly(dataStream);
                };
                return Response.ok().entity(stream).type(mimeType).build();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private void reportAccess(String pid, String streamName) {
        try {
            if (this.accessLog != null) {
                this.accessLog.reportAccess(pid, streamName);
            } else {
                this.accessLog.reportAccess(pid, null);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Can't write statistic records for " + pid + ", stream name: " + streamName, e);
        }
    }
    
    // =========== Collection specific endpoints

    @GET
    @Path("{pid}/collection/cuttings")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getCollectionClips(@PathParam("pid") String pid) {
            try {
                checkSupportedObjectPid(pid);
                checkObjectExists(pid);
                if(!akubraRepository.datastreamExists(pid, "COLLECTION_CLIPS")) {
                    throw new NotFoundException();
                } else {
                    String mimetype = akubraRepository.getDatastreamMetadata(pid, "COLLECTION_CLIPS").getMimetype();
                    org.json.JSONArray outputValue = new org.json.JSONArray();
                    try(InputStream istream = akubraRepository.getDatastreamContent(pid, "COLLECTION_CLIPS").asInputStream()) {
                        org.json.JSONArray inputValue = new org.json.JSONArray(IOUtils.toString(istream, "UTF-8"));

                        List<CutItem> cutItems = CutItem.fromJSONArray(inputValue);
                        cutItems.forEach(cl-> {
                            try {
                                cl.initGeneratedThumbnail(akubraRepository, pid);
                            } catch (NoSuchAlgorithmException | IOException e) {
                                LOGGER.log(Level.SEVERE,e.getMessage(),e);
                            }
                            outputValue.put(cl.toJSON());
                        });
                    }
                    return Response.ok().entity(outputValue.toString()).type(mimetype).build();
                }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/collection/cuttings/image/{thumb_id}")
    public Response getCollectionThumb(@PathParam("pid") String pid, @PathParam("thumb_id") String thumbId) {
            try {
                checkSupportedObjectPid(pid);
                checkObjectExists(pid);
                if(!akubraRepository.datastreamExists(pid, thumbId)) {
                    throw new NotFoundException("no image/thumb %s  available for object %s ", thumbId, pid);
                } else {
                    String mimetype = akubraRepository.getDatastreamMetadata(pid, thumbId).getMimetype();
                    InputStream istream = akubraRepository.getDatastreamContent(pid, thumbId).asInputStream();
                    StreamingOutput stream = output -> {
                        IOUtils.copy(istream, output);
                        IOUtils.closeQuietly(istream);
                    };
                    return Response.ok().entity(stream).type(mimetype).build();
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
            KnownDatastreams dsId = KnownDatastreams.IMG_FULL;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); 
            checkObjectAndDatastreamExist(pid, KnownDatastreams.IMG_FULL.toString());

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

    private boolean isEpubMimeType(String pid, KnownDatastreams dsId){
        String datastreamMimetype = akubraRepository.getDatastreamMetadata(pid, dsId.name()).getMimetype();
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
            KnownDatastreams dsId = KnownDatastreams.IMG_FULL;
            checkObjectAndDatastreamExist(pid, dsId.toString());
            checkUserIsAllowedToReadDatastream(pid, dsId); 
            checkObjectAndDatastreamExist(pid, KnownDatastreams.IMG_FULL.toString());

            boolean epub = isEpubMimeType(pid, dsId);
            if (epub) {
                InputStream is = akubraRepository.getDatastreamContent(pid, dsId.name()).asInputStream();
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


    Pair<InputStream, String> getFirstAvailableImgFull(String pid) throws IOException {
        InputStream is = akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_FULL).asInputStream();
        if (is != null) {
            String mimeType = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.IMG_FULL).getMimetype();
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

    Pair<InputStream, String> getFirstAvailableImgThumb(String pid) throws IOException {
        InputStream is = akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_THUMB).asInputStream();
        if (is != null) {
            String mimeType = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.IMG_THUMB).getMimetype();
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

    Pair<InputStream, String> getFirstAvailableImgPreview(String pid) throws IOException {
        InputStream is = akubraRepository.getDatastreamContent(pid, KnownDatastreams.IMG_PREVIEW).asInputStream();
        if (is != null) {
            String mimeType = akubraRepository.getDatastreamMetadata(pid, KnownDatastreams.IMG_PREVIEW).getMimetype();
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

    private String getPidOfFirstChild(String pid) {
        Document relsExt = akubraRepository.re().get(pid).asDom4j(false);
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
