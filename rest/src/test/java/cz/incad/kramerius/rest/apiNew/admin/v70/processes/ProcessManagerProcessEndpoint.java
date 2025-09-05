/*
 * Copyright (C) 2025 Inovatika
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.rest.apiNew.admin.v70.processes;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

// TODO pepo
/**
 * ProcessManagerProcessEndpoint
 * @author ppodsednik
 */
@Path("/process")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProcessManagerProcessEndpoint {

    public ProcessManagerProcessEndpoint() {
    }

    /*
    @POST
    public Response scheduleMainProcess(ScheduleMainProcess scheduleMainProcess) {
        String processId = processService.scheduleMainProcess(scheduleMainProcess);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("processId", processId);
        return APIRestUtilities.jsonPayload(jsonObject.toString());
    }

     */

    @GET
    @Path("{processId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProcess(@PathParam("processId") String processId) {
        String json = "{" +
                "                  \"processId\": \"ed25ce29-2149-439d-85c4-cc5e516e3036\"," +
                "                  \"description\": \"Main process for the profile testPlugin1-big\"," +
                "                  \"profileId\": \"testPlugin1-big\"," +
                "                  \"workerId\": \"curatorWorker\"," +
                "                  \"pid\": 9889," +
                "                  \"planned\": 1756198668715," +
                "                  \"started\": 1756202186751," +
                "                  \"finished\": null," +
                "                  \"status\": \"RUNNING\"," +
                "                  \"payload\": {" +
                "                    \"name\": \"Pe\"," +
                "                    \"surname\": \"Po\"" +
                "                  }," +
                "                  \"batchId\": \"ed25ce29-2149-439d-85c4-cc5e516e3036\"," +
                "                  \"owner\": \"PePo\"" +
                "                }";
        return jsonPayload(json);
    }

    /*
    @GET
    @Path("batch")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBatches(
            @QueryParam("offset") String offsetStr,
            @QueryParam("limit") String limitStr,
            @QueryParam("owner") String owner,
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("state") String state
    ) {
        int offset = processService.getBatchOffset(offsetStr);
        int limit = processService.getBatchLimit(limitStr);
        BatchFilter batchFilter = processService.createBatchFilter(owner, from, to, state);
        int totalSize = processService.countBatchHeaders(batchFilter);
        JSONObject result = new JSONObject();
        result.put("offset", offset);
        result.put("limit", limit);
        result.put("totalSize", totalSize);

        List<Batch> batches = processService.getBatches(batchFilter, offset, limit);
        JSONArray batchesJson = new JSONArray();
        for (Batch batch : batches) {
            JSONObject batchJson = ProcessServiceMapper.mapBatchToJson(batch);
            batchesJson.put(batchJson);
        }
        result.put("batches", batchesJson);
        return APIRestUtilities.jsonPayload(result.toString());
    }

     */
    /*

    @DELETE
    @Path("batch/{mainProcessId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBatch(@PathParam("mainProcessId") String mainProcessId) {
        int deleted = processService.deleteBatch(mainProcessId);
        JSONObject result = new JSONObject();
        result.put("mainProcessId", mainProcessId);
        result.put("deleted", deleted);
        return APIRestUtilities.jsonPayload(result.toString());
    }

     */
/*
    @DELETE
    @Path("batch/{mainProcessId}/execution")
    @Produces(MediaType.APPLICATION_JSON)
    public Response killBatch(@PathParam("mainProcessId") String mainProcessId) {
        int killed = processService.killBatch(mainProcessId);
        JSONObject result = new JSONObject();
        result.put("mainProcessId", mainProcessId);
        result.put("killed", killed);
        return APIRestUtilities.jsonPayload(result.toString());
    }

 */

    @GET
    @Path("owner")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOwners() {
        String json = "{" +
                "                \"owners\": [" +
                "                  {" +
                "                    \"owner\": \"PePo\"" +
                "                  }," +
                "                  {" +
                "                    \"owner\": \"PaSt\"" +
                "                  }" +
                "                ]" +
                "              }";
        return jsonPayload(json);
    }

    private static Response jsonPayload(String jsonPayload) {
        return Response.ok(jsonPayload, MediaType.APPLICATION_JSON).build();
    }

/*
    @GET
    @Path("{processId}/log/out")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getProcessLogOut(@PathParam("processId") String processId,
                                      @DefaultValue("out.txt") @QueryParam("fileName") String fileName) {
        InputStream logStream = processService.getProcessLog(processId, false);
        return Response.ok((StreamingOutput) output -> {
                    try (logStream) {
                        logStream.transferTo(output);
                    }
                }).header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                .build();
    }

 */
/*
    @GET
    @Path("{processId}/log/err")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getProcessLogErr(@PathParam("processId") String processId,
                                      @DefaultValue("err.txt") @QueryParam("fileName") String fileName) {
        InputStream logStream = processService.getProcessLog(processId, true);
        return Response.ok((StreamingOutput) output -> {
                    try (logStream) {
                        logStream.transferTo(output);
                    }
                }).header("Content-Disposition", "inline; filename=\"" + fileName + "\"")
                .build();
    }

 */

}
