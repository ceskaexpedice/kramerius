package cz.incad.kramerius.rest.apiNew.client.v70.cdk;

import static cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler.RequestMethodName.get;
import static cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler.RequestMethodName.head;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jersey.api.client.Client;

import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.audio.AudioStreamForwardingHelper;
import cz.incad.kramerius.fedora.utils.CDKUtils;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.OneInstance;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler;
import cz.incad.kramerius.rest.apiNew.client.v70.redirection.item.ProxyItemHandler.RequestMethodName;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.utils.pid.LexerException;
import cz.incad.kramerius.rest.apiNew.client.v70.ClientApiResource;
import cz.incad.kramerius.rest.apiNew.client.v70.ZoomifyHelper;


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

    //public static final String API_V7 = "v7";

    
    @Inject
    Provider<HttpServletRequest> requestProvider;

    @Inject
    ZoomifyHelper zoomifyHelper;

    @Inject
    AudioStreamForwardingHelper audioHelper;

    @Inject
    @Named("forward-client")
    Provider<Client> clientProvider;

    
    /**
     * Because of rights and licenses
     */
    @Inject
    @Named("new-index")
    private SolrAccess solrAccess;

    @Inject
    RightsResolver rightsResolver;

    @Inject
    Instances instances;
    
    @HEAD
    @Path("{pid}")
    public Response checkItemExists(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.info();
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{source}/{pid}/info")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfo(@PathParam("pid") String pid, @PathParam("source") String source) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                return redirectHandler.info();
            } else {
                return Response.ok().build();
            }
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.infoData();
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


    @GET
    @Path("{source}/{pid}/info/data")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfoData(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                return redirectHandler.infoData();
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


    
    // musime resit pres forward
    @GET
    @Path("{pid}/info/providedByLicenses")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getProvidingLicenses(@PathParam("pid") String pid) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.providedByLicenses();
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{source}/{pid}/info/providedByLicenses")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getProvidingLicenses(@PathParam("pid") String pid,@PathParam("source") String source) {
        // must be redirected
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                return redirectHandler.providedByLicenses();
            } else {
                return Response.ok().build();
            }
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.infoStructure();
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{source}/{pid}/info/structure")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfoStructure(@PathParam("pid") String pid, @PathParam("source") String source) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.infoStructure();
            } else {
                return Response.ok().build();
            }
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.infoImage();
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{source}/{pid}/info/image")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfoImage(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                return redirectHandler.infoImage();
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }



    @HEAD
    @Path("{pid}/metadata/mods")
    public Response isMetadataModsAvailable(@PathParam("pid") String pid) {
        try {
            checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                return redirectHandler.mods(head);
            } else {
                return Response.ok().build();
            }

        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{source}/{pid}/metadata/mods")
    public Response isMetadataModsAvailable(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {
            checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                return redirectHandler.mods(head);
            } else {
                return Response.ok().build();
            }

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
        	// redirect
        	checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                return redirectHandler.mods(get);
            } else {
                return Response.ok().build();
            }

        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }
    
    /**
    @GET
    @Path("{pid}/metadata/deletetrig")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTrigger(@PathParam("pid") String pid) {
        try {
            // redirect
            checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                JSONObject obj = new JSONObject();
                obj.put("msg",String.format("Delete trigger for %s", pid));
                redirectHandler.deleteTriggeToReharvest(pid);
                return Response.ok(obj.toString()).type(MediaType.APPLICATION_JSON).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }**/


    @GET
    @Path("{source}/{pid}/metadata/mods")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getMetadataMods(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {
        	// redirect
        	checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                return redirectHandler.mods(get);
            } else {
                return Response.ok().build();
            }

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
        	// redirect
            checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                return redirectHandler.dc(head);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }
    
    @HEAD
    @Path("{source}/{pid}/metadata/dc")
    public Response isMetadataDublinCoreAvailable(@PathParam("pid") String pid, @PathParam("source") String source) {
        try {
        	// redirect
            checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                return redirectHandler.dc(head);
            } else {
                return Response.ok().build();
            }
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                return redirectHandler.dc(get);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{source}/{pid}/metadata/dc")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getMetadataDublinCore(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {
            checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                return redirectHandler.dc(get);
            } else {
                return Response.ok().build();
            }
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                return redirectHandler.textOCR(head);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    
    @HEAD
    @Path("{source}/{pid}/ocr/text")
    public Response isOcrTextAvailable(@PathParam("pid") String pid, @PathParam("source") String source) {
    	try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                return redirectHandler.textOCR(head);
            } else {
                return Response.ok().build();
            }
            
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                return redirectHandler.textOCR(get);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{source}/{pid}/ocr/text")
    @Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
    public Response getOcrText(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                return redirectHandler.textOCR(get);
            } else {
                return Response.ok().build();
            }
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                return redirectHandler.altoOCR(head);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{source}/{pid}/ocr/alto")
    public Response isOcrAltoAvailable(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                return redirectHandler.altoOCR(head);
            } else {
                return Response.ok().build();
            }
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
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                return redirectHandler.altoOCR(get);
            } else {
                return Response.ok().build();
            }

        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{source}/{pid}/ocr/alto")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getDatastreamOcrAlto(@PathParam("pid") String pid, @PathParam("source") String source) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                return redirectHandler.altoOCR(get);
            } else {
                return Response.ok().build();
            }

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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
            	return redirectHandler.image(head);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{source}/{pid}/image")
    public Response isImgFullAvailable(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {

            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
            	return redirectHandler.image(head);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }
    
    
    

    public ProxyItemHandler findRedirectHandler(String pid, String source) throws LexerException, IOException {
        if (source == null) {
        	source = defaultDocumentSource(pid);
        }
        OneInstance found = instances.find(source);
        if (found!= null) {
        	String remoteAddress = IPAddressUtils.getRemoteAddress(this.requestProvider.get(), KConfiguration.getInstance().getConfiguration());
        	ProxyItemHandler proxyHandler = found.createProxyItemHandler(this.userProvider.get(), this.clientProvider.get(), this.solrAccess, source, pid, remoteAddress);
        	return proxyHandler;
        } else {
        	return null;
        }
    }

    private String defaultDocumentSource(String pid) throws IOException {
        org.w3c.dom.Document solrDataByPid = this.solrAccess.getSolrDataByPid(pid);
        String leader = CDKUtils.findCDKLeader(solrDataByPid.getDocumentElement());
        List<String> sources = CDKUtils.findSources(solrDataByPid.getDocumentElement());
        return leader != null ? leader : (!sources.isEmpty() ? sources.get(0) : null);
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
            //checkObjectAndDatastreamExist(pid, dsId);

            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
            	return redirectHandler.image(get);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{source}/{pid}/image")
    public Response getImgFull(@PathParam("pid") String pid, @PathParam("source") String source) {
        try {
            checkSupportedObjectPid(pid);

            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
            	return redirectHandler.image(get);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
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
//            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.IMG_FULL;
//            checkObjectAndDatastreamExist(pid, dsId);

            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                //return sendRedirect(redirectHandler.zoomifyImageProperties());
                return redirectHandler.zoomifyImageProperties(get);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{source}/{pid}/image/zoomify/ImageProperties.xml")
    public Response getZoomifyImageProperties(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.IMG_FULL;
            //checkObjectAndDatastreamExist(pid, dsId);

            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                //return sendRedirect(redirectHandler.zoomifyImageProperties());
                return redirectHandler.zoomifyImageProperties(get);
            } else {
                return Response.ok().build();
            }
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
            //checkUserByJsessionidIsAllowedToReadIIPTile(pid);

            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                return redirectHandler.zoomifyTile(tileGroupStr, tileStr);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{source}/{pid}/image/zoomify/{tileGroup}/{tile}")
    public Response getZoomifyTile(@PathParam("pid") String pid,@PathParam("source") String source, @PathParam("tileGroup") String tileGroupStr, @PathParam("tile") String tileStr) {
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
            //checkUserByJsessionidIsAllowedToReadIIPTile(pid);

            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                return redirectHandler.zoomifyTile(tileGroupStr, tileStr);
            } else {
                return Response.ok().build();
            }
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
    // TODO : 
    @GET
    @Path("{pid}/image/thumb")
    public Response getImgThumb(@PathParam("pid") String pid) {
        try {
        	checkSupportedObjectPid(pid);
            //checkObjectExists(pid);
            
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            return redirectHandler.imageThumb(get);
            
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }
    
    @GET
    @Path("{source}/{pid}/image/thumb")
    public Response getImgThumb(@PathParam("pid") String pid, @PathParam("source") String source) {
        try {
        	checkSupportedObjectPid(pid);
            //checkObjectExists(pid);
            
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            return redirectHandler.imageThumb(get);
            
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
    @Path("{source}/{pid}/image/preview")
    public Response getImgPreview(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                return redirectHandler.imagePreview(get);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/image/preview")
    public Response getImgPreview(@PathParam("pid") String pid) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.imagePreview(get);
            } else {
                return Response.ok().build();
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.audioMP3();
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{source}/{pid}/audio/mp3")
    public Response isAudioMp3Available(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                return redirectHandler.audioMP3();
            } else {
                return Response.ok().build();
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.audioMP3();
            } else {
                return Response.ok().build();
            }

        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{source}/{pid}/audio/mp3")
    public Response getAudioMp3(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.audioMP3();
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/audio/ogg")
    public Response isAudioOggAvailable(@PathParam("pid") String pid) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.audioOGG();
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{source}/{pid}/audio/ogg")
    public Response isAudioOggAvailable(@PathParam("pid") String pid, @PathParam("source") String source) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                return redirectHandler.audioOGG();
            } else {
                return Response.ok().build();
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.audioOGG();
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{source}/{pid}/audio/ogg")
    public Response getAudioOgg(@PathParam("pid") String pid, @PathParam("source") String source) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                return redirectHandler.audioOGG();
            } else {
                return Response.ok().build();
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.audioOGG();
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{source}/{pid}/audio/wav")
    public Response isAudioWavAvailable(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                return redirectHandler.audioOGG();
            } else {
                return Response.ok().build();
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.audioOGG();
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{source}/{pid}/audio/wav")
    public Response getAudioWav(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                return redirectHandler.audioOGG();
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    //@Path("iiif/{pid}/info.json")
    @Path("{pid}/image/iiif/info.json")
    @Produces("application/ld+json")
    public Response iiiFManifest(@PathParam("pid") String pid) {
        try {
            LOGGER.info(String.format("Rendering  iiif info.json (%s)", pid));
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.iiifInfo(RequestMethodName.get, pid);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{source}/{pid}/image/iiif/info.json")
    @Produces("application/ld+json")
    public Response iiiFManifest(@PathParam("pid") String pid,@PathParam("source") String source) {
        try {
            LOGGER.info(String.format("Rendering  iiif info.json (%s,%s)", pid,source));
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                return redirectHandler.iiifInfo(RequestMethodName.get, pid);
            } else {
                return Response.ok().build();
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }
    
    
    @GET
    @Produces("image/jpeg")
    //@Path("iiif/{pid}/{region}/{size}/{rotation}/{qualityformat}")
    @Path("{pid}/image/iiif/{region}/{size}/{rotation}/{qualityformat}")
    public Response tile(@PathParam("pid") String pid, 
            @PathParam("region") String region, 
            @PathParam("size") String size,
            @PathParam("rotation") String rotation,
            @PathParam("qualityformat") String qf
            ) {

        try {

            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                return redirectHandler.iiifTile(RequestMethodName.get, pid, region, size, rotation,qf);
            } else {
                return Response.ok().build();
            }

        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Produces("image/jpeg")
    //@Path("iiif/{source}/{pid}/{region}/{size}/{rotation}/{qualityformat}")
    @Path("{source}/{pid}/image/iiif/{region}/{size}/{rotation}/{qualityformat}")
    public Response tile(@PathParam("source") String source,  @PathParam("pid") String pid, 
            @PathParam("region") String region, 
            @PathParam("size") String size,
            @PathParam("rotation") String rotation, 
            @PathParam("qualityformat") String qf
            ) {
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                return redirectHandler.iiifTile(RequestMethodName.get, pid, region, size, rotation,qf);
            } else {
                return Response.ok().build();
            }

        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

}
