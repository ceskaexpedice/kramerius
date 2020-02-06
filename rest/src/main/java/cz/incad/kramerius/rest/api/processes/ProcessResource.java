package cz.incad.kramerius.rest.api.processes;

import cz.incad.kramerius.ObjectPidsPath;
import cz.incad.kramerius.processes.new_api.Filter;
import cz.incad.kramerius.processes.new_api.ProcessInBatch;
import cz.incad.kramerius.processes.new_api.ProcessManager;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.rest.api.exceptions.UnauthorizedException;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredActions;
import cz.incad.kramerius.security.SpecialObjects;
import cz.incad.kramerius.security.User;
import cz.incad.kramerius.security.utils.UserUtils;
import cz.incad.kramerius.users.LoggedUsersSingleton;
import cz.incad.kramerius.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/v6.0/processes")
public class ProcessResource {

    public static Logger LOGGER = Logger.getLogger(ProcessResource.class.getName());

    private static final Integer DEFAULT_OFFSET = 0;
    private static final Integer DEFAULT_LIMIT = 10;
    private static final boolean DEV_DISABLE_ACCESS_CONTROL = true;

    //@Inject
    //LRProcessManager lrProcessManager;

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
     * @param offsetStr
     * @param limitStr
     * @param filterOwner owner id (login)
     * @param filterFrom
     * @param filterUntil
     * @param filterState state name (nebo code?)
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
            //access control
            if (!DEV_DISABLE_ACCESS_CONTROL) {
                String loggedUserKey = findLoggedUserKey();
                User user = this.loggedUsersSingleton.getUser(loggedUserKey);
                if (user == null) {
                    throw new UnauthorizedException(); //401
                }
                boolean allowed = rightsResolver.isActionAllowed(user, SecuredActions.MANAGE_LR_PROCESS.getFormalName(), SpecialObjects.REPOSITORY.getPid(), null, ObjectPidsPath.REPOSITORY_PATH);
                if (!allowed) {
                    throw new ActionNotAllowed("user '%s' is not allowed to manage processes", user.getLoginname()); //403
                }
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
            throw new GenericApplicationException(e.getMessage());
        }
    }

    public String findLoggedUserKey() {
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
