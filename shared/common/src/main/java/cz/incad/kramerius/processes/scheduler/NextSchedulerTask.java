package cz.incad.kramerius.processes.scheduler;

import java.util.TimerTask;
import java.util.logging.Level;

import cz.incad.kramerius.processes.client.ProcessManagerClient;
import cz.incad.kramerius.processes.definition.ProcessDefinitionManager;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
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
            // TODO pepo - get profiles, compare with lp.xml, update jvm args on pcp
            LOGGER.fine("Scheduling next task");
            definitionManager.load();
            ProcessManagerClient processManagerClient = new ProcessManagerClient(apacheClient);
            JSONObject owners = processManagerClient.getOwners();
            /*
            List<LRProcess> plannedProcess = lrProcessManager.getPlannedProcess(allowRunningProcesses());
            if (!plannedProcess.isEmpty() && this.processScheduler.getApplicationLib() != null ) {
                List<LRProcess> longRunningProcesses = lrProcessManager.getLongRunningProcesses(States.RUNNING);
                if (longRunningProcesses.size() < allowRunningProcesses()) {
                    LRProcess lrProcess = plannedProcess.get(0);
                    lrProcess.startMe(false, this.processScheduler.getApplicationLib(),
                            this.processScheduler.getAdditionalJarFiles());
                } else {
                    LOGGER.fine("The maximum number of running processes is reached");
                }
            } else {
                if (this.processScheduler.getApplicationLib() == null) {
                    LOGGER.fine("Scheduler is not initialized");
                } else {
                    LOGGER.fine("No planned process found");
                }
            }
            */
            this.processScheduler.scheduleNextTask();
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }

    }

    private int allowRunningProcesses() {
        String aProcess = KConfiguration.getInstance().getProperty("processQueue.activeProcess", "1");
        return Integer.parseInt(aProcess);
    }
}
