package cz.incad.kramerius.rest.api.processes;

import cz.incad.kramerius.processes.new_api.Batch;
import cz.incad.kramerius.processes.new_api.Filter;
import cz.incad.kramerius.processes.new_api.ProcessManager;
import cz.incad.kramerius.rest.api.exceptions.ActionNotAllowed;
import cz.incad.kramerius.rest.api.exceptions.BadRequestException;
import cz.incad.kramerius.rest.api.exceptions.GenericApplicationException;
import cz.incad.kramerius.utils.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;

@Path("/v6.0/processes")
public class ProcessResource {

    public static Logger LOGGER = Logger.getLogger(ProcessResource.class.getName());

    private static final Integer DEFAULT_OFFSET = 0;
    private static final Integer DEFAULT_LIMIT = 10;

    //@Inject
    //LRProcessManager lrProcessManager;

    @Inject
    ProcessManager processManager;

    /**
     * Returns filtered batches
     *
     * @param offset
     * @param limit
     * @param filterOwner owner id (login)
     * @param filterFrom
     * @param filterUntil
     * @param filterState state name (nebo code?)
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public Response getBatches(
            @QueryParam("offset") String offset,
            @QueryParam("limit") String limit,
            @QueryParam("owner") String filterOwner,
            @QueryParam("from") String filterFrom,
            @QueryParam("until") String filterUntil,
            @QueryParam("state") String filterState
            //TODO: sort by selected columns, ASC, DESC
            //TODO: ASC, DESC pro default order by process_id, coz je priblizne podle naplanovani
    ) {
       /* String loggedUserKey = findLoggedUserKey();
        User user = this.loggedUsersSingleton.getUser(loggedUserKey);
        if (user == null) {
            // no user
            //throw new SecurityException("access denided");
            throw new ActionNotAllowed("action is not allowed");
        }

        boolean permitted = permit(actionAllowed, user);
        */
        //TODO: access control
        boolean permitted = true;

        if (permitted) {
            try {
                //OFFSET & LIMIT
                int offsetInt = DEFAULT_OFFSET;
                if (StringUtils.isAnyString(offset)) {
                    try {
                        offsetInt = Integer.valueOf(offset);
                        if (offsetInt < 0) {
                            throw new BadRequestException("offset must be zero or positive, '%s' is not", offset);
                        }
                    } catch (NumberFormatException e) {
                        throw new BadRequestException("offset must be integer, '%s' is not", offset);
                    }
                }
                int limitInt = DEFAULT_LIMIT;
                if (StringUtils.isAnyString(limit)) {
                    try {
                        limitInt = Integer.valueOf(limit);
                        if (limitInt < 1) {
                            throw new BadRequestException("limit must be positive, '%s' is not", limit);
                        }
                    } catch (NumberFormatException e) {
                        throw new BadRequestException("limit must be integer, '%s' is not", limit);
                    }
                }

                //FILTER
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

                return getBatches(filter, offsetInt, limitInt);
            } catch (BadRequestException e) {
                throw e;
            } catch (Throwable e) {
                throw new GenericApplicationException(e.getMessage());
            }
        } else {
            throw new ActionNotAllowed("action is not allowed");
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

    private Response getBatches(Filter filter, int offset, int limit) {
        try {
            int size = this.processManager.getBatchesCount(filter);
            JSONObject result = new JSONObject();
            result.put("offset", offset);
            result.put("limit", limit);
            //batches
            List<Batch> batches = this.processManager.getBatches(filter, offset, limit);
            JSONArray batchesJson = new JSONArray();
            for (Batch batch : batches) {
                JSONObject batchJson = new JSONObject();
                batchJson.put("batch_token", batch.token);
                batchJson.put("batch_id", batch.id);
                batchJson.put("batch_state", toBatchStateName(batch.stateCode));
                batchJson.put("batch_planned", formatDateTimeFromDb(batch.planned));
                batchJson.put("batch_started", formatDateTimeFromDb(batch.started));
                batchJson.put("batch_finished", formatDateTimeFromDb(batch.finished));
                batchJson.put("batch_owner_login", batch.ownerLogin);
                batchJson.put("batch_owner_firstname", batch.ownerFirstname);
                batchJson.put("batch_owner_surname", batch.ownerSurname);
                //batchJson.put("", batch.);
                batchesJson.put(batchJson);
            }
            result.put("total_size", size);
            result.put("items", batchesJson);
            return Response.ok().entity(result.toString()).build();
        } catch (Throwable e) {
            throw new GenericApplicationException(e.getMessage());
        }
    }

    private String formatDateTimeFromDb(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
        }
    }

    private String toBatchStateName(Integer batchStateCode) {
        switch (batchStateCode) {
            //TODO: zmenit, planned by melo byt 0, tady jsem se drzel cislovani podle stavu procesu, ale to uz muzu ignorovat, jen je potreba upravit funkci pro pocitani batch stavu
            case 5:
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
                return 5; //TODO: change to 0
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
}

//TODO:
//http://localhost:8080/search/api/v6.0/processes?offset=0&state=RUNNING
//total_size=1, ale 2 items
