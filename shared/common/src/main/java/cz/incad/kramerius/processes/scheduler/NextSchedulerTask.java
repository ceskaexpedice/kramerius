package cz.incad.kramerius.processes.scheduler;

import java.util.*;
import java.util.logging.Level;

import cz.incad.kramerius.processes.client.ProcessManagerClient;
import cz.incad.kramerius.processes.client.ProcessManagerMapper;
import cz.incad.kramerius.processes.definition.ProcessDefinition;
import cz.incad.kramerius.processes.definition.ProcessDefinitionManager;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Scheduler is able to start new process
 *
 * @author pavels
 */
public class NextSchedulerTask extends TimerTask {

    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(NextSchedulerTask.class.getName());

    private ProcessScheduler processScheduler;
    private ProcessDefinitionManager definitionManager;
    private CloseableHttpClient apacheClient;

    public NextSchedulerTask(ProcessDefinitionManager definitionManager,
                             ProcessScheduler processScheduler, long interval, CloseableHttpClient apacheClient) {
        super();
        this.definitionManager = definitionManager;
        this.processScheduler = processScheduler;
        this.apacheClient = apacheClient;
    }

    @Override
    public void run() {
        try {
            LOGGER.fine("Scheduling next task");
            definitionManager.load();
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            JSONArray profiles = processManagerClient.getProfiles();
            for (int i = 0; i < profiles.length(); i++) {
                JSONObject profile = profiles.getJSONObject(i);
                String profileId = profile.getString(ProcessManagerMapper.PCP_PROFILE_ID);
                ProcessDefinition processDefinition = definitionManager.getProcessDefinition(profileId);
                if (processDefinition == null) {
                    LOGGER.warning("No process definition with id " + profileId);
                    continue;
                }
                List<String> jvmArgsLocal = processDefinition.getJavaProcessParameters();
                JSONArray jvmArgs = profile.getJSONArray(ProcessManagerMapper.PCP_JVM_ARGS);
                if (!areEqualIgnoreOrder(jvmArgsLocal, jvmArgs)) {
                    if(jvmArgsLocal == null) {
                        jvmArgsLocal = new ArrayList<>();
                    }
                    profile.put(ProcessManagerMapper.PCP_JVM_ARGS, new JSONArray(jvmArgsLocal));
                    processManagerClient.updateProfile(profileId, profile);
                }
            }
            this.processScheduler.scheduleNextTask();
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    private static boolean areEqualIgnoreOrder(List<String> list, JSONArray jsonArray) {
        if ((list == null || list.isEmpty()) && (jsonArray == null || (jsonArray.length() == 0))) {
            return true;
        }
        if (list == null || jsonArray == null) {
            return false;
        }
        if (list.size() != jsonArray.length()) {
            return false;
        }
        Set<String> listSet = new HashSet<>(list);
        Set<String> jsonSet = new HashSet<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            jsonSet.add(jsonArray.optString(i, null)); // optString avoids exceptions
        }
        return listSet.equals(jsonSet);
    }
}
