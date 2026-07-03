package cz.incad.kramerius.rest.apiNew.admin.v70.processes.utils;

import cz.incad.kramerius.processes.client.ProcessManagerClient;
import cz.incad.kramerius.processes.client.ProcessManagerMapper;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;

public class APIProcessScheduler {


    public static JSONObject createScheduleProcess(String profileId, Map<String, String> payload, String ownerId) {
        JSONObject json = new JSONObject();
        json.put(ProcessManagerMapper.PCP_PROFILE_ID, profileId);
        json.put(ProcessManagerMapper.PCP_PAYLOAD, new JSONObject(payload));
        json.put(ProcessManagerMapper.PCP_OWNER_ID_SCH, ownerId);
        return json;
    }

    public static JSONObject scheduleMainProcess(CloseableHttpClient apacheClient, JSONObject scheduleReindexationPar) {
        ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);

        String profileId = scheduleReindexationPar.getString(ProcessManagerMapper.PCP_PROFILE_ID);
        JSONObject profile = processManagerClient.getProfile(profileId);
        JSONObject plugin = processManagerClient.getPlugin(profile.getString(ProcessManagerMapper.PCP_PLUGIN_ID));
        JSONArray scheduledProfiles = null;
        if (!plugin.isNull(ProcessManagerMapper.PCP_SCHEDULED_PROFILES)) {
            scheduledProfiles = plugin.getJSONArray(ProcessManagerMapper.PCP_SCHEDULED_PROFILES);
        }

        String processId = processManagerClient.scheduleProcess(scheduleReindexationPar);
        JSONObject result = new JSONObject();
        result.put(ProcessManagerMapper.PCP_PROCESS_ID, processId);
        return result;
    }
}
