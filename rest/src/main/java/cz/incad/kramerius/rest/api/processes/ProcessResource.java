package cz.incad.kramerius.rest.api.processes;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.new_api.Filter;
import cz.incad.kramerius.processes.new_api.ProcessInBatch;
import cz.incad.kramerius.processes.new_api.ProcessManager;
import cz.incad.kramerius.rest.api.exceptions.*;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.IPAddressUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Logger;

@Path("/v6.0/processes")
public class ProcessResource {

    public static Logger LOGGER = Logger.getLogger(ProcessResource.class.getName());

    private static final Integer DEFAULT_OFFSET = 0;
    private static final Integer DEFAULT_LIMIT = 10;
    private static final boolean DEV_DISABLE_ACCESS_CONTROL_FOR_READ_ONLY_OPS = true;

    //TODO: proverit
    @Deprecated
    private static final String AUTH_TOKEN_HEADER_KEY = "auth-token";
    @Deprecated
    private static final String TOKEN_ATTRIBUTE_KEY = "token";

    //TODO: move url into configuration
    private static final String AUTH_URL = "https://api.kramerius.cloud/api/v1/auth/validate_token";
    private static final String AUTH_HEADER_CLIENT = "client";
    private static final String AUTH_HEADER_UID = "uid";
    private static final String AUTH_HEADER_ACCESS_TOKEN = "access-token";

    //TODO: prejmenovat role
    private static final String ROLE_LIST_PROCESSES = "kramerius_admin";
    private static final String ROLE_SCHEDULE_PROCESSES = "kramerius_admin";


    @Inject
    LRProcessManager lrProcessManager; //here only for scheduling

    @Inject
    DefinitionManager definitionManager; //process definitions

    @Inject
    ProcessManager processManager;

    @Inject
    LoggedUsersSingleton loggedUsersSingleton;

    @Inject
    RightsResolver rightsResolver;

    @Inject
    Provider<User> userProvider;

    @Inject
    Provider<HttpServletRequest> requestProvider;

    /**
     * Returns filtered batches
     *
     * @param offsetStr   offset
     * @param limitStr    limit
     * @param filterOwner filter to batches run by user with this id (login)
     * @param filterFrom  filter to batches started after this datetime, format is 2019-01-01T00:00:00
     * @param filterUntil filter to batches finished before this datetime, format is 2019-12-31T23:59:59
     * @param filterState filter to batches with this state (possible values are PLANNED, RUNNING, FINISHED, KILLED or FAILED)
     * @return
     */
    @GET
    @Path("/batches")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getBatches(
            @QueryParam("offset") String offsetStr,
            @QueryParam("limit") String limitStr,
            @QueryParam("owner") String filterOwner,
            @QueryParam("from") String filterFrom,
            @QueryParam("until") String filterUntil,
            @QueryParam("state") String filterState
    ) {
        try {
            //access control with basic access authentification (deprecated)
            if (!DEV_DISABLE_ACCESS_CONTROL_FOR_READ_ONLY_OPS) {
                String loggedUserKey = findLoggedUserKey();
                User user = this.loggedUsersSingleton.getUser(loggedUserKey);
                if (user == null) {
                    throw new UnauthorizedException("user==null"); //401
                }
                boolean allowed = rightsResolver.isActionAllowed(user, SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH);
                if (!allowed) {
                    throw new ActionNotAllowed("user '%s' is not allowed to manage processes", user.getLoginname()); //403
                }
            }

            //nova autentizace
            AuthenticatedUser user = getAuthenticatedUser();
            //System.out.println(user);
            String role = ROLE_LIST_PROCESSES; //TODO
            if (!user.getRoles().contains(role)) {
                throw new ActionNotAllowed("user '%s' is not allowed to manage processes (missing role '%s')", user.getName(), role); //403
            }

            //offset & limit
            int offset = DEFAULT_OFFSET;
            if (StringUtils.isAnyString(offsetStr)) {
                try {
                    offset = Integer.valueOf(offsetStr);
                    if (offset < 0) {
                        throw new BadRequestException("offset must be zero or positive, '%s' is not", offsetStr);
                    }
                } catch (NumberFormatException e) {
                    throw new BadRequestException("offset must be integer, '%s' is not", offsetStr);
                }
            }
            int limit = DEFAULT_LIMIT;
            if (StringUtils.isAnyString(limitStr)) {
                try {
                    limit = Integer.valueOf(limitStr);
                    if (limit < 1) {
                        throw new BadRequestException("limit must be positive, '%s' is not", limitStr);
                    }
                } catch (NumberFormatException e) {
                    throw new BadRequestException("limit must be integer, '%s' is not", limitStr);
                }
            }

            //filter
            Filter filter = new Filter();
            if (StringUtils.isAnyString(filterOwner)) {
                filter.owner = filterOwner;
            }
            if (StringUtils.isAnyString(filterFrom)) {
                filter.from = parseLocalDateTime(filterFrom);
            }
            if (StringUtils.isAnyString(filterUntil)) {
                filter.until = parseLocalDateTime(filterUntil);
            }
            if (StringUtils.isAnyString(filterState)) {
                filter.stateCode = toBatchStateCode(filterState);
            }

            //response size
            int totalSize = this.processManager.getBatchesCount(filter);
            JSONObject result = new JSONObject();
            result.put("offset", offset);
            result.put("limit", limit);
            result.put("total_size", totalSize);

            //batch & process data
            List<ProcessInBatch> pibs = this.processManager.getProcessesInBatches(filter, offset, limit);
            List<Batch> batches = extractBatchesWithProcesses(pibs);
            JSONArray batchesJson = new JSONArray();
            for (Batch batch : batches) {
                JSONObject batchJson = batchToJson(batch);
                batchesJson.put(batchJson);
            }
            result.put("batches", batchesJson);
            return Response.ok().entity(result.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new GenericApplicationException(e.getMessage());
        }
    }

    private AuthenticatedUser getAuthenticatedUser() {
        String client = requestProvider.get().getHeader(AUTH_HEADER_CLIENT);
        String uid = requestProvider.get().getHeader(AUTH_HEADER_UID);
        String accessToken = requestProvider.get().getHeader(AUTH_HEADER_ACCESS_TOKEN);
        if (!StringUtils.isAnyString(accessToken) || !StringUtils.isAnyString(uid) || !StringUtils.isAnyString(client)) {
            throw new ProxyAuthenticationRequiredException("missing one of headaers '%s', '%s', or '%s'", AUTH_HEADER_CLIENT, AUTH_HEADER_UID, AUTH_HEADER_ACCESS_TOKEN);
        }
        try {
            URL url = new URL(AUTH_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setInstanceFollowRedirects(false);
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);
            con.setRequestProperty(AUTH_HEADER_CLIENT, client);
            con.setRequestProperty(AUTH_HEADER_UID, uid);
            con.setRequestProperty(AUTH_HEADER_ACCESS_TOKEN, accessToken);
            int status = con.getResponseCode();
            if (status != 200) {
                System.out.println("status: " + status);
                throw new GenericApplicationException("error communicationg with authentification service (response status %d)", status);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            JSONObject jsonObject = new JSONObject(content.toString());
            //System.out.println(jsonObject.toString());
            if (jsonObject.getBoolean("success")) {
                JSONObject data = jsonObject.getJSONObject("data");
                String id = data.getString("uid");
                String name = data.getString("name");
                List<String> roles = Collections.emptyList();
                if (data.has("roles") && data.get("roles") != null && !data.isNull("roles")) {
                    roles = commaSeparatedItemsToList(data.getString("roles"));
                }
                return new AuthenticatedUser(id, name, roles);
            } else { //session timeout/logged out/ ...
                //TODO: otestovat (jak vypada json v pripade chyby?)
                System.out.println(jsonObject.toString());
                throw new UnauthorizedException(jsonObject.toString());
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new GenericApplicationException("error parsing response from authentification service ", e);
        } catch (IOException e) {
            throw new GenericApplicationException("error communicationg with authentification service", e);
        }
    }

    private List<String> commaSeparatedItemsToList(String commaSeparated) {
        List<String> result = new ArrayList<>();
        if (commaSeparated == null || commaSeparated.trim().isEmpty()) {
            return result;
        }
        String[] items = commaSeparated.split(",");
        for (String item : items) {
            result.add(item.trim());
        }
        return result;
    }

    /**
     * Schedules new process
     *
     * @param processDefinition
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response scheduleProcess(JSONObject processDefinition) {
        try {
            if (processDefinition == null) {
                throw new BadRequestException("missing process definition");
            }
            if (!processDefinition.has("type")) {
                throw new BadRequestException("empty type");
            }
            String type = processDefinition.getString("type");
            JSONObject params = new JSONObject();
            if (processDefinition.has("params")) {
                params = processDefinition.getJSONObject("params");
            }
            //System.out.println(params);
            List<String> paramsList = paramsToList(type, params);

            //nova autentizace
            AuthenticatedUser user = getAuthenticatedUser();
            //System.out.println(user);
            String role = ROLE_SCHEDULE_PROCESSES;
            if (!user.getRoles().contains(role)) {
                throw new ActionNotAllowed("user '%s' is not allowed to manage processes (missing role '%s')", user.getName(), role); //403
            }
            return scheduleProcess(type, paramsList, user.getId(), user.getName());
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new GenericApplicationException(e.getMessage());
        }
    }

    private List<String> paramsToList(String type, JSONObject params) {
        switch (type) {
            case "test": {
                Integer duration = 1;
                if (params.has("duration")) {
                    //TODO: test when not int
                    duration = params.getInt("duration");
                }
                Integer processesInBatch = 1;
                if (params.has("processesInBatch")) {
                    //TODO: test when not int
                    processesInBatch = params.getInt("processesInBatch");
                }
                Boolean fail = false;
                if (params.has("fail")) {
                    //TODO: test when not boolean
                    fail = params.getBoolean("fail");
                }
                List<String> array = new ArrayList<>();
                array.add(duration.toString());
                array.add(processesInBatch.toString());
                array.add(fail.toString());
                return array;
            }
            default: {
                throw new BadRequestException("unsupported process type '%s'", type);
            }
        }
    }

    private Response scheduleProcess(String type, List<String> params, String ownerId, String ownerName) {
        LRProcessDefinition definition = processDefinition(type);
        if (definition == null) {
            throw new BadRequestException("process definition for type '%' not found", type);
        }

        //access control
        /*String loggedUserKey = findLoggedUserKey();
        User user = this.loggedUsersSingleton.getUser(loggedUserKey);
        if (user == null) {
            throw new UnauthorizedException("user==null"); //401
        }
        SecuredActions actionFromDef = securedAction(type, definition);
        //System.out.println(actionFromDef);
        //TODO: proverit, vypada to, ze actionFromDef byva null a tak se chytne druha klauzule
        boolean allowed = (rightsResolver.isActionAllowed(user, SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH)
                || (actionFromDef != null && rightsResolver.isActionAllowed(user, actionFromDef.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH)));
        if (!allowed) {
            throw new ActionNotAllowed("user '%s' is not allowed to manage processes", user.getLoginname()); //403
        }*/

        LRProcess newProcess = definition.createNewProcess(authToken(), groupToken());
        //System.out.println("newProcess: " + newProcess);
        //tohle vypada, ze se je k nicemu, ve vysledku se to jen uklada do databaze do processes.params_mapping a to ani ne vzdy
        // select planned, params_mapping from processes where params_mapping!='' order by planned desc limit 10;
        //newProcess.setLoggedUserKey(loggedUserKey);
        newProcess.setParameters(params);
        //newProcess.setUser(user);
        newProcess.setOwnerId(ownerId);
        newProcess.setOwnerName(ownerName);

        if (definition.isInputTemplateDefined()) { //'plain' process
            //System.out.println("plain process");
            newProcess.planMe(new Properties(), IPAddressUtils.getRemoteAddress(this.requestProvider.get(), KConfiguration.getInstance().getConfiguration()));
            //lrProcessManager.updateAuthTokenMapping(newProcess, loggedUserKey);
            URI uri = UriBuilder.fromResource(LRResource.class).path("{uuid}").build(newProcess.getUUID());
            return Response.created(uri).entity(lrPRocessToJSONObject(newProcess).toString()).build();
        } else { //'parametrized' process
            //System.out.println("parametrized process");
            Properties props = new Properties();
            /*for (Iterator iterator = mapping.keys(); iterator.hasNext(); ) {
                String key = (String) iterator.next();
                try {
                    props.put(key.toString(), mapping.get(key).toString());
                } catch (JSONException e) {
                    throw new GenericApplicationException(e.getMessage());
                }
            }*/
            newProcess.planMe(props, IPAddressUtils.getRemoteAddress(this.requestProvider.get(), KConfiguration.getInstance().getConfiguration()));
            //lrProcessManager.updateAuthTokenMapping(newProcess, loggedUserKey);
            URI uri = UriBuilder.fromResource(LRResource.class).path("{uuid}").build(newProcess.getUUID());
            return Response.created(uri).entity(lrPRocessToJSONObject(newProcess).toString()).build();
        }
    }

    //TODO: proverit fungovani
    private LRProcessDefinition processDefinition(String processType) {
        definitionManager.load();
        LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition(processType);
        return definition;
    }

    //TODO: proverit fungovani
    private SecuredActions securedAction(String processType, LRProcessDefinition definition) {
        return definition.getSecuredAction() != null ? SecuredActions.findByFormalName(definition.getSecuredAction()) : SecuredActions.findByFormalName(processType);
    }

    private JSONObject lrPRocessToJSONObject(LRProcess lrProcess) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", lrProcess.getUUID());
        jsonObject.put("pid", lrProcess.getPid()); //empty
        jsonObject.put("type", lrProcess.getDefinitionId());
        jsonObject.put("state", lrProcess.getProcessState().toString());
        //jsonObject.put("batchState", lrProcess.getBatchState().toString());
        jsonObject.put("name", lrProcess.getProcessName());
        if (lrProcess.getPlannedTime() > 0) {
            jsonObject.put("planned", toFormattedStringOrNull(lrProcess.getPlannedTime()));
        }
        jsonObject.put("userid", lrProcess.getLoginname()); //empty
        jsonObject.put("userFirstname", lrProcess.getFirstname()); //empty
        jsonObject.put("userSurname", lrProcess.getSurname()); //empty
        return jsonObject;
    }

    public String authToken() {
        return requestProvider.get().getHeader(AUTH_TOKEN_HEADER_KEY);
    }

    public String groupToken() {
        HttpServletRequest request = requestProvider.get();
        String gtoken = request.getHeader(TOKEN_ATTRIBUTE_KEY);
        return gtoken;
    }

    private String findLoggedUserKey() {
        //TODO: otestovat, nebo zmenit
        userProvider.get(); //TODO: neni uplne zrejme, proc tohle vodlat. Co se deje v AbstractLoggedUserProvider a LoggedUsersSingletonImpl vypada zmatecne
        return (String) requestProvider.get().getSession().getAttribute(UserUtils.LOGGED_USER_KEY_PARAM);
    }

    private LocalDateTime parseLocalDateTime(String string) {
        if (string == null) {
            return null;
        } else {
            try {
                return LocalDateTime.parse(string, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new BadRequestException("incorrect date-time format '%s'", string);
            }
        }
    }

    private List<Batch> extractBatchesWithProcesses(List<ProcessInBatch> allPibs) {
        Map<String, List<ProcessInBatch>> pibListByToken = new HashMap<>();
        for (ProcessInBatch pib : allPibs) {
            if (!pibListByToken.containsKey(pib.batchToken)) {
                pibListByToken.put(pib.batchToken, new ArrayList<>());
            }
            List<ProcessInBatch> pibList = pibListByToken.get(pib.batchToken);
            pibList.add(pib);
        }
        List<Batch> result = new ArrayList<>(pibListByToken.size());
        for (List<ProcessInBatch> pibsOfBatch : pibListByToken.values()) {
            ProcessInBatch firstPib = pibsOfBatch.get(0);
            Batch batch = new Batch();
            batch.token = firstPib.batchToken;
            batch.id = firstPib.batchId;
            batch.stateCode = firstPib.batchStateCode;
            batch.planned = firstPib.batchPlanned;
            batch.started = firstPib.batchStarted;
            batch.finished = firstPib.batchFinished;
            batch.ownerLogin = firstPib.batchOwnerLogin;
            batch.ownerFirstname = firstPib.batchOwnerFirstname;
            batch.ownerSurname = firstPib.batchOwnerSurname;
            for (ProcessInBatch pib : pibsOfBatch) {
                Process process = new Process();
                process.uuid = pib.processUuid;
                process.id = pib.processId;
                process.defid = pib.processDefid;
                process.name = pib.processName;
                process.stateCode = pib.processStateCode;
                process.planned = pib.processPlanned;
                process.started = pib.processStarted;
                process.finished = pib.processFinished;
                batch.processes.add(process);
            }
            result.add(batch);
        }
        Collections.sort(result, (o1, o2) -> -1 * o1.planned.compareTo(o2.planned));
        return result;
    }

    private JSONObject batchToJson(Batch batch) {
        JSONObject json = new JSONObject();
        //batch
        JSONObject batchJson = new JSONObject();
        batchJson.put("token", batch.token);
        batchJson.put("id", batch.id);
        batchJson.put("state", toBatchStateName(batch.stateCode));
        batchJson.put("planned", toFormattedStringOrNull(batch.planned));
        batchJson.put("started", toFormattedStringOrNull(batch.started));
        batchJson.put("finished", toFormattedStringOrNull(batch.finished));
        batchJson.put("owner_login", batch.ownerLogin);
        batchJson.put("owner_firstname", batch.ownerFirstname);
        batchJson.put("owner_surname", batch.ownerSurname);
        json.put("batch", batchJson);
        //processes
        JSONArray processArray = new JSONArray();
        for (Process process : batch.processes) {
            JSONObject processJson = new JSONObject();
            processJson.put("id", process.id);
            processJson.put("uuid", process.uuid);
            processJson.put("defid", process.defid);
            processJson.put("name", process.name);
            processJson.put("state", toProcessStateName(process.stateCode));
            processJson.put("planned", toFormattedStringOrNull(process.planned));
            processJson.put("started", toFormattedStringOrNull(process.started));
            processJson.put("finished", toFormattedStringOrNull(process.finished));
            processArray.put(processJson);
        }
        json.put("processes", processArray);
        return json;
    }

    private String toProcessStateName(Integer stateCode) {
        switch (stateCode) {
            case 0:
                return "NOT_RUNNING";
            case 1:
                return "RUNNING";
            case 2:
                return "FINISHED";
            case 3:
                return "FAILED";
            case 4:
                return "KILLED";
            case 5:
                return "PLANNED";
            case 9:
                return "WARNING";
            default:
                return "UNKNOWN";
        }
    }

    private String toFormattedStringOrNull(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
        }
    }

    private String toFormattedStringOrNull(long timeInSeconds) {

        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(timeInSeconds, 0, ZoneOffset.UTC);
        return toFormattedStringOrNull(localDateTime);
    }

    private String toBatchStateName(Integer batchStateCode) {
        switch (batchStateCode) {
            case 0:
                return "PLANNED";
            case 1:
                return "RUNNING";
            case 2:
                return "FINISHED";
            case 3:
                return "FAILED";
            case 4:
                return "KILLED";
            default:
                return "UNKNOWN";
        }
    }

    private int toBatchStateCode(String batchStateName) {
        switch (batchStateName) {
            case "PLANNED":
                return 0;
            case "RUNNING":
                return 1;
            case "FINISHED":
                return 2;
            case "FAILED":
                return 3;
            case "KILLED":
                return 4;
            default:
                throw new BadRequestException("unknown state '%s'", batchStateName);
        }
    }

    public static final class Batch {
        public String token;
        public String id;
        public Integer stateCode;
        public LocalDateTime planned;
        public LocalDateTime started;
        public LocalDateTime finished;
        public String ownerLogin;
        public String ownerFirstname;
        public String ownerSurname;
        List<Process> processes = new ArrayList<>();
    }

    public static final class Process {
        public String id;
        public String uuid;
        public String defid;
        public String name;
        public Integer stateCode;
        public LocalDateTime planned;
        public LocalDateTime started;
        public LocalDateTime finished;
    }
}
