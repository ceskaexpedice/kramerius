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
import cz.incad.kramerius.uiconfig.DbUIConfigResourceService;
import cz.incad.kramerius.uiconfig.UIConfigException;
import cz.incad.kramerius.uiconfig.UIConfigResourceContent;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/admin/v7.0/ui-config/resources")
public class UIConfigContentResource extends AdminApiResource {

    private static final Logger LOGGER = Logger.getLogger(UIConfigContentResource.class.getName());

    @Inject
    RightsResolver rightsResolver;

    @Inject
    Provider<User> userProvider;

    @Inject
    @Named("kramerius4")
    Provider<Connection> connectionProvider;


    // --------------------------------------------------------------------
    // GET
    // --------------------------------------------------------------------

    @GET
    @Path("{resourceKey:.+}")
    public Response getResource(@PathParam("resourceKey") String resourceKey) {
        try {
            User user = userProvider.get();
            if (!permitConfig(user)) {
                throw new cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException(
                        "user '%s' is not allowed to manage resources",
                        user.getLoginname());
            }
            DbUIConfigResourceService service = new DbUIConfigResourceService(connectionProvider);
            UIConfigResourceContent resource = service.load(resourceKey);
            if (resource == null) {
                throw new cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException(
                        "No such resource");
            }
            return Response.ok(new ByteArrayInputStream(resource.getContent()))
                    .type(resource.getContentType())
                    .header("Cache-Control", "no-cache")
                    .build();
        } catch (UIConfigException e) {
            LOGGER.log(Level.SEVERE, "Failed to load UI resource " + resourceKey, e);
            throw new InternalServerErrorException("Failed to load UI resource");
        }
    }


    // --------------------------------------------------------------------
    // PUT
    // --------------------------------------------------------------------

    @PUT
    @Path("{resourceKey:.+}")
    public Response saveResource(
            @PathParam("resourceKey") String resourceKey,
            @HeaderParam("Content-Type") String contentType,
            InputStream content) {
        try {
            User user = userProvider.get();

            if (!permitConfig(user)) {
                throw new cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException(
                        "user '%s' is not allowed to manage resources",
                        user.getLoginname());
            }
            DbUIConfigResourceService service = new DbUIConfigResourceService(connectionProvider);
            service.save(resourceKey, contentType, content);
            return Response.noContent().build();
        } catch (UIConfigException e) {
            LOGGER.log(Level.SEVERE, "Failed to save UI resource " + resourceKey, e);
            throw new InternalServerErrorException("Failed to save UI resource");
        }
    }

    // --------------------------------------------------------------------
    // HELPERS
    // --------------------------------------------------------------------

    private boolean permitConfig(User user) {
        if (user != null) {
            return rightsResolver.isActionAllowed(
                    user,
                    SecuredActions.A_ADMIN_READ.getFormalName(),
                    SpecialObjects.REPOSITORY.getPid(),
                    null,
                    ObjectPidsPath.REPOSITORY_PATH).flag();
        }
        return false;
    }
}