package cz.incad.kramerius.rest.api.processes;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessDefinition;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.mock.ProcessApiTestProcess;
import cz.incad.kramerius.processes.new_api.*;
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
import java.io.InputStream;
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

@Path("/v6.0/admin/processes")
public class ProcessResource {

    public static Logger LOGGER = Logger.getLogger(ProcessResource.class.getName());

    private static final Integer DEFAULT_OFFSET = 0;
    private static final Integer DEFAULT_LIMIT = 10;

    //TODO: proverit
    @Deprecated
    private static final String AUTH_TOKEN_HEADER_KEY = "auth-token";
    @Deprecated
    private static final String TOKEN_ATTRIBUTE_KEY = "token";

    private static final String HEADER_PROCESS_AUTH_TOKEN = "process-auth-token";

    //TODO: move url into configuration
    private static final String AUTH_URL = "https://api.kramerius.cloud/api/v1/auth/validate_token";

    //TODO: prejmenovat role podle spravy uctu
    private static final String ROLE_SCHEDULE_PROCESSES = "kramerius_admin";
    private static final String ROLE_LIST_PROCESSES = "kramerius_admin";
    private static final String ROLE_LIST_PROCESS_OWNERS = "kramerius_admin";
    private static final String ROLE_DELETE_PROCESSES = "kramerius_admin";


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
     * Returns list of users who have scheduled some process
     *
     * @return
     */
    @GET
    @Path("/owners")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getOwners() {
        try {
            //autentizace
            AuthenticatedUser user = getAuthenticatedUser();
            String role = ROLE_LIST_PROCESS_OWNERS;
            if (!user.getRoles().contains(role)) {
                throw new ActionNotAllowed("user '%s' is not allowed to manage processes (missing role '%s')", user.getName(), role); //403
            }
            //get data from db
            List<ProcessOwner> owners = this.processManager.getProcessesOwners();
            //sort
            owners.sort((o1, o2) -> {
                if (o1.name.startsWith("_") && o1.name.startsWith("_")) {
                    return o1.name.compareTo(o2.name);
                } else if (o1.name.startsWith("_")) {
                    return 1;
                } else {
                    return -1;
                }
            });
            //convert to JSON
            JSONArray ownersJson = new JSONArray();
            for (ProcessOwner owner : owners) {
                JSONObject ownerJson = new JSONObject();
                ownerJson.put("id", owner.id);
                ownerJson.put("name", owner.name);
                ownersJson.put(ownerJson);
            }
            JSONObject result = new JSONObject();
            result.put("owners", ownersJson);
            //return
            return Response.ok().entity(result.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new GenericApplicationException(e.getMessage());
        }
    }

    @DELETE
    @Path("/batches/by_first_process_id/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response deleteBatch(@PathParam("id") String processId) {
        try {
            //autentizace
            AuthenticatedUser user = getAuthenticatedUser();
            String role = ROLE_DELETE_PROCESSES;
            if (!user.getRoles().contains(role)) {
                throw new ActionNotAllowed("user '%s' is not allowed to manage processes (missing role '%s')", user.getName(), role); //403
            }
            //id
            Integer processIdInt = null;
            if (StringUtils.isAnyString(processId)) {
                try {
                    processIdInt = Integer.valueOf(processId);
                } catch (NumberFormatException e) {
                    throw new BadRequestException("process_id must be integer, '%s' is not", processId);
                }
            }
            //get batch data from db
            Batch batch = this.processManager.getBatchByFirstProcessId(processIdInt);
            if (batch == null) {
                throw new BadRequestException("batch with first-process-id %d doesn't exist", processIdInt);
            }
            //check batch is deletable
            String batchState = toBatchStateName(batch.stateCode);
            if (!isDeletableState(batchState)) {
                throw new BadRequestException("batch in state %s cannot be deleted", batchState);
            }
            //delete processes in batch
            int deleted = this.processManager.deleteBatchByBatchToken(batch.token);
            //return
            JSONObject result = new JSONObject();
            result.put("batch_id", batch.firstProcessId);
            result.put("batch_token", batch.token);
            result.put("processes_deleted", deleted);
            return Response.ok().entity(result.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new GenericApplicationException(e.getMessage());
        }
    }

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
            checkAccessControlByBasicAccessAuth();

            //autentizace
            AuthenticatedUser user = getAuthenticatedUser();
            String role = ROLE_LIST_PROCESSES;
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
            List<BatchWithProcesses> batchWithProcesses = extractBatchesWithProcesses(pibs);
            JSONArray batchesJson = new JSONArray();
            for (BatchWithProcesses batch : batchWithProcesses) {
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

    private void checkAccessControlByBasicAccessAuth() {
        boolean disabled = true;
        if (!disabled) {
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
    }

    private AuthenticatedUser getAuthenticatedUser() throws ProxyAuthenticationRequiredException {
        ClientAuthHeaders authHeaders = ClientAuthHeaders.extract(requestProvider);
        //System.out.println(authHeaders);
        try {
            URL url = new URL(AUTH_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setInstanceFollowRedirects(false);
            con.setConnectTimeout(1000);
            con.setReadTimeout(1000);
            con.setRequestProperty(ClientAuthHeaders.AUTH_HEADER_CLIENT, authHeaders.getClient());
            con.setRequestProperty(ClientAuthHeaders.AUTH_HEADER_UID, authHeaders.getUid());
            con.setRequestProperty(ClientAuthHeaders.AUTH_HEADER_ACCESS_TOKEN, authHeaders.getAccessToken());
            int status = con.getResponseCode();

            //error with not 200
            if (status != 200) {
                String message = "response status " + status;
                String body = inputstreamToString(con.getErrorStream());
                System.err.println(body);
                if (!body.isEmpty()) {
                    JSONObject bodyJson = new JSONObject(body);
                    if (bodyJson.has("errors")) {
                        JSONArray errors = bodyJson.getJSONArray("errors");
                        if (errors.length() > 0) {
                            message = errors.getString(0);
                        }
                    }
                }
                throw new GenericApplicationException("error communicationg with authentification service: %s", message);
            }
            String body = inputstreamToString(con.getInputStream());
            JSONObject bodyJson = new JSONObject(body);

            //error with 200 but not success
            if (!bodyJson.getBoolean("success")) {
                String message = "";
                if (bodyJson.has("errors")) {
                    JSONArray errors = bodyJson.getJSONArray("errors");
                    if (errors.length() > 0) {
                        message = errors.getString(0);
                    }
                }
                throw new GenericApplicationException("error communicationg with authentification service: %s", message);
            }

            //success
            JSONObject data = bodyJson.getJSONObject("data");
            String id = data.getString("uid");
            String name = data.getString("name");
            List<String> roles = Collections.emptyList();
            if (data.has("roles") && data.get("roles") != null && !data.isNull("roles")) {
                roles = commaSeparatedItemsToList(data.getString("roles"));
            }
            return new AuthenticatedUser(id, name, roles);
        } catch (WebApplicationException e) {
            throw e;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new GenericApplicationException("error parsing response from authentification service ", e);
        } catch (IOException e) {
            throw new GenericApplicationException("error communicationg with authentification service", e);
        }
    }

    private String inputstreamToString(InputStream in) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = reader.readLine()) != null) {
                content.append(inputLine);
            }
            reader.close();
            return content.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
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
            String processAuthToken = requestProvider.get().getHeader(HEADER_PROCESS_AUTH_TOKEN);
            if (processDefinition == null) {
                throw new BadRequestException("missing process definition");
            }
            if (!processDefinition.has("defid")) {
                throw new BadRequestException("empty defid");
            }
            String defid = processDefinition.getString("defid");
            JSONObject params = new JSONObject();
            if (processDefinition.has("params")) {
                params = processDefinition.getJSONObject("params");
            }
            if (processAuthToken != null) { //spousti proces (novy proces tedy bude jeho sourozenec ve stejne davce)
                //autorizace
                ProcessManager.ProcessAboutToScheduleSibling originalProcess = processManager.getProcessAboutToScheduleSiblingByAuthToken(processAuthToken);
                if (originalProcess == null) {
                    throw new UnauthorizedException("invalid token"); //401
                }
                String userId = originalProcess.getOwnerId();
                String userName = originalProcess.getOwnerName();
                String batchToken = originalProcess.getBatchToken();
                List<String> paramsList = new ArrayList<>();
                String newProcessAuthToken = UUID.randomUUID().toString();
                paramsList.add(newProcessAuthToken); //TODO: presunout mimo paremetry procesu, ale spravovane komponentou, co procesy spousti
                paramsList.addAll(paramsToList(defid, params));
                return scheduleProcess(defid, paramsList, userId, userName, batchToken, newProcessAuthToken);
            } else { //spousti user (pres weboveho klienta)
                String batchToken = UUID.randomUUID().toString();
                List<String> paramsList = new ArrayList<>();
                String newProcessAuthToken = UUID.randomUUID().toString();
                paramsList.add(newProcessAuthToken); //TODO: presunout mimo paremetry procesu, ale spravovane komponentou, co procesy spousti
                paramsList.addAll(paramsToList(defid, params));
                //autentizace
                AuthenticatedUser user = getAuthenticatedUser();
                //autorizace
                String role = ROLE_SCHEDULE_PROCESSES;
                if (!user.getRoles().contains(role)) {
                    throw new ActionNotAllowed("user '%s' is not allowed to manage processes (missing role '%s')", user.getName(), role); //403
                }
                return scheduleProcess(defid, paramsList, user.getId(), user.getName(), batchToken, newProcessAuthToken);
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new GenericApplicationException(e.getMessage());
        }
    }

    private List<String> paramsToList(String id, JSONObject params) {
        switch (id) {
            case ProcessApiTestProcess.ID: {
                //duration (of every process in batch)
                Integer duration = 1;
                String durationKey = ProcessApiTestProcess.PARAM_DURATION;
                if (params.has(durationKey)) {
                    try {
                        duration = params.getInt(durationKey);
                        if (duration < 1) {
                            throw new BadRequestException("invalid value (not a positive number) of %s: '%d'", durationKey, duration);
                        }
                    } catch (JSONException e) {
                        throw new BadRequestException("invalid value (not a number) of %s: '%s'", durationKey, params.get(durationKey));
                    }
                }
                //number of processes in batch
                Integer processesInBatch = 1;
                String processesInBatchKey = ProcessApiTestProcess.PARAM_PROCESSES_IN_BATCH;
                if (params.has(processesInBatchKey)) {
                    try {
                        processesInBatch = params.getInt(processesInBatchKey);
                        if (processesInBatch < 1) {
                            throw new BadRequestException("invalid value (not a positive number) of %s: '%d'", processesInBatchKey, processesInBatch);
                        }
                    } catch (JSONException e) {
                        throw new BadRequestException("invalid value (not a number) of %s: '%s'", processesInBatchKey, params.get(processesInBatchKey));
                    }
                }
                //processes' final state
                ProcessApiTestProcess.FinalState finalState = ProcessApiTestProcess.FinalState.FINISHED;
                String finalStateKey = ProcessApiTestProcess.PARAM_FINAL_STATE;
                if (params.has(finalStateKey)) {
                    String finalStateStr = params.getString(finalStateKey);
                    try {
                        finalState = ProcessApiTestProcess.FinalState.valueOf(finalStateStr);
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException("invalid value of %s: '%s'", finalStateKey, finalStateStr);
                    }
                }
                List<String> array = new ArrayList<>();
                array.add(duration.toString());
                array.add(processesInBatch.toString());
                array.add(finalState.name());
                return array;
            }
            default: {
                throw new BadRequestException("unsupported process id '%s'", id);
            }
        }
    }

    private Response scheduleProcess(String defid, List<String> params, String ownerId, String ownerName, String batchToken, String newProcessAuthToken) {
        LRProcessDefinition definition = processDefinition(defid);
        if (definition == null) {
            throw new BadRequestException("process definition for defid '%s' not found", defid);
        }
        String authToken = authToken(); //jen pro ilustraci, jak funguje stare api a jak se jmenovala hlavicka
        //System.out.println("authToken: " + authToken);
        String groupToken = groupToken(); //jen pro ilustraci, jak funguje stare api a jak se jmenovala hlavicka
        groupToken = batchToken;
        //System.out.println("groupToken: " + groupToken);

        LRProcess newProcess = definition.createNewProcess(authToken, groupToken);
        //System.out.println("newProcess: " + newProcess);
        //tohle vypada, ze se je k nicemu, ve vysledku se to jen uklada do databaze do processes.params_mapping a to ani ne vzdy
        // select planned, params_mapping from processes where params_mapping!='' order by planned desc limit 10;
        //newProcess.setLoggedUserKey(loggedUserKey);
        newProcess.setParameters(params);
        //newProcess.setUser(user);
        newProcess.setOwnerId(ownerId);
        newProcess.setOwnerName(ownerName);

        Properties properties = definition.isInputTemplateDefined()
                ? new Properties() //'plain' process
                : extractPropertiesForParametrizedProcess(); //'parametrized' process
        Integer processId = newProcess.planMe(properties, IPAddressUtils.getRemoteAddress(this.requestProvider.get(), KConfiguration.getInstance().getConfiguration()));
        if (processId == null) {
            throw new RuntimeException("error scheduling new process");
        }
        processManager.setProcessAuthToken(processId, newProcessAuthToken);
        //lrProcessManager.updateAuthTokenMapping(newProcess, loggedUserKey);
        URI uri = UriBuilder.fromResource(LRResource.class).path("{uuid}").build(newProcess.getUUID());
        return Response.created(uri).entity(lrPRocessToJSONObject(newProcess).toString()).build();
    }

    private Properties extractPropertiesForParametrizedProcess() {
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
        return props;
    }

    //TODO: proverit fungovani, prejmenovat
    private LRProcessDefinition processDefinition(String id) {
        definitionManager.load();
        LRProcessDefinition definition = definitionManager.getLongRunningProcessDefinition(id);
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
        jsonObject.put("id", lrProcess.getDefinitionId());
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

    @Deprecated
    private String authToken() {
        return requestProvider.get().getHeader(AUTH_TOKEN_HEADER_KEY);
    }

    @Deprecated
    private String groupToken() {
        return requestProvider.get().getHeader(TOKEN_ATTRIBUTE_KEY);
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

    private List<BatchWithProcesses> extractBatchesWithProcesses(List<ProcessInBatch> allPibs) {
        Map<String, List<ProcessInBatch>> pibListByToken = new HashMap<>();
        for (ProcessInBatch pib : allPibs) {
            if (!pibListByToken.containsKey(pib.batchToken)) {
                pibListByToken.put(pib.batchToken, new ArrayList<>());
            }
            List<ProcessInBatch> pibList = pibListByToken.get(pib.batchToken);
            pibList.add(pib);
        }
        List<BatchWithProcesses> result = new ArrayList<>(pibListByToken.size());
        for (List<ProcessInBatch> pibsOfBatch : pibListByToken.values()) {
            ProcessInBatch firstPib = pibsOfBatch.get(0);
            BatchWithProcesses batchWithProcesses = new BatchWithProcesses();
            batchWithProcesses.token = firstPib.batchToken;
            batchWithProcesses.firstProcessId = firstPib.batchId;
            batchWithProcesses.stateCode = firstPib.batchStateCode;
            batchWithProcesses.planned = firstPib.batchPlanned;
            batchWithProcesses.started = firstPib.batchStarted;
            batchWithProcesses.finished = firstPib.batchFinished;
            batchWithProcesses.ownerId = firstPib.batchOwnerId;
            batchWithProcesses.ownerName = firstPib.batchOwnerName;
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
                batchWithProcesses.processes.add(process);
            }
            result.add(batchWithProcesses);
        }
        Collections.sort(result, (o1, o2) -> -1 * o1.planned.compareTo(o2.planned));
        return result;
    }

    private JSONObject batchToJson(BatchWithProcesses batchWithProcesses) {
        JSONObject json = new JSONObject();
        //batch
        JSONObject batchJson = new JSONObject();
        batchJson.put("token", batchWithProcesses.token);
        batchJson.put("id", batchWithProcesses.firstProcessId);
        batchJson.put("state", toBatchStateName(batchWithProcesses.stateCode));
        batchJson.put("planned", toFormattedStringOrNull(batchWithProcesses.planned));
        batchJson.put("started", toFormattedStringOrNull(batchWithProcesses.started));
        batchJson.put("finished", toFormattedStringOrNull(batchWithProcesses.finished));
        batchJson.put("owner_id", batchWithProcesses.ownerId);
        batchJson.put("owner_name", batchWithProcesses.ownerName);
        json.put("batch", batchJson);
        //processes
        JSONArray processArray = new JSONArray();
        for (Process process : batchWithProcesses.processes) {
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

    private boolean isDeletableState(String batchState) {
        String[] deletableStates = new String[]{"FINISHED", "FAILED", "KILLED"};
        return batchState != null && Arrays.asList(deletableStates).contains(batchState);
    }

    public static final class BatchWithProcesses extends Batch {
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
