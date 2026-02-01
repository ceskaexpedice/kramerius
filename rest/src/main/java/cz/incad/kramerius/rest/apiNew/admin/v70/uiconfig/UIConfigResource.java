package cz.incad.kramerius.rest.apiNew.admin.v70.uiconfig;

import cz.incad.kramerius.rest.apiNew.admin.v70.AdminApiResource;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.uiconfig.InvalidJsonException;
import cz.incad.kramerius.uiconfig.DbUIConfigService;
import cz.incad.kramerius.uiconfig.UIConfigException;
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
    DbUIConfigService dbUiConfigService;

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
            InputStream in = dbUiConfigService.load(type);
            return Response.ok(in).header("Cache-Control", "no-cache").build();
        } catch (NotFoundException e) {
            throw e;
        } catch (UIConfigException e) {
            LOGGER.log(Level.SEVERE, "Failed to load UI config " + type, e);
            throw new InternalServerErrorException("Failed to load UI config");
        }
    }

    private Response saveConfig(UIConfigType type, InputStream json) {
        try {
            dbUiConfigService.save(type, json);
            return Response.noContent().build();
        } catch (InvalidJsonException e) {
            throw new BadRequestException(e.getMessage(), e);
        } catch (UIConfigException e) {
            LOGGER.log(Level.SEVERE, "Failed to save UI config " + type, e);
            throw new InternalServerErrorException("Failed to save UI config");
        }
    }
}
