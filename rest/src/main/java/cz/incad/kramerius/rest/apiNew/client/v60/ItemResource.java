package cz.incad.kramerius.rest.apiNew.client.v60;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.rest.apiNew.exceptions.ApiException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;
import cz.incad.kramerius.utils.ApplicationURL;

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
public class ItemResource {

    //TODO: uklid
    //(ne-admin) client je neutentizovany, jenom cte data a mela by pred nim byt do urcite miry skryta implementece, takze:

    // {pid}/foxml                  -> zrusit tady, presunotu do admin api
    // {pid}/streams                -> zrusit, odhaluje implementaci
    // {pid}/full                   -> nahradit za {pid}/image/full
    // {pid}/thumb                  -> nahradit za {pid}/image/thumb
    // {pid}/preview                -> nahradit za {pid}/image/preview, nebo uplne zrusit (nepouziva se bud thumb, nebo preview, nikdy nevim ktery)
    // {pid}/streams/BIBLIO_MODS    -> nahradit za {pid}/metadata/mods
    // {pid}/streams/DC             -> nahradit za {pid}/metadata/dublin_core
    // {pid}/streams/RELS_EXT       -> nahradit za {pid}/structure, nebo vyhledove zahodi, pokud se ukaze, ze neni potreba

    // {pid}
    // {pid}/parents
    // {pid}/siblings
    // {pid}/children

    @Inject
    @Named("securedFedoraAccess")
    FedoraAccess repositoryAccess;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    public static final Logger LOGGER = Logger.getLogger(ItemResource.class.getName());

    private void checkObjectExists(String pid) throws ApiException {
        try {
            boolean objectExists = this.repositoryAccess.isObjectAvailable(pid);
            if (!objectExists) {
                throw new NotFoundException("object with pid %s not found in repository", pid);
            }
        } catch (IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/foxml")
    public Response getFoxml(@PathParam("pid") String pid) {
        //TODO: poradna implementace, namisto redirectu na api/v5.0
        try {
            checkObjectExists(pid);
            URI uri = new URI(String.format("%s/v5.0/item/%s/foxml", getApiBaseUrl(), pid));
            return Response.seeOther(uri).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/full")
    public Response getImgFull(@PathParam("pid") String pid, @QueryParam("asFile") String asFile) {
        //TODO: poradna implementace, namisto redirectu na api/v5.0
        try {
            checkObjectExists(pid);
            URI uri = new URI(String.format("%s/v5.0/item/%s/full", getApiBaseUrl(), pid));
            Response.temporaryRedirect(uri).build();
            return Response.seeOther(uri).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/thumb")
    public Response getImgThumb(@PathParam("pid") String pid) {
        //TODO: poradna implementace, namisto redirectu na api/v5.0
        try {
            checkObjectExists(pid);
            URI uri = new URI(String.format("%s/v5.0/item/%s/thumb", getApiBaseUrl(), pid));
            Response.temporaryRedirect(uri).build();
            return Response.seeOther(uri).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/preview")
    public Response getImgPreview(@PathParam("pid") String pid) {
        //TODO: poradna implementace, namisto redirectu na api/v5.0
        try {
            checkObjectExists(pid);
            URI uri = new URI(String.format("%s/v5.0/item/%s/preview", getApiBaseUrl(), pid));
            Response.temporaryRedirect(uri).build();
            return Response.seeOther(uri).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("{pid}/streams/{dsid}")
    public Response stream(@PathParam("pid") String pid,
                           @PathParam("dsid") String dsid) {
        //TODO: implement or remove
        //tohle asi pujde pryc, na teto urovni abstrakce mame konkretni metody pro konkretni streamy
        //ale tim padem by tu mely byt metody getMods, getOcrTxt, getOcrXml apod.
        //system streamu (a verzovani) bude pro client api skryty (detail imlementace)
        throw new InternalErrorException("not implemented yet");
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
        throw new InternalErrorException("not implemented yet");
    }

    @GET
    @Path("{pid}")
    @Produces({MediaType.APPLICATION_JSON + ";charset=utf-8"})
    public Response basic(@PathParam("pid") String pid) {
        //TODO: implement or remove
        throw new InternalErrorException("not implemented yet");
    }

    private String getApiBaseUrl() {
        //return "http://localhost:8080/search/api";
        String appUrl = ApplicationURL.applicationURL(this.requestProvider.get());
        return appUrl + "/api";
    }

}
