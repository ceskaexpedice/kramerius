package cz.incad.kramerius.rest.apiNew.client.v60;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sun.jmx.mbeanserver.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.utils.ApplicationURL;
import org.dom4j.Document;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * @see cz.incad.kramerius.rest.api.k5.client.item.ItemResource
 */
@Path("/client/v6.0/item")
public class ItemResource extends ClientApiResource {

    //TODO: uklid
    //(ne-admin) client je neutentizovany, jenom cte data a mela by pred nim byt do urcite miry skryta implementece, takze:

    // {pid}/foxml                  -> zrusit tady, presunotu do admin api
    // {pid}/streams                -> zrusit, odhaluje implementaci
    // {pid}/full                   -> nahradit za {pid}/image/full
    // {pid}/thumb                  -> nahradit za {pid}/image/thumb
    // {pid}/preview                -> nahradit za {pid}/image/preview, nebo uplne zrusit (nepouziva se bud thumb, nebo preview, nikdy nevim ktery)
    // {pid}/streams/BIBLIO_MODS    -> nahradit za {pid}/metadata/mods - DONE
    // {pid}/streams/DC             -> nahradit za {pid}/metadata/dublin_core- DONE
    // {pid}/streams/RELS_EXT       -> nahradit za {pid}/structure, nebo vyhledove zahodi, pokud se ukaze, ze neni potreba
    // {pid}/streams/OCR_TEXT       -> nahradit za {pid}/ocr/text
    // {pid}/streams/OCR_ALTO       -> nahradit za {pid}/ocr/alto


    //pripadne jen plochou strukturu ( {pid}/mods, {pid}/thumb {pid}/full, {pid}/children ...)

    // {pid}
    // {pid}/parents
    // {pid}/siblings
    // {pid}/children
    // anebo dohromady v
    // {pid}/structure

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
    @Path("{pid}/metadata/mods")
    @Produces({MediaType.APPLICATION_XML + ";charset=utf-8"})
    public Response getDatastreamMods(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectExists(pid);
            checkDsExists(pid, KrameriusRepositoryApi.KnownDatastreams.BIBLIO_MODS);
            Document mods = krameriusRepositoryApi.getMods(pid, true);
            return Response.ok()
                    .entity(mods.asXML())
                    .build();
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/metadata/dublin_core")
    @Produces({MediaType.APPLICATION_XML + ";charset=utf-8"})
    public Response getDatastreamDublinCore(@PathParam("pid") String pid) {
        //TODO: autorizace podle zdroje přístupu, POLICY apod.
        try {
            checkObjectExists(pid);
            checkDsExists(pid, KrameriusRepositoryApi.KnownDatastreams.BIBLIO_DC);
            Document dc = krameriusRepositoryApi.getDublinCore(pid, true);
            return Response.ok().entity(dc.asXML()).build();
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


    @GET
    @Path("{pid}/streams/{dsid}")
    public Response stream(@PathParam("pid") String pid,
                           @PathParam("dsid") String dsid) {
        //TODO: poradna implementace, namisto redirectu na api/v5.0
        //tohle asi pujde pryc, na teto urovni abstrakce mame konkretni metody pro konkretni streamy
        //ale tim padem by tu mely byt metody getMods, getOcrTxt, getOcrXml apod.
        //system streamu (a verzovani) bude pro client api skryty (detail imlementace)
        try {
            checkObjectExists(pid);
            URI uri = new URI(String.format("%s/v5.0/item/%s/streams/%s", getApiBaseUrl(), pid, dsid));
            return Response.temporaryRedirect(uri).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/streams")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response streams(@PathParam("pid") String pid) {
        //TODO: implement or remove
        throw new InternalErrorException("not implemented yet");
    }

    @GET
    @Path("{pid}/parents")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response getObjectsParents(@PathParam("pid") String pid) {
        //TODO: remove or implement (implementation will use repository, not search index)
        throw new InternalErrorException("not implemented yet");
    }

    @GET
    @Path("{pid}/siblings")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response getObjectSiblings(@PathParam("pid") String pid) {
        //TODO: remove or implement (implementation will use repository, not search index)
        throw new InternalErrorException("not implemented yet");
    }

    @GET
    @Path("{pid}/children")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response getObjectsChildren(@PathParam("pid") String pid) {
        //TODO: remove or implement (implementation will use repository, not search index)
        try {
            checkObjectExists(pid);
            URI uri = new URI(String.format("%s/v5.0/item/%s/children", getApiBaseUrl(), pid));
            return Response.temporaryRedirect(uri).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response basic(@PathParam("pid") String pid) {
        //TODO: implement or remove
        try {
            checkObjectExists(pid);
            URI uri = new URI(String.format("%s/v5.0/item/%s", getApiBaseUrl(), pid));
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
