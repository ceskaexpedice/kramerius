package cz.incad.kramerius.rest.apiNew.admin.v10;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.inject.Inject;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.rest.apiNew.ConfigManager;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;

/**
 * @see cz.incad.kramerius.rest.api.k5.client.info.InfoResource
 */
@Path("/admin/v7.0/config")
public class ConfigResource extends AdminApiResource {

    public static Logger LOGGER = Logger.getLogger(ConfigResource.class.getName());

    @Inject
    private ConfigManager configService;

    @javax.inject.Inject
    Provider<User> userProvider;


    boolean permitConfig(User user) {
        if (user != null)
            return this.rightsResolver.isActionAllowed(user,
                    SecuredActions.A_ADMIN_READ.getFormalName(),
                    SpecialObjects.REPOSITORY.getPid(), null,
                    ObjectPidsPath.REPOSITORY_PATH).flag();
        else
            return false;
    }

    @PUT
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setProperties(String values) {
        try {
            User user1 = this.userProvider.get();
            if (permitConfig(user1)) {
                JSONObject obj = new JSONObject(values);
                Map<String,String> map = new HashMap<>();
                obj.keySet().forEach(key-> {
                    map.put(key.toString(), obj.getString(key.toString()));
                });
                configService.setProperties(map);
                return Response.ok(obj.toString()).build();
            } else {
                throw new ForbiddenException("user '%s' is not allowed to manage config ", user1.getLoginname()); //403
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }


    @DELETE
    @Path("/{key}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response deleteProperty(@PathParam("key") String key) {
        try {
            User user1 = this.userProvider.get();
            if (permitConfig(user1)) {
                configService.deleteProperty(key);
                return Response.ok().build();
            } else {
                throw new ForbiddenException("user '%s' is not allowed to manage config ", user1.getLoginname()); //403
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }
    

    
    @PUT
    @Path("/{key}")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response setProperty(@PathParam("key") String key, String value) {
        try {
            User user1 = this.userProvider.get();
            if (permitConfig(user1)) {
                configService.setProperty(key, value);
                return Response.ok().build();
            } else {
                throw new ForbiddenException("user '%s' is not allowed to manage config ", user1.getLoginname()); //403
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    public static String escapeCharacters(String input) {
        StringBuilder result = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (c == '.' || c == '^' || c == '$' || c == '*' || c == '+' || c == '?' || c == '[' || c == ']' || c == '(' || c == ')') {
                result.append('\\').append(c);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }


    @GET
    @Path("/keys/{prefix}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getKeys(@PathParam("prefix") String prefix) {
        try {
            User user1 = this.userProvider.get();
            if (permitConfig(user1)) {
                List<String> keys = configService.getKeysByRegularExpression("^"+escapeCharacters(prefix));
                JSONArray jsonArray = new JSONArray();
                keys.stream().forEach(jsonArray::put);
                return Response.ok(jsonArray.toString()).build();
            } else {
                throw new ForbiddenException("user '%s' is not allowed to manage config ", user1.getLoginname()); //403
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }
    
    
    
    @GET
    @Path("/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProperty(@PathParam("key") String key) {
        try {
            User user1 = this.userProvider.get();
            if (permitConfig(user1)) {
                String value = configService.getProperty(key);
                JSONObject json = new JSONObject();
                json.put(key, value);
                return Response.ok(json).build();
            } else {
                throw new ForbiddenException("user '%s' is not allowed to manage config ", user1.getLoginname()); //403
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    
    
    @POST
    @Path("/getProperties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProperties(String values) {
        try {
            User user1 = this.userProvider.get();
            if (permitConfig(user1)) {
                JSONObject obj = new JSONObject(values);
                Map<String,String> map = configService.getProperties(obj.keySet());
                JSONObject retval = new JSONObject(map);
                return Response.ok(retval).build();
            } else {
                throw new ForbiddenException("user '%s' is not allowed to manage config ", user1.getLoginname()); //403
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
        
    }
    
    
    @POST
    @Path("/deleteProperties")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProperties(String values) {
        try {
            User user1 = this.userProvider.get();
            if (permitConfig(user1)) {
                JSONObject obj = new JSONObject(values);
                configService.deleteProperties(obj.keySet());
                return Response.ok(obj.toString()).build();
            } else {
                throw new ForbiddenException("user '%s' is not allowed to manage config ", user1.getLoginname()); //403
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
        
    }


}
