package cz.incad.kramerius.rest.apiNew.client.v70;

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

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/client/v7.0/ui-config")
public class UIConfigResource extends AdminApiResource {

    private static final Logger LOGGER = Logger.getLogger(UIConfigResource.class.getName());

    @Inject
    RightsResolver rightsResolver;

    @Inject
    javax.inject.Provider<User> userProvider;

    @Inject
    @Named("kramerius4")
    private Provider<Connection> connectionProvider;

    @GET
    @Path("general")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGeneralConfig() {
        return getConfig(UIConfigType.GENERAL);
    }

    @GET
    @Path("licenses")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLicensesConfig() {
        return getConfig(UIConfigType.LICENSES);
    }

    @GET
    @Path("curator-lists")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCuratorListsConfig() {
        return getConfig(UIConfigType.CURATOR_LISTS);
    }

    private Response getConfig(UIConfigType type) {
        try {
            DbUIConfigService dbUIConfigService = new DbUIConfigService(connectionProvider, new JsonValidator());
            InputStream in = dbUIConfigService.load(type);
            return Response.ok(in).header("Cache-Control", "no-cache").build();
        } catch (NotFoundException e) {
            throw e;
        } catch (UIConfigException e) {
            LOGGER.log(Level.SEVERE, "Failed to load UI config " + type, e);
            throw new InternalServerErrorException("Failed to load UI config");
        }
    }

}
