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
import cz.incad.kramerius.rest.apiNew.admin.v70.reharvest.ReharvestItem.TypeOfReharvset;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.timestamps.Timestamp;
import cz.incad.kramerius.timestamps.TimestampStore;
import cz.incad.kramerius.rest.apiNew.client.v70.libs.Instances;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import cz.incad.kramerius.utils.ProcessTokenSupport;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

/** CDK-specific process scheduling API. */
@Path("/admin/v7.0/cdk")
public class CDKProcessResource extends AdminApiResource {

    private static final Logger LOGGER = Logger.getLogger(CDKProcessResource.class.getName());
    private static final String DESTINATION_URL_PROPERTY = "cdk.process.destinationUrl";
    private static final String ITERATION_PATH = "api/cdk/v7.0/forward/sync/solr";
    private static final String SELECT_PATH = "api/cdk/v7.0/forward/sync/solr/select";
    private static final String TIMESTAMP_PATH = "/api/admin/v7.0/connected/%s/timestamp";

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

    @Inject
    private TimestampStore timestampStore;

    @POST
    @Path("{library}/process/harvest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response harvest(@PathParam("library") String library, JSONObject options) {
        return schedule(library, Action.HARVEST, options, buildHarvest(library, Action.HARVEST, options == null ? new JSONObject() : options));
    }

    @POST
    @Path("{library}/process/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response update(@PathParam("library") String library, JSONObject options) {
        checkReadOnlyWorkMode();
        validateLibrary(library);
        JSONObject requestOptions = options == null ? new JSONObject() : options;
        long updates = countSourceUpdates(library, requestOptions);
        if (updates == 0) {
            JSONObject result = new JSONObject();
            result.put("library", library);
            result.put("action", Action.UPDATE.path);
            result.put("scheduled", false);
            result.put("updates", 0);
            return Response.ok(result.toString()).build();
        }
        return schedule(library, Action.UPDATE, requestOptions, buildUpdatePayload(library, requestOptions), updates);
    }

    @POST
    @Path("{library}/process/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response delete(@PathParam("library") String library, JSONObject options) {
        return schedule(library, Action.DELETE, options, buildDelete(library, options == null ? new JSONObject() : options));
    }

    @POST
    @Path("{library}/process/compare")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response compare(@PathParam("library") String library, JSONObject options) {
        JSONObject requestOptions = options == null ? new JSONObject() : options;
        return schedule(library, Action.COMPARE, requestOptions, buildCompare(library, requestOptions));
    }

    @POST
    @Path("{library}/process/reharvest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response reharvest(@PathParam("library") String library, JSONObject options) {
        JSONObject requestOptions = options == null ? new JSONObject() : options;
        List<String> requestedLibraries = parseLibraries(library);
        requestedLibraries.forEach(this::validateLibrary);
        String normalizedLibraries = String.join(",", requestedLibraries);
        return schedule(normalizedLibraries, Action.REHARVEST, requestOptions, buildReharvest(normalizedLibraries, requestOptions));
    }

    @POST
    @Path("process/reharvest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response deleteReharvest(JSONObject options) {
        JSONObject requestOptions = options == null ? new JSONObject() : options;
        String requestedType = requestOptions.optString("type", "");
        TypeOfReharvset type;
        try {
            type = TypeOfReharvset.valueOf(requestedType);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown reharvest type: " + requestedType);
        }
        if (!type.isDeletingReharvest()) {
            throw new BadRequestException("Library-independent reharvest endpoint only accepts delete types");
        }
        return schedule("", Action.REHARVEST, requestOptions, buildReharvest("", requestOptions));
    }

    private Response schedule(String library, Action action, JSONObject options, Map<String, String> payload) {
        return schedule(library, action, options, payload, -1);
    }

    private Response schedule(String library, Action action, JSONObject options, Map<String, String> payload, long updates) {
        checkReadOnlyWorkMode();
        if (action == Action.REHARVEST) {
            if (!library.isEmpty()) {
                parseLibraries(library).forEach(this::validateLibrary);
            }
        } else {
            validateLibrary(library);
        }
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
            result.put("scheduled", true);
            if (updates >= 0) result.put("updates", updates);
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

    /** Checks the source for documents indexed since the last successful CDK update. */
    private long countSourceUpdates(String library, JSONObject options) {
        String sourcePrefix = "cdk.collections.sources." + library + ".";
        String forwardUrl = configuration().getString(sourcePrefix + "forwardurl");
        if (forwardUrl == null || forwardUrl.trim().isEmpty()) {
            throw new BadRequestException("Unknown CDK source or missing forward URL for library: " + library);
        }
        try {
            Timestamp latest = timestampStore.findLatest(library);
            String filterQuery = options.optString("filterQuery", "").trim();
            if (latest != null) {
                String indexedAfter = "indexed:[" + latest.getDate().toInstant().toString() + " TO *]";
                filterQuery = filterQuery.isEmpty() ? indexedAfter : indexedAfter + " AND (" + filterQuery + ")";
            }
            URIBuilder uri = new URIBuilder(forwardUrl.replaceAll("/+$", "") + "/" + SELECT_PATH)
                    .addParameter("q", "*:*" )
                    .addParameter("rows", "0")
                    .addParameter("wt", "json");
            if (!filterQuery.isEmpty()) uri.addParameter("fq", filterQuery);

            HttpGet request = new HttpGet(uri.build());
            String apiKey = configuration().getString(sourcePrefix + "apikey", "");
            if (apiKey != null && !apiKey.trim().isEmpty()) request.setHeader("X-API-KEY", apiKey);
            return apacheClient.execute(request, response -> {
                int status = response.getCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (status < 200 || status >= 300) {
                    throw new IOException("Source update check failed with HTTP " + status + ": " + body);
                }
                return new JSONObject(body).getJSONObject("response").getLong("numFound");
            });
        } catch (javax.ws.rs.WebApplicationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to check source updates for " + library, e);
            throw new InternalErrorException("Unable to check source updates: " + e.getMessage());
        }
    }



    private Map<String, String> buildUpdatePayload(String library, JSONObject options) {
        Map<String, String> payload = new LinkedHashMap<>();
        String sourcePrefix = "cdk.collections.sources." + library + ".";
        String forwardUrl = configuration().getString(sourcePrefix + "forwardurl");
        if (forwardUrl == null || forwardUrl.trim().isEmpty()) {
            throw new BadRequestException("Unknown CDK source or missing forward URL for library: " + library);
        }
        String apiKey = configuration().getString(sourcePrefix + "apikey", "");
        String iterationUrl = forwardUrl.replaceAll("/+$", "") + "/" + ITERATION_PATH;
        payload.put("configSource", "/cz/incad/kramerius/services/workers/replicate/configurations/default_k7_v2_update.xml");
        payload.put("destinationUrl",KConfiguration.getInstance().getSolrSearchHost());

        payload.put("iterationUrl", iterationUrl);
        payload.put("iterationApiKey", apiKey);
        payload.put("iterationDl", library);

        String iterationId = configuration().getString(sourcePrefix + "iterationId", "pid");

        payload.put("iterationId", iterationId);
        payload.put("iterationFQuery", options.optString("filterQuery", ""));
        payload.put("iterationWorkingtime", options.optString("iterationWorkingtime", ""));
        payload.put("showConfigurationOnly", options.optString("showConfigurationOnly", "false"));
        String timestampBaseUrl = configuration().getString("cdk.process.timestampBaseUrl", KConfiguration.getInstance().getApplicationURL());
        payload.put("timestampUrl", timestampBaseUrl.replaceAll("/+$", "") + String.format(TIMESTAMP_PATH, library));
        payload.put("comparingIdentifier", options.optString("comparingIdentifier", "pid"));
        payload.put("feederBatchSize", options.optString("feederBatchSize", ""));
        payload.put("showEffectiveConfigurationOnly", options.optString("showEffectiveConfigurationOnly", "false"));

        return payload;
    }


    private Map<String, String> buildHarvest(String library, Action action, JSONObject options) {


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

    private Map<String, String> buildCompare(String library, JSONObject options) {
        Map<String, String> payload = new LinkedHashMap<>();
        String sourcePrefix = "cdk.collections.sources." + library + ".";
        String forwardUrl = configuration().getString(sourcePrefix + "forwardurl");
        if (forwardUrl == null || forwardUrl.trim().isEmpty()) {
            throw new BadRequestException("Unknown CDK source or missing forward URL for library: " + library);
        }
        payload.put("sourceUrl", forwardUrl.replaceAll("/+$", "") + "/" + ITERATION_PATH + "/");
        payload.put("sourceApiKey", configuration().getString(sourcePrefix + "apikey", ""));
        payload.put("destinationUrl", KConfiguration.getInstance().getSolrSearchHost());
        payload.put("library", library);
        payload.put("iterationId", options.optString("iterationId",
                configuration().getString(sourcePrefix + "iterationId", "pid")));
        Object requestedModels = options.opt("models");
        if (requestedModels instanceof JSONArray) {
            JSONArray modelArray = (JSONArray) requestedModels;
            StringBuilder models = new StringBuilder();
            for (int i = 0; i < modelArray.length(); i++) {
                if (models.length() > 0) models.append(',');
                models.append(modelArray.getString(i));
            }
            payload.put("models", models.toString());
        } else if (requestedModels != null && requestedModels != JSONObject.NULL) {
            payload.put("models", requestedModels.toString());
        }
        payload.put("license", options.optString("license", ""));
        payload.put("deleteMissing", String.valueOf(options.optBoolean("deleteMissing", false)));
        payload.put("onlyModelsWithMoreInCdk",
                String.valueOf(options.optBoolean("onlyModelsWithMoreInCdk", false)));
        payload.put("rows", options.optString("rows", "300"));
        payload.put("reportEvery", options.optString("reportEvery", "5000"));
        payload.put("onlyShowConfiguration", String.valueOf(options.optBoolean("onlyShowConfiguration", false)));
        return payload;
    }

    private Map<String, String> buildReharvest(String library, JSONObject options) {
        String pid = options.optString("pid", "").trim();
        if (pid.isEmpty()) {
            throw new BadRequestException("Reharvest requires pid");
        }

        String ownPidPath;
        if (options.has("own_pid_path") && !options.isNull("own_pid_path")) {
            ownPidPath = options.optString("own_pid_path", "").trim();
        } else {
            ownPidPath = findOwnPidPath(pid);
        }
        if (ownPidPath.isEmpty()) {
            throw new BadRequestException("Could not find own_pid_path in CDK index for PID: " + pid);
        }

        String requestedType = options.optString("type", TypeOfReharvset.children.name());
        TypeOfReharvset type;
        try {
            type = TypeOfReharvset.valueOf(requestedType);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown reharvest type: " + requestedType);
        }
        if (type == TypeOfReharvset.live_conflict) {
            throw new BadRequestException("live_conflict cannot be scheduled as a reharvest");
        }

        String adminApi = configuration().getString("api.admin.v7.point");
        if (adminApi == null || adminApi.trim().isEmpty()) {
            throw new InternalErrorException("Missing api.admin.v7.point configuration");
        }
        adminApi = adminApi.replaceAll("/+$", "");

        Map<String, String> payload = new LinkedHashMap<>();

        payload.put("destinationUrl", KConfiguration.getInstance().getSolrSearchHost());
        payload.put("iterationUrl", KConfiguration.getInstance().getSolrSearchHost());
        payload.put("proxyApiUrl", adminApi + "/connected");
        payload.put("pid", pid);
        payload.put("ownPidPath", ownPidPath);
        payload.put("rootPid", options.optString("rootPid", rootPidFromPath(ownPidPath)));
        payload.put("type", type.name());
        if (!library.isEmpty()) {
            payload.put("libraries", library);
        }

        for (String parameter : new String[]{"iterationUrl", "iterationType", "iterationRows", "iterationBatch"}) {
            if (options.has(parameter) && !options.isNull(parameter)) {
                payload.put(parameter, options.get(parameter).toString());
            }
        }
        if (options.has("onlyShowConfiguration")) {
            payload.put("onlyShowConfiguration", String.valueOf(options.optBoolean("onlyShowConfiguration")));
        }
        if (options.has("maxItemsToDelete") && !options.isNull("maxItemsToDelete")) {
            int maxItemsToDelete = options.getInt("maxItemsToDelete");
            if (maxItemsToDelete < 1) {
                throw new BadRequestException("maxItemsToDelete must be a positive integer");
            }
            payload.put("maxItemsToDelete", String.valueOf(maxItemsToDelete));
        }
        if (options.has("commitWaitMillis") && !options.isNull("commitWaitMillis")) {
            long commitWaitMillis = options.getLong("commitWaitMillis");
            if (commitWaitMillis < 0) {
                throw new BadRequestException("commitWaitMillis must be zero or greater");
            }
            payload.put("commitWaitMillis", String.valueOf(commitWaitMillis));
        }
        return payload;
    }

    private String findOwnPidPath(String pid) {
        try {
            URIBuilder uri = new URIBuilder(KConfiguration.getInstance().getSolrSearchHost() + "/select")
                    .addParameter("q", "pid:\"" + pid.replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
                    .addParameter("fl", "own_pid_path")
                    .addParameter("rows", "1")
                    .addParameter("wt", "json");
            HttpGet request = new HttpGet(uri.build());
            return apacheClient.execute(request, response -> {
                int status = response.getCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (status < 200 || status >= 300) {
                    throw new IOException("CDK index lookup failed with HTTP " + status + ": " + body);
                }
                JSONArray documents = new JSONObject(body).getJSONObject("response").getJSONArray("docs");
                return documents.length() == 0 ? "" : documents.getJSONObject(0).optString("own_pid_path", "").trim();
            });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to find own_pid_path in CDK index for PID " + pid, e);
            throw new InternalErrorException("Unable to find own_pid_path in CDK index for PID " + pid + ": " + e.getMessage());
        }
    }

    private String rootPidFromPath(String ownPidPath) {
        int slash = ownPidPath.indexOf('/');
        return slash >= 0 ? ownPidPath.substring(0, slash) : ownPidPath;
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

    private List<String> parseLibraries(String libraries) {
        if (libraries == null || libraries.trim().isEmpty()) {
            throw new BadRequestException("At least one CDK library is required");
        }
        List<String> parsed = Arrays.stream(libraries.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (parsed.isEmpty() || parsed.size() != libraries.split(",").length) {
            throw new BadRequestException("Invalid CDK library list");
        }
        return parsed;
    }

    private enum Action {
        HARVEST("harvest", "cdk-migration"),
        UPDATE("update", "cdk-update-migration"),
        DELETE("delete", "cdk-delete-library"),
        COMPARE("compare", "cdk-compare-library"),
        REHARVEST("reharvest", "cdk-reharvest");

        private final String path;
        private final String profileId;

        Action(String path, String profileId) {
            this.path = path;
            this.profileId = profileId;
        }
    }
}
