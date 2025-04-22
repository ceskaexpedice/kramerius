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

import cz.incad.kramerius.rest.apiNew.client.v70.redirection.DeleteTriggerSupport;
import cz.inovatika.monitoring.APICallMonitor;
import cz.inovatika.monitoring.ApiCallEvent;

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
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;


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

    @Inject
    @Named("forward-client")
    Provider<CloseableHttpClient> apacheClient;


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

    @Inject
    APICallMonitor apiCallMonitor;

    @Inject
    DeleteTriggerSupport deleteTriggerSupport;

    @HEAD
    @Path("{pid}")
    public Response checkItemExists(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s",pid), "", "HEAD", pid);
        try {
            checkSupportedObjectPid(pid);
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.info(event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/info")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfo(@PathParam("pid") String pid, @PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/info", source,pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.info(event);
            } else {
                return Response.ok().build();
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
    @Path("{pid}/info/data")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfoData(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/info/data", pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.infoData(event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/info/data")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfoData(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/info/data", source, pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.infoData(event);
            } else {
                return Response.ok().build();
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


    
    // musime resit pres forward
    @GET
    @Path("{pid}/info/providedByLicenses")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getProvidingLicenses(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/info/providedByLicenses", pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.providedByLicenses(event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/info/providedByLicenses")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getProvidingLicenses(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/info/providedByLicenses",source, pid), "", "GET", pid);
        // must be redirected
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.providedByLicenses(event);
            } else {
                return Response.ok().build();
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.infoStructure(event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/info/structure")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfoStructure(@PathParam("pid") String pid, @PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/info/structure",pid,source), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.infoStructure(event);
            } else {
                return Response.ok().build();
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
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.infoImage(event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/info/image")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getInfoImage(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/info/image",source,pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.infoImage(event);
            } else {
                return Response.ok().build();
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



    @HEAD
    @Path("{pid}/metadata/mods")
    public Response isMetadataModsAvailable(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/metadata/mods",pid), "", "HEAD", pid);
        try {
            checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.mods(head, event);
            } else {
                return Response.ok().build();
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

    @HEAD
    @Path("{source}/{pid}/metadata/mods")
    public Response isMetadataModsAvailable(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/metadata/mods", source,pid), "", "HEAD", pid);
        try {
            checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.mods(head, event);
            } else {
                return Response.ok().build();
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
    @Path("{pid}/metadata/mods")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getMetadataMods(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/metadata/mods",pid), "", "GET", pid);
        try {
        	// redirect
        	checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.mods(get, event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/metadata/mods")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getMetadataMods(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/metadata/mods",source,pid), "", "GET", pid);
        try {
        	// redirect
        	checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.mods(get, event);
            } else {
                return Response.ok().build();
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


    @HEAD
    @Path("{pid}/metadata/dc")
    public Response isMetadataDublinCoreAvailable(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/metadata/dc",pid), "", "HEAD", pid);
        try {
        	// redirect
            checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.dc(head, event);
            } else {
                return Response.ok().build();
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
    
    @HEAD
    @Path("{source}/{pid}/metadata/dc")
    public Response isMetadataDublinCoreAvailable(@PathParam("pid") String pid, @PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/metadata/dc",source,pid), "", "HEAD", pid);
        try {
        	// redirect
            checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.dc(head, event);
            } else {
                return Response.ok().build();
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
    @Path("{pid}/metadata/dc")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getMetadataDublinCore(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/metadata/dc",pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.dc(get, event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/metadata/dc")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getMetadataDublinCore(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/metadata/dc", source,pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.dc(get, event);
            } else {
                return Response.ok().build();
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

    @HEAD
    @Path("{pid}/ocr/text")
    public Response isOcrTextAvailable(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/ocr/text", pid), "", "HEAD", pid);
    	try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.textOCR(head, event);
            } else {
                return Response.ok().build();
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

    
    @HEAD
    @Path("{source}/{pid}/ocr/text")
    public Response isOcrTextAvailable(@PathParam("pid") String pid, @PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/ocr/text", source, pid), "", "HEAD", pid);
    	try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.textOCR(head,event);
            } else {
                return Response.ok().build();
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
    @Path("{pid}/ocr/text")
    @Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
    public Response getOcrText(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/ocr/text", pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.textOCR(get, event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/ocr/text")
    @Produces(MediaType.TEXT_PLAIN + ";charset=utf-8")
    public Response getOcrText(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/ocr/text", source, pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.textOCR(get, event);
            } else {
                return Response.ok().build();
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

    @HEAD
    @Path("{pid}/ocr/alto")
    public Response isOcrAltoAvailable(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/ocr/alto", pid), "", "HEAD", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.altoOCR(head, event);
            } else {
                return Response.ok().build();
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

    @HEAD
    @Path("{source}/{pid}/ocr/alto")
    public Response isOcrAltoAvailable(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/ocr/alto",source, pid), "", "HEAD", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.altoOCR(head, event);
            } else {
                return Response.ok().build();
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
    @Path("{pid}/ocr/alto")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getDatastreamOcrAlto(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/ocr/alto", pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.altoOCR(get, event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/ocr/alto")
    @Produces(MediaType.APPLICATION_XML + ";charset=utf-8")
    public Response getDatastreamOcrAlto(@PathParam("pid") String pid, @PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/ocr/alto", source, pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.altoOCR(get, event);
            } else {
                return Response.ok().build();
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

    /**
     * Zkontroluje existenci a právo čtení datastreamu IMG_FULL
     */
    @HEAD
    @Path("{pid}/image")
    public Response isImgFullAvailable(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/image",  pid), "", "HEAD", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
            	return redirectHandler.image(head, event);
            } else {
                return Response.ok().build();
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

    @HEAD
    @Path("{source}/{pid}/image")
    public Response isImgFullAvailable(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/image", source,  pid), "", "HEAD", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
            	return redirectHandler.image(head, event);
            } else {
                return Response.ok().build();
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
    

    public ProxyItemHandler findRedirectHandler(String pid, String source) throws LexerException, IOException {
        if (source == null) {
        	source = defaultDocumentSource(pid);
        }
        OneInstance found = instances.find(source);
        if (found!= null) {
        	String remoteAddress = IPAddressUtils.getRemoteAddress(this.requestProvider.get(), KConfiguration.getInstance().getConfiguration());
        	ProxyItemHandler proxyHandler = found.createProxyItemHandler(this.userProvider.get(), this.apacheClient.get(), this.deleteTriggerSupport, this.solrAccess, source, pid, remoteAddress);
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
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/image",  pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.IMG_FULL;
            //checkObjectAndDatastreamExist(pid, dsId);

            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
            	return redirectHandler.image(get, event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/image")
    public Response getImgFull(@PathParam("pid") String pid, @PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/image", source,  pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
            	return redirectHandler.image(get, event);
            } else {
                return Response.ok().build();
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
    /***
     * Vrací zoomify ImageProperties.xml tohoto objektu
     * @see cz.incad.Kramerius.imaging.ZoomifyServlet
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/image/zoomify/ImageProperties.xml")
    public Response getZoomifyImageProperties(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/image/ImageProperties.xml", pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);

            ProxyItemHandler redirectHandler = findRedirectHandler(pid, null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.zoomifyImageProperties(get, event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/image/zoomify/ImageProperties.xml")
    public Response getZoomifyImageProperties(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/image/zoomify/ImageProperties.xml", source, pid), "", "GET", pid);
        try {
            checkSupportedObjectPid(pid);
            KrameriusRepositoryApi.KnownDatastreams dsId = KrameriusRepositoryApi.KnownDatastreams.IMG_FULL;
            //checkObjectAndDatastreamExist(pid, dsId);

            ProxyItemHandler redirectHandler = findRedirectHandler(pid, source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.zoomifyImageProperties(get,event);
            } else {
                return Response.ok().build();
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

    /***
     * Vrací zoomify dlaždice obrázku tohoto objektu
     * @see cz.incad.Kramerius.imaging.ZoomifyServlet
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/image/zoomify/{tileGroup}/{tile}")
    public Response getZoomifyTile(@PathParam("pid") String pid, @PathParam("tileGroup") String tileGroupStr, @PathParam("tile") String tileStr) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/image/zoomify/%s/%s",  pid,tileGroupStr, tileStr), "", "GET", pid);
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
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.zoomifyTile(tileGroupStr, tileStr, event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/image/zoomify/{tileGroup}/{tile}")
    public Response getZoomifyTile(@PathParam("pid") String pid,@PathParam("source") String source, @PathParam("tileGroup") String tileGroupStr, @PathParam("tile") String tileStr) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/image/zoomify/%s/%s", source,  pid,tileGroupStr, tileStr), "", "GET", pid);
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
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.zoomifyTile(tileGroupStr, tileStr,event);
            } else {
                return Response.ok().build();
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

    /***
     * Vrací thumbnail buď tohoto objektu, nebo prvního potomka, který má IMG_THUMB
     */
    // TODO : 
    @GET
    @Path("{pid}/image/thumb")
    public Response getImgThumb(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/image/thumb",  pid), "", "GET", pid);
        try {
        	checkSupportedObjectPid(pid);
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.imageThumb(get, event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/image/thumb")
    public Response getImgThumb(@PathParam("pid") String pid, @PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/image/thumb", source,  pid), "", "GET", pid);
        try {
        	checkSupportedObjectPid(pid);
            //checkObjectExists(pid);
            
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.imageThumb(get, event);
            } else {
                return Response.ok().build();
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


    /***
     * Vrací preview buď tohoto objektu, nebo prvního potomka, který má IMG_PREVIEW
     */
    @GET
    @Path("{source}/{pid}/image/preview")
    public Response getImgPreview(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/image/preview", source,  pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.imagePreview(get, event);
            } else {
                return Response.ok().build();
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
    @Path("{pid}/image/preview")
    public Response getImgPreview(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/image/preview", pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.imagePreview(get, event);
            } else {
                return Response.ok().build();
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
    
    @HEAD
    @Path("{pid}/audio/mp3")
    public Response isAudioMp3Available(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/audio/mp3", pid), "", "HEAD", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.audioMP3(event);
            } else {
                return Response.ok().build();
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

    @HEAD
    @Path("{source}/{pid}/audio/mp3")
    public Response isAudioMp3Available(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/audio/mp3", source, pid), "", "HEAD", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.audioMP3(event);
            } else {
                return Response.ok().build();
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
    /***
     * Vrací obsah datastreamu MP3 tohoto objektu
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/audio/mp3")
    public Response getAudioMp3(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/audio/mp3",  pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.audioMP3(event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/audio/mp3")
    public Response getAudioMp3(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/audio/mp3", source, pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.audioMP3(event);
            } else {
                return Response.ok().build();
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

    @HEAD
    @Path("{pid}/audio/ogg")
    public Response isAudioOggAvailable(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/audio/ogg",  pid), "", "HEAD", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.audioOGG(event);
            } else {
                return Response.ok().build();
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

    @HEAD
    @Path("{source}/{pid}/audio/ogg")
    public Response isAudioOggAvailable(@PathParam("pid") String pid, @PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/audio/ogg", source,  pid), "", "HEAD", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.audioOGG(event);
            } else {
                return Response.ok().build();
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

    /***
     * Vrací obsah datastreamu OGG tohoto objektu
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/audio/ogg")
    public Response getAudioOgg(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/audio/ogg",   pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.audioOGG(event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/audio/ogg")
    public Response getAudioOgg(@PathParam("pid") String pid, @PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/audio/ogg", source,   pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.audioOGG(event);
            } else {
                return Response.ok().build();
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

    @HEAD
    @Path("{pid}/audio/wav")
    public Response isAudioWavAvailable(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/audio/wav",  pid), "", "HEAD", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.audioOGG(event);
            } else {
                return Response.ok().build();
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

    @HEAD
    @Path("{source}/{pid}/audio/wav")
    public Response isAudioWavAvailable(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/audio/wav", source,   pid), "", "HEAD", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.audioOGG(event);
            } else {
                return Response.ok().build();
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

    /***
     * Vrací obsah datastreamu WAV tohoto objektu
     */
    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{pid}/audio/wav")
    public Response getAudioWav(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/audio/wav",   pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.audioOGG(event);
            } else {
                return Response.ok().build();
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

    @SuppressWarnings("JavadocReference")
    @GET
    @Path("{source}/{pid}/audio/wav")
    public Response getAudioWav(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/audio/wav", source,   pid), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.audioOGG(event);
            } else {
                return Response.ok().build();
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
    //@Path("iiif/{pid}/info.json")
    @Path("{pid}/image/iiif/info.json")
    @Produces("application/ld+json")
    public Response iiiFManifest(@PathParam("pid") String pid) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/image/iiif/info.json",   pid), "", "GET", pid);
        try {
            LOGGER.fine(String.format("Rendering  iiif info.json (%s)", pid));
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.iiifInfo(RequestMethodName.get, pid, event);
            } else {
                return Response.ok().build();
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
    @Path("{source}/{pid}/image/iiif/info.json")
    @Produces("application/ld+json")
    public Response iiiFManifest(@PathParam("pid") String pid,@PathParam("source") String source) {
        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/image/iiif/info.json", source,   pid), "", "GET", pid);
        try {
            LOGGER.info(String.format("Rendering  iiif info.json (%s,%s)", pid,source));
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.iiifInfo(RequestMethodName.get, pid, event);
            } else {
                return Response.ok().build();
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
    @Produces("image/jpeg")
    //@Path("iiif/{pid}/{region}/{size}/{rotation}/{qualityformat}")
    @Path("{pid}/image/iiif/{region}/{size}/{rotation}/{qualityformat}")
    public Response tile(@PathParam("pid") String pid, 
            @PathParam("region") String region, 
            @PathParam("size") String size,
            @PathParam("rotation") String rotation,
            @PathParam("qualityformat") String qf
            ) {

        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/image/%s/%s/%s/%s",  pid, region, size, rotation, qf), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,null);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.iiifTile(RequestMethodName.get, pid, region, size, rotation,qf, event);
            } else {
                return Response.ok().build();
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
    @Produces("image/jpeg")
    //@Path("iiif/{source}/{pid}/{region}/{size}/{rotation}/{qualityformat}")
    @Path("{source}/{pid}/image/iiif/{region}/{size}/{rotation}/{qualityformat}")
    public Response tile(@PathParam("source") String source,  @PathParam("pid") String pid, 
            @PathParam("region") String region, 
            @PathParam("size") String size,
            @PathParam("rotation") String rotation, 
            @PathParam("qualityformat") String qf
            ) {

        ApiCallEvent event = this.apiCallMonitor.start("/client/v7.0/items", String.format("/client/v7.0/items/%s/%s/image/%s/%s/%s/%s", source,  pid, region, size, rotation, qf), "", "GET", pid);
        try {
            ProxyItemHandler redirectHandler = findRedirectHandler(pid,source);
            if (redirectHandler != null) {
                event.addLabel(redirectHandler.getSource());
                return redirectHandler.iiifTile(RequestMethodName.get, pid, region, size, rotation,qf,event);
            } else {
                return Response.ok().build();
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

}
