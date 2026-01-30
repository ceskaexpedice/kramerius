package cz.incad.kramerius.rest.apiNew.admin.v70.uiconfig;

import cz.incad.kramerius.rest.apiNew.admin.v70.AdminApiResource;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.uiconfig.InvalidJsonException;
import cz.incad.kramerius.uiconfig.UIConfigService;
import cz.incad.kramerius.uiconfig.UIConfigType;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/admin/v7.0/ui-config")
public class UIConfigResource extends AdminApiResource {

    public static final Logger LOGGER =
            Logger.getLogger(UIConfigResource.class.getName());

    @Inject
    @Named("dbUiConfig")
    UIConfigService uiConfigService;

    // --------------------------------------------------------------------
    // GENERAL
    // --------------------------------------------------------------------

    @GET
    @Path("/general")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGeneralConfig() {
        return getConfig(UIConfigType.GENERAL);
    }

    @POST
    @Path("/general")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveGeneralConfig(InputStream json) {
        return saveConfig(UIConfigType.GENERAL, json);
    }

    // --------------------------------------------------------------------
    // LICENSES
    // --------------------------------------------------------------------

    @GET
    @Path("/licenses")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLicensesConfig() {
        return getConfig(UIConfigType.LICENSES);
    }

    @POST
    @Path("/licenses")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveLicensesConfig(InputStream json) {
        return saveConfig(UIConfigType.LICENSES, json);
    }

    // --------------------------------------------------------------------
    // CURATOR LISTS
    // --------------------------------------------------------------------

    @GET
    @Path("/curator-lists")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCuratorListsConfig() {
        return getConfig(UIConfigType.CURATOR_LISTS);
    }

    @POST
    @Path("/curator-lists")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveCuratorListsConfig(InputStream json) {
        return saveConfig(UIConfigType.CURATOR_LISTS, json);
    }

    // --------------------------------------------------------------------
    // INTERNAL HELPERS
    // --------------------------------------------------------------------

    private Response getConfig(UIConfigType type) {
        try {
            if (!uiConfigService.exists(type)) {
                throw new NotFoundException("UI config not found: " + type);
            }

            InputStream in = uiConfigService.load(type);

            return Response.ok(in)
                    .header("Cache-Control", "no-cache")
                    .build();

        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private Response saveConfig(UIConfigType type, InputStream json) {
        try {
            uiConfigService.save(type, json);
            return Response.noContent().build();

        } catch (InvalidJsonException e) {
            throw new BadRequestException(e.getMessage(), e);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }
}
