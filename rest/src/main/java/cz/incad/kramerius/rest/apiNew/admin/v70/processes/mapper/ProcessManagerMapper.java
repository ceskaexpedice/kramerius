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
package cz.incad.kramerius.rest.apiNew.admin.v70.processes.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.incad.kramerius.rest.apiNew.admin.v70.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * ProcessManagerMapper
 * @author ppodsednik
 */
public final class ProcessManagerMapper {
    private static final ObjectMapper mapper = new ObjectMapper();

    private ProcessManagerMapper(){}

    public static List<ProcessOwner> mapOwners(JSONObject owners) {
        if(owners == null){
            return new ArrayList<>();
        }
        JSONArray ownersA = owners.getJSONArray("owners");
        List<ProcessOwner> processOwners = new ArrayList<>();
        for (int i = 0; i < ownersA.length(); i++) {
            JSONObject jsonObject = ownersA.getJSONObject(i);
            ProcessOwner processOwner = new ProcessOwner();
            processOwner.id = jsonObject.getString("owner");
            processOwner.name = processOwner.id;
            processOwners.add(processOwner);
        }
        return processOwners;
    }

    public static ProcessInBatch mapProcess(JSONObject process) {
        // TODO pepo
        if(process == null){
            return null;
        }
        ProcessInBatch processInBatch = new ProcessInBatch();
        //processInBatch.batchToken = rs.getString("batch_token");
        processInBatch.batchId = process.getString("batchId");
        //processInBatch.batchStateCode = rs.getInt("batch_state");
        //processInBatch.batchPlanned = toLocalDateTime(rs.getTimestamp("batch_planned"));
        //processInBatch.batchStarted = toLocalDateTime(rs.getTimestamp("batch_started"));
        //processInBatch.batchFinished = toLocalDateTime(rs.getTimestamp("batch_finished"));
        processInBatch.batchOwnerId = process.getString("owner");
        //processInBatch.batchOwnerName = rs.getString("batch_owner_name");
        //processInBatch.batchSize = rs.getInt("batch_size");

        processInBatch.processId = process.getString("processId");
        //processInBatch.processUuid = rs.getString("process_uuid");
        processInBatch.processDefid = process.getString("profileId");
        processInBatch.processName = process.getString("description");
        //processInBatch.processStateCode = rs.getInt("process_state");
        //processInBatch.processPlanned = toLocalDateTime(rs.getTimestamp("process_planned"));
        //processInBatch.processStarted = toLocalDateTime(rs.getTimestamp("process_started"));
        //processInBatch.processFinished = toLocalDateTime(rs.getTimestamp("process_finished"));
        return processInBatch;
    }

    public static JSONObject mapBatchWithProcesses(JSONObject batchWithProcesses) {
        JSONObject json = new JSONObject();
        //batch
        JSONObject batchJson = new JSONObject();
//        batchJson.put("token", batchWithProcesses.token);
        batchJson.put("token", batchWithProcesses.getString("mainProcessId"));
        // batchJson.put("id", batchWithProcesses.firstProcessId);
        batchJson.put("id", batchWithProcesses.getString("mainProcessId"));
        // batchJson.put("state", toBatchStateName(batchWithProcesses.stateCode));
        batchJson.put("state", batchWithProcesses.getString("status"));

        // batchJson.put("planned", Utils.toFormattedStringOrNull(batchWithProcesses.planned));
        batchJson.put("planned", Utils.toFormattedStringOrNull(batchWithProcesses.getInt("planned")));
        // batchJson.put("started", Utils.toFormattedStringOrNull(batchWithProcesses.started));
        batchJson.put("started", Utils.toFormattedStringOrNull(batchWithProcesses.getInt("started")));
        // batchJson.put("finished", Utils.toFormattedStringOrNull(batchWithProcesses.finished));
        if(!batchWithProcesses.isNull("finished")){
            batchJson.put("finished", Utils.toFormattedStringOrNull(batchWithProcesses.getInt("finished")));
        }
        // batchJson.put("owner_id", batchWithProcesses.ownerId);
        batchJson.put("owner_id", batchWithProcesses.getString("owner"));
        // batchJson.put("owner_name", batchWithProcesses.ownerName);
        batchJson.put("owner_name", batchWithProcesses.getString("owner"));


        json.put("batch", batchJson);
        //processes
        JSONArray processArray = new JSONArray();

        JSONArray batchProcesses = batchWithProcesses.getJSONArray("processes");
        for (int j = 0; j < batchProcesses.length(); j++) {
            JSONObject processInBatch = batchProcesses.getJSONObject(j);


            JSONObject processJson = new JSONObject();
            //processJson.put("id", process.id);
            processJson.put("id", processInBatch.getString("processId"));
            //processJson.put("uuid", process.uuid);
            processJson.put("uuid", processInBatch.getString("processId"));
            //processJson.put("defid", process.defid);
            processJson.put("defid", processInBatch.getString("profileId"));
            //processJson.put("name", process.name);
            processJson.put("name", processInBatch.getString("description"));
            //processJson.put("state", toProcessStateName(process.stateCode));
            processJson.put("state", processInBatch.getString("status"));
            //processJson.put("planned", Utils.toFormattedStringOrNull(process.planned));
            processJson.put("planned", Utils.toFormattedStringOrNull(processInBatch.getInt("planned")));
            //processJson.put("started", Utils.toFormattedStringOrNull(process.started));
            processJson.put("started", Utils.toFormattedStringOrNull(processInBatch.getInt("started")));
            //processJson.put("finished", Utils.toFormattedStringOrNull(process.finished));
            if(!processInBatch.isNull("finished")){
                processJson.put("finished", Utils.toFormattedStringOrNull(processInBatch.getInt("finished")));
            }
            processArray.put(processJson);
        }
        json.put("processes", processArray);


        return json;
    }

}
