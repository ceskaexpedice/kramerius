package cz.incad.kramerius.rest.apiNew.admin.v70.uiconfig;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.apiNew.admin.v70.AdminApiResource;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.uiconfig.*;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/admin/v7.0/ui-config")
public class UIConfigResource extends AdminApiResource {

    private static final Logger LOGGER = Logger.getLogger(UIConfigResource.class.getName());

    @Inject
    RightsResolver rightsResolver;

    @Inject
    javax.inject.Provider<User> userProvider;

    @Inject
    @Named("kramerius4")
    private Provider<Connection> connectionProvider;

    // --------------------------------------------------------------------
    // GENERAL
    // --------------------------------------------------------------------

    @GET
    @Path("general")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGeneralConfig() {
        return getConfig(UIConfigType.GENERAL);
    }

    @POST
    @Path("general")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveGeneralConfig(InputStream json) {
        return saveConfig(UIConfigType.GENERAL, json);
    }

    // --------------------------------------------------------------------
    // LICENSES
    // --------------------------------------------------------------------

    @GET
    @Path("licenses")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLicensesConfig() {
        return getConfig(UIConfigType.LICENSES);
    }

    @POST
    @Path("licenses")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveLicensesConfig(InputStream json) {
        return saveConfig(UIConfigType.LICENSES, json);
    }

    // --------------------------------------------------------------------
    // CURATOR LISTS
    // --------------------------------------------------------------------

    @GET
    @Path("curator-lists")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCuratorListsConfig() {
        return getConfig(UIConfigType.CURATOR_LISTS);
    }

    @POST
    @Path("curator-lists")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveCuratorListsConfig(InputStream json) {
        return saveConfig(UIConfigType.CURATOR_LISTS, json);
    }

    // --------------------------------------------------------------------
    // INTERNAL HELPERS
    // --------------------------------------------------------------------

    private Response getConfig(UIConfigType type) {
        try {
            User user = this.userProvider.get();
            if (permitConfig(user)) {
                DbUIConfigService dbUIConfigService = new DbUIConfigService(connectionProvider, new JsonValidator());
                InputStream in = dbUIConfigService.load(type);
                return Response.ok(in).header("Cache-Control", "no-cache").build();
            } else {
                throw new cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException("user '%s' is not allowed to manage config ", user.getLoginname()); //403
            }
        } catch (NotFoundException e) {
            throw e;
        } catch (UIConfigException e) {
            LOGGER.log(Level.SEVERE, "Failed to load UI config " + type, e);
            throw new InternalServerErrorException("Failed to load UI config");
        }
    }

    private Response saveConfig(UIConfigType type, InputStream json) {
        try {
            User user = this.userProvider.get();
            if (permitConfig(user)) {
                DbUIConfigService dbUIConfigService = new DbUIConfigService(connectionProvider, new JsonValidator());
                dbUIConfigService.save(type, json);
                return Response.noContent().build();
            } else {
                throw new cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException("user '%s' is not allowed to manage config ", user.getLoginname()); //403
            }
        } catch (InvalidJsonException e) {
            throw new BadRequestException(e.getMessage(), e);
        } catch (UIConfigException e) {
            LOGGER.log(Level.SEVERE, "Failed to save UI config " + type, e);
            throw new InternalServerErrorException("Failed to save UI config");
        }
    }

    private boolean permitConfig(User user) {
        if (user != null)
            return this.rightsResolver.isActionAllowed(user,
                    SecuredActions.A_ADMIN_READ.getFormalName(),
                    SpecialObjects.REPOSITORY.getPid(), null,
                    ObjectPidsPath.REPOSITORY_PATH).flag();
        else
            return false;
    }

}
