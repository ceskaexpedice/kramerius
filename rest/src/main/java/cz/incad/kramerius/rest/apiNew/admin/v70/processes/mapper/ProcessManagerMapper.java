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

}
