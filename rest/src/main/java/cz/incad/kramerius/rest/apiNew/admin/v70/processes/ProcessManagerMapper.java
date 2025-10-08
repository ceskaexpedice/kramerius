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
final class ProcessManagerMapper {
    // general
    static final String PLANNED = "planned";
    static final String STARTED = "started";
    static final String FINISHED = "finished";
    static final String PROCESSES = "processes";
    static final String PROCESS = "process";
    static final String BATCH = "batch";
    static final String BATCHES = "batches";
    static final String OFFSET = "offset";
    static final String LIMIT = "limit";

    static final String PCP_STATUS = "status";
    static final String PCP_DESCRIPTION = "description";
    static final String PCP_PLUGIN_ID = "pluginId";
    static final String PCP_PROFILE_ID = "profileId";
    static final String PCP_SCHEDULED_PROFILES = "scheduledProfiles";
    static final String PCP_PROCESS_ID = "processId";
    static final String PCP_PAYLOAD = "payload";
    static final String PCP_TOTAL_SIZE = "totalSize";
    static final String PCP_OWNERS = "owners";
    static final String PCP_OWNER_ID = "owner";
    static final String PCP_OWNER_ID_SCH = "ownerID";
    static final String PCP_OWNER_NAME = "owner";
    static final String PCP_MAIN_PROCESS_ID = "mainProcessId";
    static final String PCP_BATCH_ID = "batchId";

    static final String KR_PROCESS_UUID = "uuid";
    static final String KR_PROFILE_ID = "defid";
    static final String KR_ID = "id";
    static final String KR_STATUS = "state";
    static final String KR_DESCRIPTION = "name";
    static final String KR_PAYLOAD = "params";
    static final String KR_TOTAL_SIZE = "total_size";
    static final String KR_OWNERS = "owners";
    static final String KR_OWNER_NAME = "name";
    static final String KR_BATCH_ID = "batch_id";
    static final String KR_BATCH_TOKEN = "token";
    static final String KR_BATCH_TOKEN_1 = "batch_token";
    static final String KR_BATCH_OWNER_ID = "owner_id";
    static final String KR_BATCH_OWNER_NAME = "owner_name";
    static final String KR_PROCESSES_DELETED = "processes_deleted";

    private ProcessManagerMapper() {
    }

    static JSONObject mapOwners(JSONObject pcpOwners) {
        if (pcpOwners == null) {
            return null;
        }
        JSONArray ownersJson = new JSONArray();
        JSONArray pcpOwnersArray = pcpOwners.getJSONArray(PCP_OWNERS);
        for (int i = 0; i < pcpOwnersArray.length(); i++) {
            JSONObject pcpOwner = pcpOwnersArray.getJSONObject(i);
            JSONObject ownerJson = new JSONObject();
            ownerJson.put(KR_ID, pcpOwner.getString(PCP_OWNER_ID));
            ownerJson.put(KR_OWNER_NAME, pcpOwner.getString(PCP_OWNER_NAME));
            ownersJson.put(ownerJson);
        }
        JSONObject result = new JSONObject();
        result.put(KR_OWNERS, ownersJson);
        return result;
    }

    static JSONObject mapBatchWithProcesses(JSONObject pcpBatchWithProcesses) {
        JSONObject json = new JSONObject();
        //batch
        JSONObject batch = new JSONObject();
        batch.put(KR_BATCH_TOKEN, pcpBatchWithProcesses.getString(PCP_MAIN_PROCESS_ID));
        batch.put(KR_ID, pcpBatchWithProcesses.getString(PCP_MAIN_PROCESS_ID));
        batch.put(KR_STATUS, pcpBatchWithProcesses.getString(PCP_STATUS));
        if (!pcpBatchWithProcesses.isNull(PLANNED)) {
            batch.put(PLANNED, toFormattedStringOrNull(pcpBatchWithProcesses.getLong(PLANNED)));
        }
        if (!pcpBatchWithProcesses.isNull(STARTED)) {
            batch.put(STARTED, toFormattedStringOrNull(pcpBatchWithProcesses.getLong(STARTED)));
        }
        if (!pcpBatchWithProcesses.isNull(FINISHED)) {
            batch.put(FINISHED, toFormattedStringOrNull(pcpBatchWithProcesses.getLong(FINISHED)));
        }
        batch.put(KR_BATCH_OWNER_ID, pcpBatchWithProcesses.getString(PCP_OWNER_ID));
        batch.put(KR_BATCH_OWNER_NAME, pcpBatchWithProcesses.getString(PCP_OWNER_NAME));
        json.put(BATCH, batch);

        //processes
        JSONArray processArray = new JSONArray();
        JSONArray batchProcesses = pcpBatchWithProcesses.getJSONArray(PROCESSES);
        for (int j = 0; j < batchProcesses.length(); j++) {
            JSONObject processInBatch = batchProcesses.getJSONObject(j);
            JSONObject process = new JSONObject();
            process.put(KR_ID, processInBatch.getString(PCP_PROCESS_ID));
            process.put(KR_PROCESS_UUID, processInBatch.getString(PCP_PROCESS_ID));
            process.put(KR_PROFILE_ID, processInBatch.getString(PCP_PROFILE_ID));
            process.put(KR_DESCRIPTION, processInBatch.getString(PCP_DESCRIPTION));
            process.put(KR_STATUS, processInBatch.getString(PCP_STATUS));
            if (!processInBatch.isNull(PLANNED)) {
                process.put(PLANNED, toFormattedStringOrNull(processInBatch.getLong(PLANNED)));
            }
            if (!processInBatch.isNull(STARTED)) {
                process.put(STARTED, toFormattedStringOrNull(processInBatch.getLong(STARTED)));
            }
            if (!processInBatch.isNull(FINISHED)) {
                process.put(FINISHED, toFormattedStringOrNull(processInBatch.getLong(FINISHED)));
            }
            processArray.put(process);
        }
        json.put(PROCESSES, processArray);
        return json;
    }

    static JSONObject mapProcess(JSONObject pcpProcess) {
        JSONObject json = new JSONObject();
        //batch
        JSONObject batchJson = new JSONObject();
        batchJson.put(KR_BATCH_TOKEN, pcpProcess.getString(PCP_BATCH_ID));
        batchJson.put(KR_ID, pcpProcess.getString(PCP_BATCH_ID));
        json.put(BATCH, batchJson);
        //process
        JSONObject processJson = new JSONObject();
        processJson.put(KR_ID, pcpProcess.getString(PCP_PROCESS_ID));
        processJson.put(KR_PROCESS_UUID, pcpProcess.getString(PCP_PROCESS_ID));
        processJson.put(KR_PROFILE_ID, pcpProcess.getString(PCP_PROFILE_ID));
        processJson.put(KR_DESCRIPTION, pcpProcess.getString(PCP_DESCRIPTION));
        processJson.put(KR_STATUS, pcpProcess.getString(PCP_STATUS));
        if (!pcpProcess.isNull(PLANNED)) {
            processJson.put(PLANNED, toFormattedStringOrNull(pcpProcess.getLong(PLANNED)));
        }
        if (!pcpProcess.isNull(STARTED)) {
            processJson.put(STARTED, toFormattedStringOrNull(pcpProcess.getLong(STARTED)));
        }
        if (!pcpProcess.isNull(FINISHED)) {
            processJson.put(FINISHED, toFormattedStringOrNull(pcpProcess.getLong(FINISHED)));
        }
        JSONObject result = new JSONObject();
        result.put(PROCESS, processJson);
        result.put(BATCH, batchJson);
        return result;
    }

    static JSONObject mapScheduleMainProcess(JSONObject krSchedule, String owner) {
        JSONObject result = new JSONObject();
        if (krSchedule.has(KR_PROFILE_ID)) {
            result.put(PCP_PROFILE_ID, krSchedule.getString(KR_PROFILE_ID));
        }
        if (krSchedule.has(KR_PAYLOAD)) {
            result.put(PCP_PAYLOAD, krSchedule.getJSONObject(KR_PAYLOAD));
        }
        result.put(PCP_OWNER_ID_SCH, owner);
        return result;
    }

    static JSONObject mapLogLines(JSONObject pcpLogLines) {
        if (pcpLogLines.has(PCP_TOTAL_SIZE)) {
            Object value = pcpLogLines.remove(PCP_TOTAL_SIZE);
            pcpLogLines.put(KR_TOTAL_SIZE, value);
        }
        return pcpLogLines;
    }

    private static String toFormattedStringOrNull(long timeMillis) {
        return Utils.toFormattedStringOrNull(timeMillis / 1000);
    }
}
