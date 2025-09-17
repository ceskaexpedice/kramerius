package cz.incad.kramerius.rest.apiNew.admin.v70.processes;

import com.google.inject.name.Named;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcess;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.new_api.ProcessManager;
import cz.incad.kramerius.rest.api.processes.utils.SecurityProcessUtils;
import cz.incad.kramerius.rest.apiNew.admin.v70.AdminApiResource;
import cz.incad.kramerius.rest.apiNew.admin.v70.ProcessSchedulingHelper;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.Role;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.utils.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/admin/v7.0/processes")
public class ProcessResource extends AdminApiResource {

    /** Special keyword for null value; If there is need to fill all program arguments and one of them must be null */
    public static final String NONE_KEYWORD = "-none-";

    private static final int MAX_TITLE_LENGTH = 1024;

    public static Logger LOGGER = Logger.getLogger(ProcessResource.class.getName());

    private static final Integer GET_BATCHES_DEFAULT_OFFSET = 0;
    private static final Integer GET_BATCHES_DEFAULT_LIMIT = 10;

    private static final Integer GET_LOGS_DEFAULT_OFFSET = 0;
    private static final Integer GET_LOGS_DEFAULT_LIMIT = 10;


    @Inject
    LRProcessManager lrProcessManager;

    @Inject
    DefinitionManager definitionManager;

    /*@Inject
    Provider<HttpServletRequest> requestProvider;*/

//    @Inject
//    Provider<User> loggedUsersSingleton;

    @javax.inject.Inject
    Provider<User> userProvider;

    @Inject
    @Named("new-index")
    SolrAccess solrAccess;

    @Inject
    RightsResolver rightsResolver;

    //TODO: Merge it in future
    @Inject
    ProcessManager processManager;

    @Inject
    ProcessSchedulingHelper processSchedulingHelper;

    // TODO pepo
    @Inject
    @javax.inject.Named("forward-client")
    private CloseableHttpClient apacheClient;

    /**
     * Returns list of users who have scheduled some process
     *
     * @return
     */
    @GET
    @Path("owners")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getOwners() {
        try {
            ForbiddenCheck.checkGeneral(userProvider.get(), rightsResolver);
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            JSONObject pcpOwners = processManagerClient.getOwners();
            JSONObject result = ProcessManagerMapper.mapOwners(pcpOwners);
            return Response.ok().entity(result.toString()).build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    @GET
    @Path("by_process_id/{process_id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getProcessByProcessId(@PathParam("process_id") String processId) {
        lrProcessManager.getSynchronizingLock().lock();
        try {
            String profileId = null; // TODO pepo
            ForbiddenCheck.checkByProfile(userProvider.get(), rightsResolver, definitionManager, profileId, true);
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            JSONObject pcpProcess = processManagerClient.getProcess(processId);
            if (pcpProcess == null) {
                throw new NotFoundException("there's no process with process_id=" + processId);
            }

            JSONObject result = ProcessManagerMapper.mapProcess(pcpProcess);
            return Response.ok().entity(result.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            lrProcessManager.getSynchronizingLock().unlock();
        }
    }

    @GET
    @Path("by_process_uuid/{process_uuid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getProcessByProcessUuid(@PathParam("process_uuid") String processUuid) {
        return getProcessByProcessId(processUuid);
    }

    /**
     * Get whole OUT log file
     *
     * @param processUuid
     */
    @GET
    @Path("by_process_uuid/{process_uuid}/logs/out")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getProcessLogsOutByProcessUuid(@PathParam("process_uuid") String processUuid,
                                                   @DefaultValue("out.txt") @QueryParam("fileName") String fileName) {
        ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
        InputStream logStream = processManagerClient.getProcessLog(processUuid, false);
        return Response.ok((StreamingOutput) output -> {
                    try (logStream) {
                        logStream.transferTo(output);
                    }
                }).header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                .build();
    }

    /**
     * Get whole ERR log file
     *
     * @param processUuid
     */
    @GET
    @Path("by_process_uuid/{process_uuid}/logs/err")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getProcessLogsErrByProcessUuid(@PathParam("process_uuid") String processUuid,
                                                   @DefaultValue("err.txt") @QueryParam("fileName") String fileName) {
        ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
        InputStream logStream = processManagerClient.getProcessLog(processUuid, true);
        return Response.ok((StreamingOutput) output -> {
                    try (logStream) {
                        logStream.transferTo(output);
                    }
                }).header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                .build();
    }

    /**
     * Nahrazuje _processes_logs_std_json.jsp, _processes_logs_std_json.jsp
     *
     * @param processUuid
     * @param offsetStr
     * @param limitStr
     * @return JSON with selected lines (defined by offset, limit) of the standard log
     */
    @GET
    @Path("by_process_uuid/{process_uuid}/logs/out/lines")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getProcessLogsOutLinesByProcessUuid(@PathParam("process_uuid") String processUuid,
                                                        @QueryParam("offset") String offsetStr,
                                                        @QueryParam("limit") String limitStr) {
        try {
            return getProcessLogsLinesByProcessUuid(processUuid, false, offsetStr, limitStr);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /**
     * Nahrazuje _processes_logs_err_json.jsp, _processes_logs_err_json.jsp
     *
     * @param processUuid
     * @param offsetStr
     * @param limitStr
     * @return JSON with selected lines (defined by offset, limit) of the error log
     */
    @GET
    @Path("by_process_uuid/{process_uuid}/logs/err/lines")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getProcessLogsErrLinesByProcessUuid(@PathParam("process_uuid") String processUuid,
                                                        @QueryParam("offset") String offsetStr,
                                                        @QueryParam("limit") String limitStr) {
        try {
            return getProcessLogsLinesByProcessUuid(processUuid, true, offsetStr, limitStr);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private Response getProcessLogsLinesByProcessUuid(String processUuid, boolean err, String offsetStr, String limitStr) {
        lrProcessManager.getSynchronizingLock().lock();
        try {
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            JSONObject processLogLines = processManagerClient.getProcessLogLines(processUuid, offsetStr, limitStr, err);
            processLogLines = ProcessManagerMapper.mapLogLines(processLogLines);
            return Response.ok().entity(processLogLines.toString()).build();
        } finally {
            lrProcessManager.getSynchronizingLock().unlock();
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
    @Path("batches")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getBatches(
            @QueryParam("offset") String offsetStr,
            @QueryParam("limit") String limitStr,
            @QueryParam("owner") String filterOwner,
            @QueryParam("from") String filterFrom,
            @QueryParam("until") String filterUntil,
            @QueryParam("state") String filterState
    ) {
        lrProcessManager.getSynchronizingLock().lock();
        try {
            ForbiddenCheck.checkGeneral(userProvider.get(), rightsResolver);
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            JSONObject pcpBatches = processManagerClient.getBatches(offsetStr, limitStr, filterOwner, filterFrom, filterUntil, filterState);

            JSONObject result = new JSONObject();
            result.put("offset", offsetStr);
            result.put("limit", limitStr);
            result.put("total_size", pcpBatches.getInt("totalSize"));
            JSONArray pcpBatchesArray = pcpBatches.getJSONArray("batches");
            JSONArray resultBatchesArray = new JSONArray();
            for (int i = 0; i < pcpBatchesArray.length(); i++) {
                JSONObject pcpBatchWithProcesses = pcpBatchesArray.getJSONObject(i);
                JSONObject resultBatchWithProcesses = ProcessManagerMapper.mapBatchWithProcesses(pcpBatchWithProcesses);
                resultBatchesArray.put(resultBatchWithProcesses);

            }
            result.put("batches", resultBatchesArray);
            return Response.ok().entity(result.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (ProcessManagerClientException e) {
            if(e.getErrorCode() == ErrorCode.INVALID_INPUT){
                throw new BadRequestException(e.getMessage());
            }else{
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new InternalErrorException(e.getMessage());
            }
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            lrProcessManager.getSynchronizingLock().unlock();
        }
    }

    @DELETE
    @Path("batches/by_first_process_id/{process_id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response deleteBatch(@PathParam("process_id") String processId) {
        lrProcessManager.getSynchronizingLock().lock();
        try {
            String profileId = null; // TODO pepo
            ForbiddenCheck.checkByProfile(userProvider.get(), rightsResolver, definitionManager, profileId, false);

            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            int deleted = processManagerClient.deleteBatch(processId);
            JSONObject result = new JSONObject();
            result.put("batch_id", processId);
            result.put("batch_token", "-"); // TODO pepo
            result.put("processes_deleted", deleted);

            return Response.ok().entity(result.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (ProcessManagerClientException e) {
            if(e.getErrorCode() == ErrorCode.NOT_FOUND){
                throw new NotFoundException(e.getMessage());
            }else{
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new InternalErrorException(e.getMessage());
            }
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            lrProcessManager.getSynchronizingLock().unlock();
        }
    }

    @DELETE
    @Path("batches/by_first_process_id/{process_id}/execution")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response killBatch(@PathParam("process_id") String processId) {
        lrProcessManager.getSynchronizingLock().lock();
        try {
            String profileId = null; // TODO pepo
            ForbiddenCheck.checkByProfile(userProvider.get(), rightsResolver, definitionManager, profileId, false);
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            int killed = processManagerClient.killBatch(processId);
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (ProcessManagerClientException e) {
            if(e.getErrorCode() == ErrorCode.NOT_FOUND){
                throw new NotFoundException(e.getMessage());
            }else{
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new InternalErrorException(e.getMessage());
            }
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            lrProcessManager.getSynchronizingLock().unlock();
        }
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
            /*
            {
              "defid" : "import",
              "params" : {
                "license" : "dnntt",
                "inputDataDir" : "/045b1250-7e47-11e0-add1-000d606f5dc6",
                "startIndexer" : true
              }
            }
            {
              "profileId" : "testPlugin1-big",
              "payload" : {
                "surname" : "Po",
                "name" : "Pe"
              },
              "ownerId" : "PePo"
            }
             */
            String profileId = null; // TODO pepo
            ForbiddenCheck.checkByProfileAndParamsPids(userProvider.get(), rightsResolver, definitionManager,
                    profileId, processDefinition, solrAccess);


            JSONObject pcpSchedule = ProcessManagerMapper.mapScheduleMainProcess(processDefinition, userProvider.get().getLoginname());
            /*
            boolean ret = true;
            if(ret){
                return Response.ok().build();
            }

             */

            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            String processId = processManagerClient.scheduleProcess(pcpSchedule);
            JSONObject result = new JSONObject();
            result.put("processId", processId);
            return Response.ok().entity(result.toString()).build();
            /*
            String parentProcessAuthToken = getParentProcessAuthToken();
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

             */
/*
            if (parentProcessAuthToken != null) { //run by "parent" process (more precisely it's "older sibling" process - the new process will be its sibling within same batch)
                ProcessManager.ProcessAboutToScheduleSibling parentProcess = processManager.getProcessAboutToScheduleSiblingByAuthToken(parentProcessAuthToken);
                if (parentProcess == null) {
                    throw new UnauthorizedException("invalid token"); //401
                }
                String userId = parentProcess.getOwnerId();
                String userName = parentProcess.getOwnerName();
                String batchToken = parentProcess.getBatchToken();
                List<String> paramsList = new ArrayList<>();
                paramsList.addAll(paramsToList(defid, params, (permitted) -> {

                }));
                String title = shortenIfTooLong(buildInitialProcessName(defid, paramsList), MAX_TITLE_LENGTH);
                return scheduleProcess(defid, paramsList, userId, userName, batchToken, title);
            } else { //run by user (through web client)
                AtomicBoolean pidPermitted = new AtomicBoolean(false);
                //System.out.println("process auth token NOT found");
                String batchToken = UUID.randomUUID().toString();
                List<String> paramsList = new ArrayList<>();
                paramsList.addAll(paramsToList(defid, params, flag -> {
                    if (flag) pidPermitted.getAndSet(true);
                }));

                //authentication
                User user = this.userProvider.get();
                LRProcessDefinition definition = this.definitionManager.getLongRunningProcessDefinition(defid);

                boolean permitted = SecurityProcessUtils.permitManager(rightsResolver, user) ||
                        SecurityProcessUtils.permitProcessByDefinedAction(rightsResolver, user, definition) || pidPermitted.get();

                if (!permitted) {
                    throw new ForbiddenException("user '%s' is not allowed to manage processes (missing role '%s', '%s')", user.getLoginname(), SecuredActions.A_PROCESS_EDIT.name(), SecuredActions.A_PROCESS_READ.name()); //403
                }

                String title = shortenIfTooLong(buildInitialProcessName(defid, paramsList), MAX_TITLE_LENGTH);

                return scheduleProcess(defid, paramsList, user.getLoginname(), user.getLoginname(), batchToken, title);
            }

 */
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    /*
    private Response scheduleProcess(String defid, List<String> params, String ownerId, String ownerName, String batchToken, String processName) {
        LRProcess newProcess = processSchedulingHelper.scheduleProcess(defid, params, ownerId, ownerName, batchToken, processName);
        URI uri = UriBuilder.fromResource(LRResource.class).path("{uuid}").build(newProcess.getUUID());
        return Response.created(uri).entity(lrPRocessToJSONObject(newProcess).toString()).build();
    }

     */

    /*
    private JSONObject lrPRocessToJSONObject(LRProcess lrProcess) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uuid", lrProcess.getUUID());

        jsonObject.put("pid", lrProcess.getPid()); //empty
        jsonObject.put("id", lrProcess.getDefinitionId());
        jsonObject.put("state", lrProcess.getProcessState().toString());
        //jsonObject.put("batchState", lrProcess.getBatchState().toString());
        jsonObject.put("name", lrProcess.getProcessName());
        if (lrProcess.getPlannedTime() > 0) {
            jsonObject.put("planned", Utils.toFormattedStringOrNull(lrProcess.getPlannedTime()));
        }
        jsonObject.put("userid", lrProcess.getLoginname()); //empty
        jsonObject.put("userFirstname", lrProcess.getFirstname()); //empty
        jsonObject.put("userSurname", lrProcess.getSurname()); //empty
        return jsonObject;
    }

     */

    //TODO: I18N
    /*
    private String buildInitialProcessName(String defId, List<String> params) {

        try {
            switch (defId) {
                case "new_process_api_test":
                    return String.format("Proces pro testování správy procesů (duration=%s s, processesInBatch=%s, finalState=%s)", params.get(0), params.get(1), params.get(2));
                case "new_indexer_index_object": {
                    String type = params.get(0);
                    String pid = params.get(1);
                    String title = params.get(3);//params.get(2) is ignoreInconsistentObjects!
                    return title != null
                            ? String.format("Indexace %s (%s, typ %s)", title, pid, type)
                            : String.format("Indexace %s (typ %s)", pid, type);
                }

                case "new_indexer_index_model": {
                    return String.format("Indexace %s (typ %s)", params.get(1), params.get(0));
                }

                case "set_policy": {
                    String scope = params.get(0);
                    String policy = params.get(1);
                    String pid = params.get(2);
                    String title = params.get(3);
                    return title != null
                            ? String.format("Změna viditelnosti %s (%s, %s, %s)", title, pid, policy, scope)
                            : String.format("Změna viditelnosti %s (%s, %s)", pid, policy, scope);
                }

                case "remove_policy": {
                    String scope = params.get(0);
                    String pid = params.get(1);
                    String title = params.get(2);
                    return title != null
                            ? String.format("Odebrání příznaku viditelnosti %s (%s, %s)", title, pid, scope)
                            : String.format("Odebrání příznaku viditelnosti %s (%s)", pid, scope);
                }

                case "processing_rebuild":
                    return "Přebudování Processing indexu";
                case "processing_rebuild_for_object":
                    return String.format("Aktualizace Processing indexu z FOXML pro objekt %s", params.get(0));
                case "import": {
                    return String.format("Import FOXML z %s ", params.get(0));
                }
                case "convert_and_import": {
                    return String.format("Import NDK METS z %s ", params.get(1));
                }
                case "add_license": {
                    return String.format("Přidání licence '%s' pro %s", params.get(0), params.get(1));
                }
                case "remove_license": {
                    return String.format("Odebrání licence '%s' pro %s", params.get(0), params.get(1));
                }
                case "flag_to_license": {
                    if (params.get(0).equals("true")) {
                        return String.format("Změna příznaku na licence a spuštění procesu mazání příznaku");
                    } else {
                        return String.format("Změna příznaku na licence");
                    }
                }

                case "nkplogs": {
                    return String.format("Generování NKP logů pro období %s - %s", params.get(0), params.get(1));
                }
                case "backup-collections": {
                    return String.format("Vytváření zálohy '%s' pro %s", params.get(0), params.get(1));
                }

                case "restore-collections": {
                    return String.format("Obnoveni ze  zálohy '%s'", params.get(0));
                }

                case "migrate-collections-from-k5": {
                    return String.format("Migrace sbírek z K5 instance - ('%s')", params.get(0));
                }

                case "sdnnt-sync": {
                    return "Synchronizace se SDNNT";
                }

                case "delete_tree": {
                    String pid = params.get(0);
                    String title = params.get(1);
                    return title != null
                            ? String.format("Smazání stromu %s (%s)", title, pid)
                            : String.format("Smazání stromu %s", pid);
                }
                default:
                    return null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

     */

/*
    private static String shortenIfTooLong(String string, int maxLength) {
        if (string == null || string.isEmpty() || string.length() <= maxLength) {
            return string;
        } else {
            String suffix = "...";
            return string.substring(0, maxLength - suffix.length()) + suffix;
        }
    }
*/


    /*
    private Integer extractOptionalParamPositiveInteger(JSONObject params, String paramName, int defaultValue) {
        if (params.has(paramName)) {
            try {
                int processesInBatch = params.getInt(paramName);
                if (processesInBatch < 1) {
                    throw new BadRequestException("invalid value (not a positive number) of %s: '%d'", paramName, processesInBatch);
                } else {
                    return processesInBatch;
                }
            } catch (JSONException e) {
                throw new BadRequestException("invalid value (not a number) of %s: '%s'", paramName, params.get(paramName));
            }
        } else {
            return defaultValue;
        }
    }*/



    /*
    private Boolean extractOptionalParamBoolean(JSONObject params, String paramName, boolean defaultValue) {
        if (params.has(paramName)) {
            return params.getBoolean(paramName);
        } else {
            return defaultValue;
        }
    }

     */
/*
    private Boolean extractMandatoryParamBoolean(JSONObject params, String paramName) {
        if (params.has(paramName)) {
            return params.getBoolean(paramName);
        } else {
            throw new BadRequestException("missing mandatory parameter %s: ", paramName);
        }
    }
*/
    /*
    private String extractMandatoryParamString(JSONObject params, String paramName) {
        String value = extractOptionalParamString(params, paramName, null);
        if (value == null) {
            throw new BadRequestException("missing mandatory parameter %s: ", paramName);
        } else {
            return value;
        }
    }*/

    /*
    private File extractMandatoryParamFileContainedInADir(JSONObject params, String paramName, File rootDir) {
        String value = extractOptionalParamString(params, paramName, null);
        if (value == null) {
            throw new BadRequestException("missing mandatory parameter %s: ", paramName);
        } else {
            return extractFileContainedInADirFromParamValue(value, paramName, rootDir);
        }
    }

     */

    /*
    private File extractOptionalParamFileContainedInADir(JSONObject params, String paramName, File rootDir) {
        String value = extractOptionalParamString(params, paramName, null);
        return value == null ? null : extractFileContainedInADirFromParamValue(value, paramName, rootDir);
    }

     */

    /*
    private File extractFileContainedInADirFromParamValue(String paramValue, String paramName, File rootDir) {
        //sanitize against problematic characters
        char[] forbiddenChars = new char[]{'~', '#', '%', '&', '{', '}', '<', '>', '*', '?', '$', '!', '@', '+', '`', '|', '=', ';', ' ', '\t'};
        for (char forbiddenChar : forbiddenChars) {
            if (paramValue.indexOf(forbiddenChar) != -1) {
                throw new BadRequestException("invalid value of %s (contains forbidden character '%s'): '%s'", paramName, forbiddenChar, paramValue);
            }
        }
        try {

            boolean canonical = KConfiguration.getInstance().getConfiguration().getBoolean("io.canonical.file", true);
            File paramFile = null;
            if (rootDir != null) {
                paramFile = canonical ? new File(rootDir, paramValue).getCanonicalFile() : new File(rootDir, paramValue);
            } else {
                paramFile = canonical ? new File(paramValue).getCanonicalFile() : new File(paramValue);
            }
            return paramFile;
        } catch (IOException e) { //protoze getCanonicalPath saha na filesystem
            throw new BadRequestException("invalid value of %s (IOException): '%s': %s", paramName, paramValue, e.getMessage());
        }
    }

     */




    /*
    private String extractOptionalParamWithValueFromEnum(JSONObject params, String paramName, Class enumClass, String defaultValue) {
        String value = extractOptionalParamString(params, paramName, defaultValue);
        if (value == null) {
            return null;
        } else {
            Object[] enumConstants = enumClass.getEnumConstants();
            for (int i = 0; i < enumConstants.length; i++) {
                String enumValue = enumConstants[i].toString();
                if (value.equals(enumValue)) {
                    return value;
                }
            }
            throw new BadRequestException("invalid value of %s: '%s'", paramName, value);
        }
    }

     */

    /*
    private String extractMandatoryParamWithValueFromEnum(JSONObject params, String paramName, Class enumClass) {
        String value = extractOptionalParamString(params, paramName, null);
        if (value == null) {
            throw new BadRequestException("missing mandatory parameter %s: ", paramName);
        } else {
            Object[] enumConstants = enumClass.getEnumConstants();
            for (int i = 0; i < enumConstants.length; i++) {
                String enumValue = enumConstants[i].toString();
                if (value.equals(enumValue)) {
                    return value;
                }
            }
            throw new BadRequestException("invalid value of %s: '%s'", paramName, value);
        }
    }

     */
/*
    public enum Policy {
        PRIVATE, PUBLIC
    }

 */
}
