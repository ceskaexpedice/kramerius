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

import cz.incad.kramerius.rest.apiNew.admin.v70.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * ProcessManagerMapper
 * @author ppodsednik
 */
public final class ProcessManagerMapper {

    private ProcessManagerMapper() {
    }

    public static JSONObject mapOwners(JSONObject pcpOwners) {
        if (pcpOwners == null) {
            return null;
        }
        JSONArray ownersJson = new JSONArray();
        JSONArray pcpOwnersArray = pcpOwners.getJSONArray("owners");
        for (int i = 0; i < pcpOwnersArray.length(); i++) {
            JSONObject pcpOwner = pcpOwnersArray.getJSONObject(i);
            JSONObject ownerJson = new JSONObject();
            ownerJson.put("id", pcpOwner.getString("owner"));
            ownerJson.put("name", pcpOwner.getString("owner"));
            ownersJson.put(ownerJson);
        }
        JSONObject result = new JSONObject();
        result.put("owners", ownersJson);
        return result;
    }

    public static JSONObject mapBatchWithProcesses(JSONObject pcpBatchWithProcesses) {
        JSONObject json = new JSONObject();
        //batch
        JSONObject batch = new JSONObject();
        batch.put("token", pcpBatchWithProcesses.getString("mainProcessId"));
        batch.put("id", pcpBatchWithProcesses.getString("mainProcessId"));
        batch.put("state", pcpBatchWithProcesses.getString("status"));
        if (!pcpBatchWithProcesses.isNull("planned")) {
            batch.put("planned", toFormattedStringOrNull(pcpBatchWithProcesses.getLong("planned")));
        }
        if (!pcpBatchWithProcesses.isNull("started")) {
            batch.put("started", toFormattedStringOrNull(pcpBatchWithProcesses.getLong("started")));
        }
        if (!pcpBatchWithProcesses.isNull("finished")) {
            batch.put("finished", toFormattedStringOrNull(pcpBatchWithProcesses.getLong("finished")));
        }
        batch.put("owner_id", pcpBatchWithProcesses.getString("owner"));
        batch.put("owner_name", pcpBatchWithProcesses.getString("owner"));
        json.put("batch", batch);

        //processes
        JSONArray processArray = new JSONArray();
        JSONArray batchProcesses = pcpBatchWithProcesses.getJSONArray("processes");
        for (int j = 0; j < batchProcesses.length(); j++) {
            JSONObject processInBatch = batchProcesses.getJSONObject(j);
            JSONObject process = new JSONObject();
            process.put("id", processInBatch.getString("processId"));
            process.put("uuid", processInBatch.getString("processId"));
            process.put("defid", processInBatch.getString("profileId"));
            process.put("name", processInBatch.getString("description"));
            process.put("state", processInBatch.getString("status"));
            if (!processInBatch.isNull("planned")) {
                process.put("planned", toFormattedStringOrNull(processInBatch.getLong("planned")));
            }
            if (!processInBatch.isNull("started")) {
                process.put("started", toFormattedStringOrNull(processInBatch.getLong("started")));
            }
            if (!processInBatch.isNull("finished")) {
                process.put("finished", toFormattedStringOrNull(processInBatch.getLong("finished")));
            }
            processArray.put(process);
        }
        json.put("processes", processArray);
        return json;
    }

    /* pcpProcess example
    {
  "owner" : "PePo",
  "workerId" : "curatorWorker",
  "processId" : "ed25ce29-2149-439d-85c4-cc5e516e3036",
  "payload" : {
    "surname" : "Po",
    "name" : "Pe"
  },
  "profileId" : "testPlugin1-big",
  "description" : "Main process for the profile testPlugin1-big",
  "pid" : 9889,
  "started" : 1756202186751,
  "finished" : null,
  "planned" : 1756198668715,
  "batchId" : "ed25ce29-2149-439d-85c4-cc5e516e3036",
  "status" : "RUNNING"
}
     */
    public static JSONObject mapProcess(JSONObject pcpProcess) {
        JSONObject json = new JSONObject();
        //batch
        JSONObject batchJson = new JSONObject();
        batchJson.put("token", pcpProcess.getString("batchId"));
        batchJson.put("id", pcpProcess.getString("batchId"));
        json.put("batch", batchJson);
        //process
        JSONObject processJson = new JSONObject();
        processJson.put("id", pcpProcess.getString("processId"));
        processJson.put("uuid", pcpProcess.getString("processId"));
        processJson.put("defid", pcpProcess.getString("profileId"));
        processJson.put("name", pcpProcess.getString("description"));
        processJson.put("state", pcpProcess.getString("status"));
        if (!pcpProcess.isNull("planned")) {
            processJson.put("planned", toFormattedStringOrNull(pcpProcess.getLong("planned")));
        }
        if (!pcpProcess.isNull("started")) {
            processJson.put("started", toFormattedStringOrNull(pcpProcess.getLong("started")));
        }
        if (!pcpProcess.isNull("finished")) {
            processJson.put("finished", toFormattedStringOrNull(pcpProcess.getLong("finished")));
        }
        JSONObject result = new JSONObject();
        result.put("process", processJson);
        result.put("batch", batchJson);
        return result;
    }

    public static JSONObject mapScheduleMainProcess(JSONObject krSchedule, String owner) {
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
        boolean justTemp = true;
        if(justTemp){
            String scheduleMainProcess = "            {" +
                    "              \"profileId\" : \"testPlugin1-small\"," +
                    "              \"payload\" : {" +
                    "                \"surname\" : \"Po\"," +
                    "                \"name\" : \"Pe\"" +
                    "              }," +
                    "              \"ownerId\" : \"PePo\"" +
                    "            }";
            return new JSONObject(scheduleMainProcess);
        }



        JSONObject result = new JSONObject();
        if (krSchedule.has("defid")) {
            result.put("profileId", krSchedule.getString("defid"));
        }
        if (krSchedule.has("params")) {
            result.put("payload", krSchedule.getJSONObject("params"));
        }
        result.put("ownerId", owner);
        return result;
    }

    public static String toFormattedStringOrNull(long timeMillis) {
        return Utils.toFormattedStringOrNull(timeMillis / 1000);
    }
}
