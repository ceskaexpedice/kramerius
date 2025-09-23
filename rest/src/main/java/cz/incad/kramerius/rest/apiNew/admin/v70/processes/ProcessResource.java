package cz.incad.kramerius.rest.apiNew.admin.v70.processes;

import com.google.inject.name.Named;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.rest.apiNew.admin.v70.AdminApiResource;
import cz.incad.kramerius.rest.apiNew.exceptions.BadRequestException;
import cz.incad.kramerius.rest.apiNew.exceptions.InternalErrorException;
import cz.incad.kramerius.rest.apiNew.exceptions.NotFoundException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.User;
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
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/admin/v7.0/processes")
public class ProcessResource extends AdminApiResource {

    private static Logger LOGGER = Logger.getLogger(ProcessResource.class.getName());

    @Inject
    LRProcessManager lrProcessManager;

    @Inject
    DefinitionManager definitionManager;

    @javax.inject.Inject
    Provider<User> userProvider;

    @Inject
    @Named("new-index")
    SolrAccess solrAccess;

    @Inject
    RightsResolver rightsResolver;

    // TODO pepo
    @Inject
    @javax.inject.Named("forward-client")
    private CloseableHttpClient apacheClient;

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
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            JSONObject pcpProcess = processManagerClient.getProcess(processId);
            if (pcpProcess == null) {
                throw new NotFoundException("there's no process with process_id=" + processId);
            }
            ForbiddenCheck.checkByProfile(userProvider.get(), rightsResolver, definitionManager,
                    pcpProcess.getString(ProcessManagerMapper.PCP_PROFILE_ID), true);
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
            result.put(ProcessManagerMapper.OFFSET, offsetStr);
            result.put(ProcessManagerMapper.LIMIT, limitStr);
            result.put(ProcessManagerMapper.KR_TOTAL_SIZE, pcpBatches.getInt(ProcessManagerMapper.PCP_TOTAL_SIZE));
            JSONArray pcpBatchesArray = pcpBatches.getJSONArray(ProcessManagerMapper.BATCHES);
            JSONArray resultBatchesArray = new JSONArray();
            for (int i = 0; i < pcpBatchesArray.length(); i++) {
                JSONObject pcpBatchWithProcesses = pcpBatchesArray.getJSONObject(i);
                JSONObject resultBatchWithProcesses = ProcessManagerMapper.mapBatchWithProcesses(pcpBatchWithProcesses);
                resultBatchesArray.put(resultBatchWithProcesses);

            }
            result.put(ProcessManagerMapper.BATCHES, resultBatchesArray);
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
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            JSONObject pcpProcess = processManagerClient.getProcess(processId);
            if (pcpProcess == null) {
                throw new NotFoundException("there's no process with process_id=" + processId);
            }
            String profileId = pcpProcess.getString(ProcessManagerMapper.PCP_PROFILE_ID);
            ForbiddenCheck.checkByProfile(userProvider.get(), rightsResolver, definitionManager, profileId, false);

            int deleted = processManagerClient.deleteBatch(processId);
            JSONObject result = new JSONObject();
            result.put(ProcessManagerMapper.KR_BATCH_ID, processId);
            result.put(ProcessManagerMapper.KR_BATCH_TOKEN_1, "-"); // TODO pepo
            result.put(ProcessManagerMapper.KR_PROCESSES_DELETED, deleted);

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
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            JSONObject pcpProcess = processManagerClient.getProcess(processId);
            if (pcpProcess == null) {
                throw new NotFoundException("there's no process with process_id=" + processId);
            }
            String profileId = pcpProcess.getString(ProcessManagerMapper.PCP_PROFILE_ID);
            ForbiddenCheck.checkByProfile(userProvider.get(), rightsResolver, definitionManager, profileId, false);

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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response scheduleProcess(JSONObject processDefinition) {
        try {
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            JSONObject pcpSchedule = ProcessManagerMapper.mapScheduleMainProcess(processDefinition, userProvider.get().getLoginname());
            String profileId = pcpSchedule.getString(ProcessManagerMapper.PCP_PROFILE_ID);
            JSONObject profile = processManagerClient.getProfile(profileId);
            JSONObject plugin = processManagerClient.getPlugin(profile.getString(ProcessManagerMapper.PCP_PLUGIN_ID));
            JSONArray scheduledProfiles = null;
            if(!plugin.isNull(ProcessManagerMapper.PCP_SCHEDULED_PROFILES)){
                scheduledProfiles = plugin.getJSONArray(ProcessManagerMapper.PCP_SCHEDULED_PROFILES);
            }
            ForbiddenCheck.checkByProfileAndParamsPids(userProvider.get(), rightsResolver, definitionManager,
                    profileId, processDefinition.getJSONObject(ProcessManagerMapper.PCP_PAYLOAD), scheduledProfiles, solrAccess);
            String processId = processManagerClient.scheduleProcess(pcpSchedule);
            JSONObject result = new JSONObject();
            result.put(ProcessManagerMapper.PCP_PROCESS_ID, processId);
            return Response.ok().entity(result.toString()).build();
        } catch (WebApplicationException e) {
            throw e;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new InternalErrorException(e.getMessage());
        }
    }

}
