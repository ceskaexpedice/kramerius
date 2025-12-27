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
package cz.incad.kramerius.processes.client;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * ProcessManagerMapper
 * @author ppodsednik
 */
public final class ProcessManagerMapper {
    // general
    public static final String PLANNED = "planned";
    public static final String STARTED = "started";
    public static final String FINISHED = "finished";
    public static final String PROCESSES = "processes";
    public static final String PROCESS = "process";
    public static final String BATCH = "batch";
    public static final String BATCHES = "batches";
    public static final String OFFSET = "offset";
    public static final String LIMIT = "limit";
    public static final String WORKERS ="workers";

    public static final String PCP_STATUS = "status";
    public static final String PCP_DESCRIPTION = "description";
    public static final String PCP_PLUGIN_ID = "pluginId";
    public static final String PCP_PROFILE_ID = "profileId";
    public static final String PCP_JVM_ARGS = "jvmArgs";
    public static final String PCP_SCHEDULED_PROFILES = "scheduledProfiles";
    public static final String PCP_SCHEDULE_MAIN_PROCESS = "scheduleMainProcess";
    public static final String PCP_PROCESS_ID = "processId";
    public static final String PCP_PAYLOAD = "payload";
    public static final String PCP_TOTAL_SIZE = "totalSize";
    public static final String PCP_OWNERS = "owners";
    public static final String PCP_OWNER_ID = "owner";
    public static final String PCP_OWNER_ID_SCH = "ownerId";
    public static final String PCP_OWNER_NAME = "owner";
    public static final String PCP_MAIN_PROCESS_ID = "mainProcessId";
    public static final String PCP_BATCH_ID = "batchId";
    public static final String PCP_WORKER_ID = "workerId";

    public static final String KR_PROCESS_UUID = "uuid";
    public static final String KR_PROFILE_ID = "defid";
    public static final String KR_ID = "id";
    public static final String KR_STATUS = "state";
    public static final String KR_DESCRIPTION = "name";
    public static final String KR_PAYLOAD = "params";
    public static final String KR_TOTAL_SIZE = "total_size";
    public static final String KR_OWNERS = "owners";
    public static final String KR_OWNER_NAME = "name";
    public static final String KR_BATCH_ID = "batch_id";
    public static final String KR_BATCH_TOKEN = "token";
    public static final String KR_BATCH_TOKEN_1 = "batch_token";
    public static final String KR_BATCH_OWNER_ID = "owner_id";
    public static final String KR_BATCH_OWNER_NAME = "owner_name";
    public static final String KR_PROCESSES_DELETED = "processes_deleted";

    public static final String KR_WORKER="worker";

    private ProcessManagerMapper() {
    }

    public static JSONObject mapOwners(JSONObject pcpOwners) {
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

    public static JSONObject mapBatchWithProcesses(JSONObject pcpBatchWithProcesses) {
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

        Set<String> workers = new LinkedHashSet<>();

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
            if (!processInBatch.isNull(PCP_WORKER_ID)) {
                String wid = processInBatch.getString(PCP_WORKER_ID);
                workers.add(wid);
                process.put(KR_WORKER, wid);
            }
            processArray.put(process);
        }

        JSONArray workersArray = new JSONArray();
        workers.stream().forEach(workersArray::put);
        batch.put(WORKERS, workersArray);

        json.put(PROCESSES, processArray);
        return json;
    }

    public static JSONObject mapProcess(JSONObject pcpProcess) {
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

    public static JSONObject mapScheduleMainProcess(JSONObject krSchedule, String owner) {
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

    public static JSONObject mapLogLines(JSONObject pcpLogLines) {
        if (pcpLogLines.has(PCP_TOTAL_SIZE)) {
            Object value = pcpLogLines.remove(PCP_TOTAL_SIZE);
            pcpLogLines.put(KR_TOTAL_SIZE, value);
        }
        return pcpLogLines;
    }

    private static String toFormattedStringOrNull(long timeMillis) {
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(timeMillis / 1000, 0, ZoneOffset.UTC);
        return toFormattedStringOrNull(localDateTime);
    }


    private static String toFormattedStringOrNull(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        } else {
            return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
        }
    }

}
