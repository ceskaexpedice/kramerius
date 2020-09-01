package cz.incad.kramerius.rest.apiNew.admin.v10;

import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import org.dom4j.Document;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Logger;

@Path("/admin/v1.0/item")
public class ItemResource extends AdminApiResource {

    public static Logger LOGGER = Logger.getLogger(ItemResource.class.getName());

    //TODO: prejmenovat role podle spravy uctu
    private static final String ROLE_READ_FOXML = "kramerius_admin";

    @GET
    @Path("{pid}/foxml")
    @Produces(MediaType.APPLICATION_XML)
    public Response getFoxml(@PathParam("pid") String pid) {
        try {
            boolean disableAuth = true; //TODO: reenable for production
            //authentication
            if (!disableAuth) {
                AuthenticatedUser user = getAuthenticatedUser();
                String role = ROLE_READ_FOXML;
                if (!user.getRoles().contains(role)) {
                    throw new ForbiddenException("user '%s' is not allowed to manage processes (missing role '%s')", user.getName(), role); //403
                }
            }
            checkObjectExists(pid);
            Document foxml = krameriusRepositoryApi.getLowLevelApi().getFoxml(pid);
            return Response.ok().entity(foxml.asXML()).build();
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }

    @HEAD
    @Path("{pid}/streams/{dsid}")
    public Response checkDatastreamExists(@PathParam("pid") String pid, @PathParam("dsid") String dsid) {
        boolean disableAuth = true; //TODO: reenable for production
        //authentication
        if (!disableAuth) {
            AuthenticatedUser user = getAuthenticatedUser();
            String role = ROLE_READ_FOXML;
            if (!user.getRoles().contains(role)) {
                throw new ForbiddenException("user '%s' is not allowed to manage processes (missing role '%s')", user.getName(), role); //403
            }
        }
        checkDsExists(pid, dsid);
        return Response.ok().build();
    }

    @GET
    @Path("{pid}/streams/{dsid}")
    public Response getDatastream(@PathParam("pid") String pid, @PathParam("dsid") String dsid) {
        try {
            boolean disableAuth = true; //TODO: reenable for production
            //authentication
            if (!disableAuth) {
                AuthenticatedUser user = getAuthenticatedUser();
                String role = ROLE_READ_FOXML;
                if (!user.getRoles().contains(role)) {
                    throw new ForbiddenException("user '%s' is not allowed to manage processes (missing role '%s')", user.getName(), role); //403
                }
            }
            checkObjectExists(pid);
            checkDsExists(pid, dsid);
            switch (dsid) {
                case "BIBLIO_MODS":
                    return Response.ok()
                            .type(MediaType.APPLICATION_XML + ";charset=utf-8")
                            .entity(krameriusRepositoryApi.getMods(pid, true).asXML())
                            .build();
                case "DC":
                    return Response.ok()
                            .type(MediaType.APPLICATION_XML + ";charset=utf-8")
                            .entity(krameriusRepositoryApi.getDublinCore(pid, true).asXML())
                            .build();
                case "RELS-EXT":
                    return Response.ok()
                            .type(MediaType.APPLICATION_XML + ";charset=utf-8")
                            .entity(krameriusRepositoryApi.getRelsExt(pid, true).asXML())
                            .build();
                //TODO: OCR, IMAGES, ...
                default:
                    return Response.status(Response.Status.BAD_REQUEST).build();

            }
        } catch (RepositoryException | IOException e) {
            throw new InternalErrorException(e.getMessage());
        }
    }
}
