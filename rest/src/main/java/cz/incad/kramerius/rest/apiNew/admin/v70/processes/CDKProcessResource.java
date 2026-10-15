package cz.incad.kramerius.rest.apiNew.admin.v70.processes;

import com.google.inject.name.Named;
import cz.incad.kramerius.processes.client.ErrorCode;
import cz.incad.kramerius.processes.client.ProcessManagerClient;
import cz.incad.kramerius.processes.client.ProcessManagerClientException;
import cz.incad.kramerius.processes.client.ProcessManagerMapper;
import cz.incad.kramerius.processes.definition.ProcessDefinitionManager;
import cz.incad.kramerius.rest.apiNew.admin.v70.AdminApiResource;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.commons.configuration.Configuration;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** CDK-specific process scheduling API. */
@Path("/admin/v7.0/cdk/{library}/process")
public class CDKProcessResource extends AdminApiResource {

    private static final Logger LOGGER = Logger.getLogger(CDKProcessResource.class.getName());
    private static final String DESTINATION_URL_PROPERTY = "cdk.process.destinationUrl";
    private static final String ITERATION_PATH = "api/cdk/v7.0/forward/sync/solr";
    //private static final String HARVEST_CONFIG = "/cz/incad/kramerius/services/workers/replicate/configurations/default_k7_v2.xml";

    @Inject
    @Named("forward-client")
    private CloseableHttpClient apacheClient;

    @Inject
    private ProcessDefinitionManager definitionManager;

    @Inject
    private Provider<User> cdkUserProvider;

    @Inject
    private RightsResolver cdkRightsResolver;

    @Inject
    private Instances libraries;

//    @POST
//    @Path("harvest")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
//    public Response harvest(@PathParam("library") String library, JSONObject options) {
//        return schedule(library, Action.HARVEST, options, buildUpdate(library, Action.HARVEST, options == null ? new JSONObject() : options));
//    }

    @POST
    @Path("update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response update(@PathParam("library") String library, JSONObject options) {
        return schedule(library, Action.HARVEST, options, buildUpdate(library, Action.HARVEST, options == null ? new JSONObject() : options));
    }

    @POST
    @Path("delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response delete(@PathParam("library") String library, JSONObject options) {
        return schedule(library, Action.DELETE, options, buildDelete(library, options == null ? new JSONObject() : options));
    }

    private Response schedule(String library, Action action, JSONObject options, Map<String, String> payload) {
        checkReadOnlyWorkMode();
        validateLibrary(library);
        try {
            //Map<String, String> payload = buildPayload(library, action, options == null ? new JSONObject() : options);
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            JSONObject profile = processManagerClient.getProfile(action.profileId);
            JSONObject plugin = processManagerClient.getPlugin(profile.getString(ProcessManagerMapper.PCP_PLUGIN_ID));
            JSONArray scheduledProfiles = plugin.optJSONArray(ProcessManagerMapper.PCP_SCHEDULED_PROFILES);

            ForbiddenCheck.checkByProfileAndParamsPids(
                    cdkUserProvider.get(), cdkRightsResolver, definitionManager, action.profileId,
                    new JSONObject(payload), scheduledProfiles, null);

            JSONObject schedule = new JSONObject();
            schedule.put(ProcessManagerMapper.PCP_PROFILE_ID, action.profileId);
            schedule.put(ProcessManagerMapper.PCP_PAYLOAD, new JSONObject(payload));
            schedule.put(ProcessManagerMapper.PCP_OWNER_ID_SCH, cdkUserProvider.get().getLoginname());

            String processId = processManagerClient.scheduleProcess(schedule);
            JSONObject result = new JSONObject();
            result.put(ProcessManagerMapper.PCP_PROCESS_ID, processId);
            result.put("library", library);
            result.put("action", action.path);
            return Response.status(Response.Status.ACCEPTED).entity(result.toString()).build();
        } catch (ProcessManagerClientException e) {
            if (e.getErrorCode() == ErrorCode.NOT_FOUND) {
                throw new NotFoundException(e.getMessage());
            }
            if (e.getErrorCode() == ErrorCode.INVALID_INPUT) {
                throw new BadRequestException(e.getMessage());
            }
            LOGGER.log(Level.SEVERE, "Unable to schedule CDK process " + action.path, e);
            throw new InternalErrorException(e.getMessage());
        } catch (javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to schedule CDK process " + action.path, e);
            throw new InternalErrorException(e.getMessage());
        }
    }



    private Map<String, String> buildUpdate(String library, Action action, JSONObject options) {


        Map<String, String> payload = new LinkedHashMap<>();
        String sourcePrefix = "cdk.collections.sources." + library + ".";
        String forwardUrl = configuration().getString(sourcePrefix + "forwardurl");
        if (forwardUrl == null || forwardUrl.trim().isEmpty()) {
            throw new BadRequestException("Unknown CDK source or missing forward URL for library: " + library);
        }
        String apiKey = configuration().getString(sourcePrefix + "apikey", "");
        String iterationUrl = forwardUrl.replaceAll("/+$", "") + "/" + ITERATION_PATH;
        payload.put("configSource", "/cz/incad/kramerius/services/workers/replicate/configurations/default_k7_v2.xml");
        payload.put("destinationUrl",KConfiguration.getInstance().getSolrSearchHost());

        payload.put("iterationUrl", iterationUrl);
        payload.put("iterationApiKey", apiKey);
        payload.put("iterationDl", library);

        String iterationId = configuration().getString(sourcePrefix + "iterationId", "pid");

        payload.put("iterationId", iterationId);
        payload.put("iterationFQuery", options.optString("filterQuery", ""));
        payload.put("iterationWorkingtime", options.optString("iterationWorkingtime", ""));
        payload.put("showConfigurationOnly", options.optString("showConfigurationOnly", ""));

        return payload;
    }

    private Map<String, String> buildDelete(String library, JSONObject options) {

        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("destinationUrl",KConfiguration.getInstance().getSolrSearchHost());
        payload.put("library", library);
        payload.put("filterQuery", options.optString("filterQuery", ""));
        payload.put("rows", options.optString("rows", "300"));

        String sourcePrefix = "cdk.collections.sources." + library + ".";
        String iterationId = configuration().getString(sourcePrefix + "iterationId", "pid");
        payload.put("iterationId", iterationId);

        payload.put("reportEvery", options.optString("reportEvery", "5000"));
        payload.put("onlyShowConfiguration", String.valueOf(options.optBoolean("onlyShowConfiguration", false)));
        return payload;
    }

    private Configuration configuration() {
        return KConfiguration.getInstance().getConfiguration();
    }

    private void validateLibrary(String library) {
        if (library == null || !library.matches("[A-Za-z0-9_-]+")) {
            throw new BadRequestException("Invalid library acronym");
        }
        if (libraries.find(library) == null) {
            throw new BadRequestException("Unknown CDK library: " + library);
        }
    }

    private enum Action {
        HARVEST("harvest", "cdk-migration"),
        DELETE("delete", "cdk-delete-library");

        private final String path;
        private final String profileId;

        Action(String path, String profileId) {
            this.path = path;
            this.profileId = profileId;
        }
    }
}
