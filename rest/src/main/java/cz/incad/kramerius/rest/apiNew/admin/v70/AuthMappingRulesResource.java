package cz.incad.kramerius.rest.apiNew.admin.v70;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.auth.AttributeRoleRulesService;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Administrative API for the authentication attribute mapping rules. */
@Path("/admin/v7.0/auth-mapping/rules")
public class AuthMappingRulesResource extends AdminApiResource {
    private static final Logger LOGGER = Logger.getLogger(AuthMappingRulesResource.class.getName());

    @Inject
    private AttributeRoleRulesService rulesService;
    @Inject
    private Provider<User> userProvider;
    @Inject
    private RightsResolver rightsResolver;

    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getRules() {
        checkPermission(SecuredActions.A_ADMIN_READ);
        try {
            return Response.ok(rulesService.getRules()).type(MediaType.APPLICATION_JSON + ";charset=utf-8")
                    .header("Cache-Control", "no-cache").build();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not load authentication attribute rules", e);
            throw new InternalErrorException("Could not load authentication attribute rules");
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response saveRules(String body) {
        checkPermission(SecuredActions.A_ROLES_EDIT);
        try {
            AttributeRoleRulesService.validate(body);
            rulesService.saveRules(body);
            return Response.ok(new JSONObject().put("saved", true).toString())
                    .type(MediaType.APPLICATION_JSON + ";charset=utf-8").build();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage(), e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Could not save authentication attribute rules", e);
            throw new InternalErrorException("Could not save authentication attribute rules");
        } catch (WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not save authentication attribute rules", e);
            throw new InternalErrorException("Could not save authentication attribute rules");
        }
    }

    private void checkPermission(SecuredActions action) {
        User user = userProvider.get();
        boolean allowed = user != null && rightsResolver.isActionAllowed(user, action.getFormalName(),
                SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH).flag();
        if (!allowed) {
            throw new cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException(
                    "user '%s' is not allowed to manage authentication rules", user == null ? "not_logged" : user.getLoginname());
        }
    }
}
