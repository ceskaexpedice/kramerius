package cz.incad.kramerius.rest.apiNew.client.v70;

import com.google.inject.Inject;
import cz.incad.kramerius.rest.apiNew.ConfigManager;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @see cz.incad.kramerius.rest.api.k5.client.info.InfoResource
 */
@Path("/client/v7.0/config")
public class ConfigResource extends ClientApiResource {

    public static Logger LOGGER = Logger.getLogger(ConfigResource.class.getName());

    @Inject
    private ConfigManager configService;

    @GET
    @Path("/{key}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProperty(@PathParam("key") String key) {
        try {
            String value = configService.getProperty(key);
            JSONObject json = new JSONObject();
            json.put(key, value);
            return Response.ok(json).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }
}
