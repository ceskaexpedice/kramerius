package cz.incad.kramerius.rest.apiNew.admin.v70.processes;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.processes.*;
import cz.incad.kramerius.processes.mock.ProcessApiTestProcess;
import cz.incad.kramerius.processes.new_api.*;
import cz.incad.kramerius.rest.api.processes.LRResource;
import cz.incad.kramerius.rest.api.processes.utils.SecurityProcessUtils;
import cz.incad.kramerius.rest.apiNew.admin.v70.*;
import cz.incad.kramerius.rest.apiNew.admin.v70.processes.mapper.ProcessManagerMapper;
import cz.incad.kramerius.rest.apiNew.exceptions.*;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.ForbiddenException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;
import cz.incad.kramerius.security.*;
import cz.incad.kramerius.statistics.StatisticReport;
import cz.incad.kramerius.utils.IOUtils;
import cz.incad.kramerius.utils.StringUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.kramerius.searchIndex.indexer.execution.IndexationType;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.inject.name.Named;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
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
            //authentication
            //AuthenticatedUser user = getAuthenticatedUserByOauth();
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());

            boolean permitted = SecurityProcessUtils.permitManager(this.rightsResolver, user) || SecurityProcessUtils.permitReader(this.rightsResolver, user);
            if (!permitted) {
                throw new ForbiddenException("user '%s' is not allowed to manage processes (missing action '%s' or '%s')", user.getLoginname(), SecuredActions.A_PROCESS_EDIT.name(), SecuredActions.A_PROCESS_READ.name()); //403
            }
            
            //get data from db
            //List<ProcessOwner> owners = this.processManager.getProcessesOwners();
            //get data from db
            // TODO pepo List<ProcessOwner> owners = this.processManager.getProcessesOwners();
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            JSONObject json = processManagerClient.getOwners();
            List<cz.incad.kramerius.rest.apiNew.admin.v70.processes.mapper.ProcessOwner> owners = ProcessManagerMapper.mapOwners(json);

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
            for (cz.incad.kramerius.rest.apiNew.admin.v70.processes.mapper.ProcessOwner owner : owners) {
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
            //authentication
            //AuthenticatedUser user = getAuthenticatedUserByOauth();
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());

            //id
            /*
            Integer processIdInt = null;
            if (StringUtils.isAnyString(processId)) {
                try {
                    processIdInt = Integer.valueOf(processId);
                } catch (NumberFormatException e) {
                    throw new BadRequestException("process_id must be a number, '%s' is not", processId);
                }
            }

             */
            //get process (& it's batch) data from db
            // TODO pepo ProcessInBatch processInBatch = processManager.getProcessInBatchByProcessId(processIdInt);
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            JSONObject process = processManagerClient.getProcess(processId);
            cz.incad.kramerius.rest.apiNew.admin.v70.processes.mapper.ProcessInBatch processInBatch = ProcessManagerMapper.mapProcess(process);
            //get process (& it's batch) data from db
            //ProcessInBatch processInBatch = processManager.getProcessInBatchByProcessId(processIdInt);

            if (processInBatch == null) {
                throw new NotFoundException("there's no process with process_id=" + processId);
            }
            
            //authorization
            /* TODO pepo
            LRProcess lrProcess = this.lrProcessManager.getLongRunningProcess(processInBatch.processUuid);
            boolean permitted = SecurityProcessUtils.permitManager(rightsResolver, user) ||
                                SecurityProcessUtils.permitReader(rightsResolver, user) ||
                                SecurityProcessUtils.permitProcessByDefinedAction(rightsResolver, user, SecurityProcessUtils.processDefinition(this.definitionManager, lrProcess.getDefinitionId()));
            if (!permitted) {
                throw new ForbiddenException("user '%s' is not allowed to manage processes (missing action '%s', '%s')", user.getLoginname(), SecuredActions.A_PROCESS_EDIT.name(), SecuredActions.A_PROCESS_READ.name()); //403
            }

             */


//            JSONObject result = processInBatchToJson(processInBatch);
            JSONObject result = ProcessManagerMapper.processInBatchToJson(process);
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
        lrProcessManager.getSynchronizingLock().lock();
        try {
            //authentication
            //AuthenticatedUser user = getAuthenticatedUserByOauth();
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());
            
            ProcessInBatch processInBatch = this.processManager.getProcessInBatchByProcessUUid(processUuid);

            if (processInBatch == null) {
                throw new NotFoundException("there's no process with process_uuid=" + processUuid);
            }

            
            LRProcess lrProcess = this.lrProcessManager.getLongRunningProcess(processInBatch.processUuid);
            boolean permitted = SecurityProcessUtils.permitManager(rightsResolver, user) ||
                                SecurityProcessUtils.permitReader(rightsResolver, user) ||
                                SecurityProcessUtils.permitProcessByDefinedAction(rightsResolver, user, SecurityProcessUtils.processDefinition(this.definitionManager, lrProcess.getDefinitionId()));
            if (!permitted) {
                throw new ForbiddenException("user '%s' is not allowed to manage processes (missing action '%s', '%s')", user.getLoginname(), SecuredActions.A_PROCESS_EDIT.name(), SecuredActions.A_PROCESS_READ.name()); //403
            }
            
            JSONObject result = processInBatchToJson(processInBatch);
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

        /* TODO pepo
        try {
            return getProcessLogsFileByProcessUuid(processUuid, ProcessLogsHelper.LogType.OUT, fileName);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }*/
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
        try {
            return getProcessLogsFileByProcessUuid(processUuid, ProcessLogsHelper.LogType.ERR, fileName);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private Response getProcessLogsFileByProcessUuid(String processUuid, ProcessLogsHelper.LogType logType, String fileName) {
        //resource is secured by difficulty of guessing uuid, also client needs this to be accessible without authentication
        //access to process data
        LRProcess lrProces = lrProcessManager.getLongRunningProcess(processUuid);
        if (lrProces == null) {
            throw new BadRequestException("process with uuid " + processUuid + " not found");
        }
        ProcessLogsHelper processLogsHelper = new ProcessLogsHelper(lrProces);
        InputStream processInputStream = processLogsHelper.getLogsFileWhole(logType);
        return Response.ok().entity(processInputStream)
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
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
            return getProcessLogsLinesByProcessUuid(processUuid, ProcessLogsHelper.LogType.OUT, offsetStr, limitStr);
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
            return getProcessLogsLinesByProcessUuid(processUuid, ProcessLogsHelper.LogType.ERR, offsetStr, limitStr);
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private Response getProcessLogsLinesByProcessUuid(String processUuid, ProcessLogsHelper.LogType logType, String offsetStr, String limitStr) {
        lrProcessManager.getSynchronizingLock().lock();
        try {
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());


            LRProcess lrProcess = this.lrProcessManager.getLongRunningProcess(processUuid);
            boolean permitted = SecurityProcessUtils.permitManager(rightsResolver, user) ||
                    SecurityProcessUtils.permitReader(rightsResolver, user) ||
                    SecurityProcessUtils.permitProcessByDefinedAction(rightsResolver, user, SecurityProcessUtils.processDefinition(this.definitionManager, lrProcess.getDefinitionId()));

            if (!permitted) {
                throw new ForbiddenException("user '%s' is not allowed to manage processes (missing actions '%s', '%s')", user.getLoginname(), SecuredActions.A_PROCESS_EDIT.name(), SecuredActions.A_PROCESS_READ.name()); //403
            }

            //offset & limit
            long offset = GET_LOGS_DEFAULT_OFFSET;
            if (StringUtils.isAnyString(offsetStr)) {
                try {
                    offset = Long.valueOf(offsetStr);
                    if (offset < 0) {
                        throw new BadRequestException("offset must be zero or a positive number, '%s' is not", offsetStr);
                    }
                } catch (NumberFormatException e) {
                    throw new BadRequestException("offset must be a number, '%s' is not", offsetStr);
                }
            }
            long limit = GET_LOGS_DEFAULT_LIMIT;
            if (StringUtils.isAnyString(limitStr)) {
                try {
                    limit = Long.valueOf(limitStr);
                    if (limit < 1) {
                        throw new BadRequestException("limit must be a positive number, '%s' is not", limitStr);
                    }
                } catch (NumberFormatException e) {
                    throw new BadRequestException("limit must be a number, '%s' is not", limitStr);
                }
            }
            //access to process data
            LRProcess lrProces = lrProcessManager.getLongRunningProcess(processUuid);
            if (lrProces == null) {
                throw new BadRequestException("process with uuid " + processUuid + " not found");
            }
            ProcessLogsHelper processLogsHelper = new ProcessLogsHelper(lrProces);
            //result
            JSONObject result = new JSONObject();
            result.put("total_size", processLogsHelper.getLogsFileSize(logType));
            JSONArray linesJson = new JSONArray();
            List<String> lines = processLogsHelper.getLogsFileData(logType, offset, limit);
            for (String line : lines) {
                linesJson.put(line);
            }
            result.put("lines", linesJson);
            return Response.ok().entity(result.toString()).build();
        }finally{
            lrProcessManager.getSynchronizingLock().unlock();
        }
    }

    private JSONObject processInBatchToJson(ProcessInBatch processInBatch) {
        JSONObject json = new JSONObject();
        //batch
        JSONObject batchJson = new JSONObject();
        batchJson.put("token", processInBatch.batchToken);
        batchJson.put("id", processInBatch.batchId);
        batchJson.put("state", toBatchStateName(processInBatch.batchStateCode));
        batchJson.put("planned", Utils.toFormattedStringOrNull(processInBatch.batchPlanned));
        batchJson.put("started", Utils.toFormattedStringOrNull(processInBatch.batchStarted));
        batchJson.put("finished", Utils.toFormattedStringOrNull(processInBatch.batchFinished));
        batchJson.put("owner_id", processInBatch.batchOwnerId);
        batchJson.put("owner_name", processInBatch.batchOwnerName);
        json.put("batch", batchJson);
        //process
        JSONObject processJson = new JSONObject();
        processJson.put("id", processInBatch.processId);
        processJson.put("uuid", processInBatch.processUuid);
        processJson.put("defid", processInBatch.processDefid);
        processJson.put("name", processInBatch.processName);
        processJson.put("state", toProcessStateName(processInBatch.processStateCode));
        processJson.put("planned", Utils.toFormattedStringOrNull(processInBatch.processPlanned));
        processJson.put("started", Utils.toFormattedStringOrNull(processInBatch.processStarted));
        processJson.put("finished", Utils.toFormattedStringOrNull(processInBatch.processFinished));
        JSONObject result = new JSONObject();
        result.put("process", processJson);
        result.put("batch", batchJson);
        return result;
    }

    @DELETE
    @Path("batches/by_first_process_id/{process_id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response deleteBatch(@PathParam("process_id") String processId) {
        lrProcessManager.getSynchronizingLock().lock();
        try {
            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());

//            //authorization
//            String role = ROLE_DELETE_PROCESSES;
//            if (!roles.contains(role)) {
//                throw new ForbiddenException("user '%s' is not allowed to manage processes (missing role '%s')", user.getLoginname(), role); //403
//            }
            //id
            Integer processIdInt = null;
            if (StringUtils.isAnyString(processId)) {
                try {
                    processIdInt = Integer.valueOf(processId);
                } catch (NumberFormatException e) {
                    throw new BadRequestException("process_id must be a number, '%s' is not", processId);
                }
            }
            //get batch data from db
            Batch batch = this.processManager.getBatchByFirstProcessId(processIdInt);
            if (batch == null) {
                throw new BadRequestException("batch with first-process-id %d doesn't exist", processIdInt);
            }
            
            
            
            ProcessInBatch processInBatch = this.processManager.getProcessInBatchByProcessId(processIdInt);

            LRProcess lrProcess = this.lrProcessManager.getLongRunningProcess(processInBatch.processUuid);
            boolean permitted = SecurityProcessUtils.permitManager(rightsResolver, user) ||
                                //SecurityProcessUtils.permitReader(rightsResolver, user) ||
                                SecurityProcessUtils.permitProcessByDefinedAction(rightsResolver, user,  SecurityProcessUtils.processDefinition(this.definitionManager, lrProcess.getDefinitionId()));

            //authorization
            //String role = ROLE_DELETE_PROCESSES;
            if (!permitted) {
                throw new ForbiddenException("user '%s' is not allowed to manage processes (missing actions '%s','%s')", user.getLoginname(), SecuredActions.A_PROCESS_EDIT.name(), SecuredActions.A_PROCESS_READ.name()); //403
            }
            
            
            //check batch is deletable
            String batchState = toBatchStateName(batch.stateCode);
            if (!isDeletableState(batchState)) {
                throw new BadRequestException("batch in state %s cannot be deleted", batchState);
            }

            // delete folders
            List<LRProcess> lrs = this.lrProcessManager.getLongRunningProcessesByGroupToken(batch.token);
            

            //delete processes in batch
            int deleted = this.processManager.deleteBatchByBatchToken(batch.token);
                
            if (deleted == lrs.size()) {
                List<File> folders = lrs.stream().map(LRProcess::processWorkingDirectory).collect(Collectors.toList());
                folders.stream().forEach(f-> {
                    IOUtils.cleanDirectory(f);
                    f.delete();
                });
            } else {
                LOGGER.log(Level.INFO, "Cannot delete directory for processes "+batch.token);
            }

            JSONObject result = new JSONObject();
            result.put("batch_id", batch.firstProcessId);
            result.put("batch_token", batch.token);
            result.put("processes_deleted", deleted);

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

    @DELETE
    @Path("batches/by_first_process_id/{process_id}/execution")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response killBatch(@PathParam("process_id") String processId) {
        lrProcessManager.getSynchronizingLock().lock();
        try {
            //authentication
            //AuthenticatedUser user = getAuthenticatedUserByOauth();
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());

            Integer processIdInt = null;
            if (StringUtils.isAnyString(processId)) {
                try {
                    processIdInt = Integer.valueOf(processId);
                } catch (NumberFormatException e) {
                    throw new BadRequestException("process_id must be a positive number, '%s' is not", processId);
                }
            }
            //get batch data from db
            Batch batch = this.processManager.getBatchByFirstProcessId(processIdInt);
            if (batch == null) {
                throw new BadRequestException("batch with first-process-id %d doesn't exist", processIdInt);
            }
            
            ProcessInBatch processInBatch = this.processManager.getProcessInBatchByProcessId(processIdInt);

            LRProcess lrProcess = this.lrProcessManager.getLongRunningProcess(processInBatch.processUuid);
            boolean permitted = SecurityProcessUtils.permitManager(rightsResolver, user) ||
                                SecurityProcessUtils.permitReader(rightsResolver, user) ||
                                SecurityProcessUtils.permitProcessByDefinedAction(rightsResolver, user,  SecurityProcessUtils.processDefinition(this.definitionManager, lrProcess.getDefinitionId()));
            if (!permitted) {
                    throw new ForbiddenException("user '%s' is not allowed to manage processes (missing actions '%s','%s')", user.getLoginname(), SecuredActions.A_PROCESS_EDIT.name(), SecuredActions.A_PROCESS_READ.name()); //403
            }


            //kill all processes in batch if possible
            String batchState = toBatchStateName(batch.stateCode);
            if (isKillableState(batchState)) {
                List<ProcessInBatch> processes = processManager.getProcessesInBatchByFirstProcessId(processIdInt);
                for (ProcessInBatch process : processes) {
                    String uuid = process.processUuid;
                    //LRProcess lrProcess = lrProcessManager.getLongRunningProcess(uuid);
                    if (lrProcess != null && !States.notRunningState(lrProcess.getProcessState())) { //process in batch is running
                        try {
                            lrProcess.stopMe();
                            lrProcessManager.updateLongRunningProcessFinishedDate(lrProcess);
                        } catch (Throwable e) { //because AbstractLRProcessImpl.stopMe() throws java.lang.IllegalStateException: cannot stop this process! No PID associated
                            e.printStackTrace();
                        }
                    } else { //process in batch not running
                        //System.out.println(lrProcess.getProcessState());
                    }
                }
            }
            return Response.ok().build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        } finally {
            lrProcessManager.getSynchronizingLock().unlock();
        }
    }

// TODO pepo
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
            //access control with basic access authentication (deprecated)
            //checkAccessControlByBasicAccessAuth();

            //authentication
            User user = this.userProvider.get();
            List<String> roles = Arrays.stream(user.getGroups()).map(Role::getName).collect(Collectors.toList());

            boolean permitted = SecurityProcessUtils.permitManager(rightsResolver, user) ||
                                SecurityProcessUtils.permitReader(rightsResolver, user);
            if (!permitted) {
                throw new ForbiddenException("user '%s' is not allowed to manage processes (missing role '%s', '%s')", user.getLoginname(), SecuredActions.A_PROCESS_EDIT.name(), SecuredActions.A_PROCESS_READ.name()); //403
            }

            //offset & limit
            int offset = GET_BATCHES_DEFAULT_OFFSET;
            if (StringUtils.isAnyString(offsetStr)) {
                try {
                    offset = Integer.valueOf(offsetStr);
                    if (offset < 0) {
                        throw new BadRequestException("offset must be zero or a positive number, '%s' is not", offsetStr);
                    }
                } catch (NumberFormatException e) {
                    throw new BadRequestException("offset must be a number, '%s' is not", offsetStr);
                }
            }
            int limit = GET_BATCHES_DEFAULT_LIMIT;
            if (StringUtils.isAnyString(limitStr)) {
                try {
                    limit = Integer.valueOf(limitStr);
                    if (limit < 1) {
                        throw new BadRequestException("limit must be a positive number, '%s' is not", limitStr);
                    }
                } catch (NumberFormatException e) {
                    throw new BadRequestException("limit must be a number, '%s' is not", limitStr);
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
            //int totalSize = this.processManager.getBatchesCount(filter);
            /*
            JSONObject result = new JSONObject();
            result.put("offset", offset);
            result.put("limit", limit);
            result.put("total_size", totalSize);
            */


            //--------------------
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            JSONObject batches = processManagerClient.getBatches(null, null, null, null, null, null);

            JSONObject result = new JSONObject();
            result.put("offset", offset);
            result.put("limit", limit);
            result.put("total_size", batches.getInt("totalSize"));


            JSONArray jsonArray = batches.getJSONArray("batches");
            JSONArray batchesJson = new JSONArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObjectBatch = jsonArray.getJSONObject(i);

                JSONObject batchToJson = ProcessManagerMapper.mapBatchWithProcesses(jsonObjectBatch);
                batchesJson.put(batchToJson);

            }
            //--------------------


            //batch & process data
            /*
            List<ProcessInBatch> pibs = this.processManager.getProcessesInBatches(filter, offset, limit);
            List<BatchWithProcesses> batchWithProcesses = extractBatchesWithProcesses(pibs);
            JSONArray batchesJson = new JSONArray();
            for (BatchWithProcesses batch : batchWithProcesses) {
                JSONObject batchJson = batchToJson(batch);
                batchesJson.put(batchJson);
            }

             */
            result.put("batches", batchesJson);
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

//    private void checkAccessControlByBasicAccessAuth() {
//        boolean disabled = true;
//        if (!disabled) {
//            User user = this.userProvider.get();
//            if (user == null) {
//                throw new UnauthorizedException("user==null"); //401
//            }
//            boolean allowed = rightsResolver.isActionAllowed(user, SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH).flag();
//            if (!allowed) {
//                throw new ForbiddenException("user '%s' is not allowed to manage processes", user.getLoginname()); //403
//            }
//        }
//    }

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
                paramsList.addAll(paramsToList(defid, params, flag-> {
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

                String title =  shortenIfTooLong(buildInitialProcessName(defid, paramsList), MAX_TITLE_LENGTH);
                
                return scheduleProcess(defid, paramsList, user.getLoginname(), user.getLoginname(), batchToken, title);
            }
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

    private Response scheduleProcess(String defid, List<String> params, String ownerId, String ownerName, String batchToken, String processName) {
        LRProcess newProcess = processSchedulingHelper.scheduleProcess(defid, params, ownerId, ownerName, batchToken, processName);
        URI uri = UriBuilder.fromResource(LRResource.class).path("{uuid}").build(newProcess.getUUID());
        return Response.created(uri).entity(lrPRocessToJSONObject(newProcess).toString()).build();
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
            jsonObject.put("planned", Utils.toFormattedStringOrNull(lrProcess.getPlannedTime()));
        }
        jsonObject.put("userid", lrProcess.getLoginname()); //empty
        jsonObject.put("userFirstname", lrProcess.getFirstname()); //empty
        jsonObject.put("userSurname", lrProcess.getSurname()); //empty
        return jsonObject;
    }

    //TODO: I18N
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
                            : String.format("Odebrání příznaku viditelnosti %s (%s)", pid,  scope);
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
                    if(params.get(0).equals("true")) {
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

    
    private static String shortenIfTooLong(String string, int maxLength) {
        if (string == null || string.isEmpty() || string.length() <= maxLength) {
            return string;
        } else {
            String suffix = "...";
            return string.substring(0, maxLength - suffix.length()) + suffix;
        }
    }

    
    private List<String> paramsToList(String id, JSONObject params, Consumer<Boolean> consumer) {
        switch (id) {
            case "new_process_api_test": {
                //duration (of every process in the batch) in seconds
                Integer duration = extractOptionalParamPositiveInteger(params, "duration", 1);
                //number of processes in the batch
                Integer processesInBatch = extractOptionalParamPositiveInteger(params, "processesInBatch", 1);
                //final state of every process in the batch (including random)
                String finalState = extractOptionalParamWithValueFromEnum(params, "finalState", ProcessApiTestProcess.FinalState.class, ProcessApiTestProcess.FinalState.FINISHED.name());

                List<String> result = new ArrayList<>();
                result.add(duration.toString());
                result.add(processesInBatch.toString());
                result.add(finalState);
                consumer.accept(false);
                return result;
            }
            case "new_indexer_index_object": {
                String type = extractMandatoryParamWithValueFromEnum(params, "type", IndexationType.class);
                String pid = extractOptionalParamString(params, "pid", null);
                List<String> pidlist = extractOptionalParamStringList(params, "pidlist", null);

                
                String checkPidlistFile = extractOptionalParamString(params, "pidlist_file", null);
                File pidlistFile = null;
                if (checkPidlistFile != null && (new File(checkPidlistFile)).exists()) {
                    pidlistFile = new File(checkPidlistFile);
                } else {
                    pidlistFile = extractOptionalParamFileContainedInADir(params, "pidlist_file", new File(KConfiguration.getInstance().getProperty("convert.directory"))); //TODO: specialni adresar pro pidlisty, ne convert.directory
                }

                
                Boolean ignoreInconsistentObjects = extractOptionalParamBoolean(params, "ignoreInconsistentObjects", false);
                String title = extractOptionalParamString(params, "title", null);

                List<String> result = new ArrayList<>();
                result.add(type);//indexation type
                
                String target;
                if (pid != null) {
                    target = pid;
                } else if (pidlist != null) {
                    target = pidlist.stream().collect(Collectors.joining(";"));
                } else if (pidlistFile != null) {
                    target = "pidlist_file:" + pidlistFile.getAbsolutePath();

                } else {
                    throw new BadRequestException("target not specified, use one of following parameters: pid, pidlist");
                }
                result.add(target);
                
 
                result.add(ignoreInconsistentObjects.toString());
                result.add(title);//indexation's root title
                consumer.accept(false);
                return result;
            }
            case "new_indexer_index_model": {
                String type = extractMandatoryParamWithValueFromEnum(params, "type", IndexationType.class);
                String pid = extractMandatoryParamWithValuePrefixed(params, "pid", "model:");
                Boolean ignoreInconsistentObjects = extractOptionalParamBoolean(params, "ignoreInconsistentObjects", false);
                Boolean indexNotIndexed = extractOptionalParamBoolean(params, "indexNotIndexed", true);
                Boolean indexRunningOrError = extractOptionalParamBoolean(params, "indexRunningOrError", false);
                Boolean indexIndexedOutdated = extractOptionalParamBoolean(params, "indexIndexedOutdated", false);
                Boolean indexIndexed = extractOptionalParamBoolean(params, "indexIndexed", false);

                List<String> result = new ArrayList<>();
                result.add(type); //indexation type
                result.add(pid); //indexation's root pid
                result.add(ignoreInconsistentObjects.toString());
                result.add(indexNotIndexed.toString()); //if not-indexed objects should be indexed
                result.add(indexRunningOrError.toString());//if running-or-error objects should be indexed
                result.add(indexIndexedOutdated.toString());//if indexed-outdated objects should be indexed
                result.add(indexIndexed.toString());//if indexed objects should be indexed
                consumer.accept(false);
                return result;
            }
            case "set_policy": {
                String scope = extractMandatoryParamWithValueFromEnum(params, "scope", SetPolicyProcess.Scope.class);
                String policy = extractMandatoryParamWithValueFromEnum(params, "policy", SetPolicyProcess.Policy.class);
                String pid = extractMandatoryParamWithValuePrefixed(params, "pid", "uuid:");
                String title = extractOptionalParamString(params, "title", null);
                
                try {
                    ObjectPidsPath[] pidPaths = this.solrAccess.getPidPaths(pid);
                    User user = this.userProvider.get();
                    LRProcessDefinition definition = this.definitionManager.getLongRunningProcessDefinition("set_policy");
                    boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, pid, pidPaths);
                    consumer.accept(permit);
                } catch (IOException e) {
                    consumer.accept(false);
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }
                
                List<String> result = new ArrayList<>();
                result.add(scope);
                result.add(policy);
                result.add(pid);
                result.add(title);
                return result;
            }
            case "remove_policy": {
                String scope = extractMandatoryParamWithValueFromEnum(params, "scope", SetPolicyProcess.Scope.class);
                //String policy = extractMandatoryParamWithValueFromEnum(params, "policy", SetPolicyProcess.Policy.class);
                //String pid = extractMandatoryParamWithValuePrefixed(params, "pid", "uuid:");
                String pid = extractOptionalParamString(params, "pid", null);
                String title = extractOptionalParamString(params, "title", null);

                List<String> pidlist = extractOptionalParamStringList(params, "pidlist", null);

                String checkPidlistFile = extractOptionalParamString(params, "pidlist_file", null);
                File pidlistFile = null;
                if (checkPidlistFile != null && (new File(checkPidlistFile)).exists()) {
                    pidlistFile = new File(checkPidlistFile);
                } else {
                    pidlistFile = extractOptionalParamFileContainedInADir(params, "pidlist_file", new File(KConfiguration.getInstance().getProperty("convert.directory"))); //TODO: specialni adresar pro pidlisty, ne convert.directory
                }

                String target;
                if (pid != null) {
                    target = "pid:" + pid;
                } else if (pidlist != null) {
                    target = "pidlist:" + pidlist.stream().collect(Collectors.joining(";"));
                } else if (pidlistFile != null) {
                    target = "pidlist_file:" + pidlistFile.getAbsolutePath();

                } else {
                    throw new BadRequestException("target not specified, use one of following parameters: pid, pidlist");
                }

                
                if (pid != null) {
                    try {
                        ObjectPidsPath[] pidPaths = this.solrAccess.getPidPaths(pid);
                        User user = this.userProvider.get();
                        LRProcessDefinition definition = this.definitionManager.getLongRunningProcessDefinition("set_policy");
                        boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, pid, pidPaths);
                        consumer.accept(permit);
                    } catch (IOException e) {
                        consumer.accept(false);
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                } else if (pidlist != null) {
                    pidlist.stream().forEach(p-> {
                        try {
                            ObjectPidsPath[] pidPaths = this.solrAccess.getPidPaths(p);
                            User user = this.userProvider.get();
                            LRProcessDefinition definition = this.definitionManager.getLongRunningProcessDefinition("set_policy");
                            boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, p, pidPaths);
                            consumer.accept(permit);
                        } catch (IOException e) {
                            consumer.accept(false);
                            LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        }
                    });
                } else {
                    // musi mit prava pro cely repozitar
                    ObjectPidsPath[] pidPaths = new ObjectPidsPath[] {
                            ObjectPidsPath.REPOSITORY_PATH
                    };
                    User user = this.userProvider.get();
                    LRProcessDefinition definition = this.definitionManager.getLongRunningProcessDefinition("add_license");
                    boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, SpecialObjects.REPOSITORY.getPid(), pidPaths);
                    consumer.accept(permit);
                }
                
                
                List<String> result = new ArrayList<>();

                result.add(scope);
                
                if (pid != null) {
                    result.add(pid);
                } else if (pidlist != null) {
                    result.add(pidlist.stream().collect(Collectors.joining(";")));
                } else {
                    result.add(target);
                }

                result.add(title);
                return result;
            }
            case "processing_rebuild": {
                consumer.accept(false);
                return Collections.emptyList();
            }
            case "processing_rebuild_for_object": {
                String pid = extractOptionalParamString(params, "pid", null);
                List<String> pidlist = extractOptionalParamStringList(params, "pidlist", null);
                String checkPidlistFile = extractOptionalParamString(params, "pidlist_file", null);
                File pidlistFile = null;
                if (checkPidlistFile != null && (new File(checkPidlistFile)).exists()) {
                    pidlistFile = new File(checkPidlistFile);
                } else {
                    pidlistFile = extractOptionalParamFileContainedInADir(params, "pidlist_file", new File(KConfiguration.getInstance().getProperty("convert.directory"))); //TODO: specialni adresar pro pidlisty, ne convert.directory
                }

                String target;
                if (pid != null) {
                    target = "pid:" + pid;
                } else if (pidlist != null) {
                    target = "pidlist:" + pidlist.stream().collect(Collectors.joining(";"));
                } else if (pidlistFile != null) {
                    target = "pidlist_file:" + pidlistFile.getAbsolutePath();

                } else {
                    throw new BadRequestException("target not specified, use one of following parameters: pid, pidlist");
                }

                // musi mit prava pro cely repozitar
                ObjectPidsPath[] pidPaths = new ObjectPidsPath[] {
                        ObjectPidsPath.REPOSITORY_PATH
                };
                User user = this.userProvider.get();
                boolean permitProcessingIndex = user!= null? (rightsResolver.isActionAllowed(user,SecuredActions.A_REBUILD_PROCESSING_INDEX.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null , ObjectPidsPath.REPOSITORY_PATH)).flag() : false;
                consumer.accept(permitProcessingIndex);
                if (permitProcessingIndex) {
                    List<String> result = new ArrayList<>();
                    if (pid != null) {
                        result.add(pid);
                    } else if (pidlist != null) {
                        result.add(pidlist.stream().collect(Collectors.joining(";")));
                    } else {
                        result.add(target);
                    }
                    return result;
                } else {
                    return new ArrayList<>();
                }
            }
            case "import": {
                // import directory

                File inputDataDir = null;
                String pathType = extractOptionalParamString(params, "pathtype", "relative");
                if (pathType.equals("relative")) {
                    inputDataDir = extractMandatoryParamFileContainedInADir(params, "inputDataDir", new File(KConfiguration.getInstance().getProperty("import.directory")));
                } else { // absolute
                    inputDataDir = extractMandatoryParamFileContainedInADir(params, "inputDataDir",  null);
                }

                Boolean startIndexer = extractMandatoryParamBoolean(params, "startIndexer");

                String license = extractOptionalParamString(params, "license", null);
                String collections = extractOptionalParamString(params, "collections", null);

                String indexationType = extractOptionalParamString(params, "indexationType", null);

                List<String> result = new ArrayList<>();
                result.add(inputDataDir.getPath());
                result.add(startIndexer.toString());
                if (license != null) {
                    result.add(license.toString());
                } else {
                    result.add(NONE_KEYWORD);
                }
                if (collections != null) {
                    result.add(collections.toString());
                } else {
                    result.add(NONE_KEYWORD);
                }
                result.add(indexationType);

                consumer.accept(false);
                return result;
            }
            case "convert_and_import": {
                String policy = extractMandatoryParamWithValueFromEnum(params, "policy", Policy.class);
                //File inputDataDir = extractMandatoryParamFileContainedInADir(params, "inputDataDir", new File(KConfiguration.getInstance().getProperty("convert.directory")));

                File inputDataDir = null;
                String pathType = extractOptionalParamString(params, "pathtype", "relative");
                if (pathType.equals("relative")) {
                    inputDataDir = extractMandatoryParamFileContainedInADir(params, "inputDataDir", new File(KConfiguration.getInstance().getProperty("convert.directory")));
                } else { // absolute
                    inputDataDir = extractMandatoryParamFileContainedInADir(params, "inputDataDir",  null);
                }

                
                String convertedDataDirSuffix = new SimpleDateFormat("yyMMdd_HHmmss_SSS").format(System.currentTimeMillis());
                File convertedDataDir = new File(new File(KConfiguration.getInstance().getProperty("convert.target.directory")), inputDataDir.getName() + "_" + convertedDataDirSuffix);
                Boolean startIndexer = extractMandatoryParamBoolean(params, "startIndexer");
                Boolean useIIPServer = extractMandatoryParamBoolean(params, "useIIPServer");

                String license = extractOptionalParamString(params, "license", null);
                String collections = extractOptionalParamString(params, "collections", null);
                String indexationType = extractOptionalParamString(params, "indexationType", null);

                
                List<String> result = new ArrayList<>();
                result.add(policy);
                result.add(inputDataDir.getPath());
                result.add(convertedDataDir.getPath());
                result.add(startIndexer.toString());
                result.add(useIIPServer.toString());

                if (license != null) {
                    result.add(license.toString());
                } else {
                    result.add(NONE_KEYWORD);
                }                
                if (collections != null) {
                    result.add(collections.toString());
                } else {
                    result.add(NONE_KEYWORD);
                }
                result.add(indexationType);
                consumer.accept(false);
                return result;
            }
            case "add_license":
            case "remove_license": {
                String license = extractMandatoryParamString(params, "license");
                String pid = extractOptionalParamString(params, "pid", null);
                List<String> pidlist = extractOptionalParamStringList(params, "pidlist", null);

                // TODO: Change it; not only files came from convert.directory
                String checkPidlistFile = extractOptionalParamString(params, "pidlist_file", null);
                File pidlistFile = null;
                
                if (checkPidlistFile != null && (new File(checkPidlistFile)).exists()) {
                    pidlistFile = new File(checkPidlistFile);
                } else {
                    pidlistFile = extractOptionalParamFileContainedInADir(params, "pidlist_file", new File(KConfiguration.getInstance().getProperty("convert.directory"))); //TODO: specialni adresar pro pidlisty, ne convert.directory
                }
                
                String target;
                if (pid != null) {
                    target = "pid:" + pid;
                } else if (pidlist != null) {
                    target = "pidlist:" + pidlist.stream().collect(Collectors.joining(";"));
                } else if (pidlistFile != null) {
                    target = "pidlist_file:" + pidlistFile.getAbsolutePath();
                } else {
                    throw new BadRequestException("target not specified, use one of following parameters: pid, pidlist, pidlist_file");
                }
                
                if (pid != null) {
                    try {
                        ObjectPidsPath[] pidPaths = this.solrAccess.getPidPaths(pid);
                        User user = this.userProvider.get();
                        LRProcessDefinition definition = this.definitionManager.getLongRunningProcessDefinition("add_license");
                        boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, pid, pidPaths);
                        consumer.accept(permit);
                    } catch (IOException e) {
                        consumer.accept(false);
                        LOGGER.log(Level.SEVERE,e.getMessage(),e);
                    }
                } else if (pidlist != null) {
                    pidlist.forEach(p-> {
                        try {
                            ObjectPidsPath[] pidPaths = this.solrAccess.getPidPaths(p);
                            User user = this.userProvider.get();
                            LRProcessDefinition definition = this.definitionManager.getLongRunningProcessDefinition("add_license");
                            boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, p, pidPaths);
                            consumer.accept(permit);
                        } catch (IOException e) {
                            consumer.accept(false);
                            LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        }
                    });
                    
                } else {
                    // musi mit prava pro cely repozitar
                    ObjectPidsPath[] pidPaths = new ObjectPidsPath[] {
                            ObjectPidsPath.REPOSITORY_PATH
                    };
                    User user = this.userProvider.get();
                    LRProcessDefinition definition = this.definitionManager.getLongRunningProcessDefinition("add_license");
                    boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, SpecialObjects.REPOSITORY.getPid(), pidPaths);
                    consumer.accept(permit);
                }

                List<String> result = new ArrayList<>();
                result.add(license);
                result.add(target);
                return result;
            }

            case "flag_to_license": {
                    
                //String modelList = extractOptionalParamString(params, "modellist","monographunit;periodicalvolume");
                boolean removePolicy = extractOptionalParamBoolean(params, "remove_policy",false);
                
                ObjectPidsPath[] pidPaths = new ObjectPidsPath[] {
                    ObjectPidsPath.REPOSITORY_PATH
                };
                User user = this.userProvider.get();
                LRProcessDefinition definition = this.definitionManager.getLongRunningProcessDefinition("add_license");
                boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, SpecialObjects.REPOSITORY.getPid(), pidPaths);
                consumer.accept(permit);

                
                List<String> result = new ArrayList<>();
                //result.add(modelList);
                result.add(""+removePolicy);
                
                return result;

            }
                
            case "nkplogs": { //TODO: rename to verb like "generate_nkp_logs"

                String dateFrom = extractMandatoryParamString(params, "dateFrom");
                String dateTo = extractMandatoryParamString(params, "dateTo");
                Boolean emailNotification = extractMandatoryParamBoolean(params, "emailNotification");

                try {
                    StatisticReport.DATE_FORMAT.parse(dateFrom);
                } catch (ParseException e) {
                    throw new BadRequestException("cannot parse dateFrom, following pattern is expected: 'yyyy.MM.dd'");
                }

                try {
                    StatisticReport.DATE_FORMAT.parse(dateTo);
                } catch (ParseException e) {
                    throw new BadRequestException("cannot parse dateTo, following pattern is expected: 'yyyy.MM.dd'");
                }

                List<String> result = new ArrayList<>();
                result.add(dateFrom);
                result.add(dateTo);
                    
                if (emailNotification != null) {
                    result.add(emailNotification+"");
                } else {
                    result.add(Boolean.FALSE.toString());
                }
                

                consumer.accept(false);
                return result;
            }
            case "sdnnt-sync": { //TODO: rename to verb like "generate_nkp_logs"
                List<String> result = new ArrayList<>();
                return result;
            }

            case "backup-collections": { 
                String backupname = extractMandatoryParamString(params, "backupname");
                List<String> pidlist = extractOptionalParamStringList(params, "pidlist", null);
                
                String target  = "pidlist:" + pidlist.stream().collect(Collectors.joining(";"));
                
                if (pidlist != null) {
                    pidlist.forEach(p-> {
                        try {
                            ObjectPidsPath[] pidPaths = this.solrAccess.getPidPaths(p);
                            User user = this.userProvider.get();
                            LRProcessDefinition definition = this.definitionManager.getLongRunningProcessDefinition("add_license");
                            boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, p, pidPaths);
                            consumer.accept(permit);
                        } catch (IOException e) {
                            consumer.accept(false);
                            LOGGER.log(Level.SEVERE,e.getMessage(),e);
                        }
                    });
                }

                List<String> result = new ArrayList<>();
                result.add(target);
                result.add(backupname);
                return result;
            }

            case "restore-collections": { 
                String backupname = extractMandatoryParamString(params, "backupname");
                
                ObjectPidsPath[] pidPaths = new ObjectPidsPath[] {
                        ObjectPidsPath.REPOSITORY_PATH
                };
                User user = this.userProvider.get();
                LRProcessDefinition definition = this.definitionManager.getLongRunningProcessDefinition("add_license");
                boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, SpecialObjects.REPOSITORY.getPid(), pidPaths);
                consumer.accept(permit);

                List<String> result = new ArrayList<>();
                result.add(backupname);
                return result;
            }

            case "migrate-collections-from-k5": { 
                String k5 = extractMandatoryParamString(params, "k5");
                
                ObjectPidsPath[] pidPaths = new ObjectPidsPath[] {
                        ObjectPidsPath.REPOSITORY_PATH
                };
                User user = this.userProvider.get();
                LRProcessDefinition definition = this.definitionManager.getLongRunningProcessDefinition("add_license");
                boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, SpecialObjects.REPOSITORY.getPid(), pidPaths);
                consumer.accept(permit);

                List<String> result = new ArrayList<>();
                result.add(k5);
                return result;
            }

            case "delete_tree": {
                String pid = extractMandatoryParamWithValuePrefixed(params, "pid", "uuid:");
                Boolean ignoreIncostencies = extractOptionalParamBoolean(params, "ignoreIncosistencies", false);

                String title = extractOptionalParamString(params, "title", null);

                List<String> result = new ArrayList<>();
                result.add(pid);
                result.add(title);
                if (ignoreIncostencies) {
                    result.add(ignoreIncostencies.toString());
                }
                

                try {
                    ObjectPidsPath[] pidPaths = this.solrAccess.getPidPaths(pid);
                    User user = this.userProvider.get();
                    LRProcessDefinition definition = this.definitionManager.getLongRunningProcessDefinition("delete_tree");
                    boolean permit = SecurityProcessUtils.permitProcessByDefinedActionWithPid(rightsResolver, user, definition, pid, pidPaths);
                    consumer.accept(permit);
                } catch (IOException e) {
                    consumer.accept(false);
                    LOGGER.log(Level.SEVERE,e.getMessage(),e);
                }

                return result;
            }
            // TODO: Support annotation @Process and @ProcessParam - mapping in old API
            default: {
                LOGGER.log(Level.SEVERE, String.format("unsupported process id '%s'", id));
                throw new BadRequestException("unsupported process id '%s'", id);
            }
        }
    }

    private List<String> extractOptionalParamStringList(JSONObject params, String paramName, List<String> defaultValue) {
        if (params.has(paramName)) {
            if (! (params.get(paramName) instanceof JSONArray)) {
                throw new BadRequestException("mandatory parameter %s is not array", paramName);
            }
            JSONArray jsonArray = params.getJSONArray(paramName);
            List<String> result = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                result.add(jsonArray.getString(i));
            }
            return result;
        } else {
            return defaultValue;
        }
    }

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
    }

    private String extractOptionalParamString(JSONObject params, String paramName, String defaultValue) {
        return params.has(paramName) ? params.getString(paramName) : defaultValue;
    }

    private Boolean extractOptionalParamBoolean(JSONObject params, String paramName, boolean defaultValue) {
        if (params.has(paramName)) {
            return params.getBoolean(paramName);
        } else {
            return defaultValue;
        }
    }

    private Boolean extractMandatoryParamBoolean(JSONObject params, String paramName) {
        if (params.has(paramName)) {
            return params.getBoolean(paramName);
        } else {
            throw new BadRequestException("missing mandatory parameter %s: ", paramName);
        }
    }

    private String extractMandatoryParamString(JSONObject params, String paramName) {
        String value = extractOptionalParamString(params, paramName, null);
        if (value == null) {
            throw new BadRequestException("missing mandatory parameter %s: ", paramName);
        } else {
            return value;
        }
    }

    private File extractMandatoryParamFileContainedInADir(JSONObject params, String paramName, File rootDir) {
        String value = extractOptionalParamString(params, paramName, null);
        if (value == null) {
            throw new BadRequestException("missing mandatory parameter %s: ", paramName);
        } else {
            return extractFileContainedInADirFromParamValue(value, paramName, rootDir);
        }
    }

    private File extractOptionalParamFileContainedInADir(JSONObject params, String paramName, File rootDir) {
        String value = extractOptionalParamString(params, paramName, null);
        return value == null ? null : extractFileContainedInADirFromParamValue(value, paramName, rootDir);
    }

    private File extractFileContainedInADirFromParamValue(String paramValue, String paramName, File rootDir) {
        //sanitize against problematic characters
        char[] forbiddenChars = new char[]{'~', '#', '%', '&', '{', '}', '<', '>', '*', '?', '$', '!',  '@', '+', '`', '|', '=', ';', ' ', '\t'};
        for (char forbiddenChar : forbiddenChars) {
            if (paramValue.indexOf(forbiddenChar) != -1) {
                throw new BadRequestException("invalid value of %s (contains forbidden character '%s'): '%s'", paramName, forbiddenChar, paramValue);
            }
        }
        try {

            boolean canonical = KConfiguration.getInstance().getConfiguration().getBoolean("io.canonical.file",true);
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


    private String extractMandatoryParamWithValuePrefixed(JSONObject params, String paramName, String prefix) {
        String value = extractOptionalParamString(params, paramName, null);
        if (value == null) {
            throw new BadRequestException("missing mandatory parameter %s: ", paramName);
        } else {
            if (!value.toLowerCase().startsWith(prefix)) {
                throw new BadRequestException("invalid value of %s (doesn't start with '%s'): '%s'", paramName, prefix, value);
            }
            return value;
        }
    }

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
        
        batchJson.put("planned", Utils.toFormattedStringOrNull(batchWithProcesses.planned));
        batchJson.put("started", Utils.toFormattedStringOrNull(batchWithProcesses.started));
        batchJson.put("finished", Utils.toFormattedStringOrNull(batchWithProcesses.finished));
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
            processJson.put("planned", Utils.toFormattedStringOrNull(process.planned));
            processJson.put("started", Utils.toFormattedStringOrNull(process.started));
            processJson.put("finished", Utils.toFormattedStringOrNull(process.finished));
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


    private String toBatchStateName(Integer batchStateCode) {
        if(batchStateCode == null){
            return "UNKNOWN";
        }

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
            case 5:
                return "WARNING";
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
            case "WARNING":
                return 5;
            default:
                throw new BadRequestException("unknown state '%s'", batchStateName);
        }
    }

    private boolean isDeletableState(String batchState) {
        String[] deletableStates = new String[]{"FINISHED", "FAILED", "KILLED"};
        return batchState != null && Arrays.asList(deletableStates).contains(batchState);
    }

    private boolean isKillableState(String batchState) {
        String[] deletableStates = new String[]{"PLANNED", "RUNNING"};
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

    public enum Policy {
        PRIVATE, PUBLIC
    }
}
