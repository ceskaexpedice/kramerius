package cz.incad.kramerius.processes;

import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;

import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Scheduler is able to start new process
 * @author pavels
 */
public class NextSchedulerTask extends TimerTask {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(NextSchedulerTask.class.getName());
	
	private ProcessScheduler processScheduler;
	private LRProcessManager lrProcessManager;
	private DefinitionManager definitionManager;
	
	public NextSchedulerTask(LRProcessManager lrProcessManager, DefinitionManager definitionManager, ProcessScheduler processScheduler,  long interval) {
		super();
		this.lrProcessManager = lrProcessManager;
		this.definitionManager = definitionManager;
		this.processScheduler = processScheduler;
	}

	@Override
	public void run() {
		try {
			// neco delej
			definitionManager.load();
			List<LRProcess> plannedProcess = lrProcessManager.getPlannedProcess(allowRunningProcesses());
			if (!plannedProcess.isEmpty()) {
				int runningProcesses = 0;
				List<LRProcess> longRunningProcesses = this.lrProcessManager.getLongRunningProcessesAsFlat(null, null, null);
				for (LRProcess lrProcess : longRunningProcesses) {
					if (lrProcess.getProcessState().equals(States.RUNNING)) {
						runningProcesses +=1;
					}
				}
				if (runningProcesses < allowRunningProcesses()) {
					LRProcess lrProcess = plannedProcess.get(0);
					lrProcess.startMe(false, this.processScheduler.getApplicationLib(), this.processScheduler.getAdditionalJarFiles());
				}
			}  else {
				LOGGER.fine("no planned process found");
			}
			this.processScheduler.scheduleNextTask();
		} catch(Throwable e) {
			this.processScheduler.shutdown();
			LOGGER.log(Level.SEVERE,e.getMessage(), e);
		}
		
	}

	private int allowRunningProcesses() {
		String aProcess = KConfiguration.getInstance().getProperty("processQueue.activeProcess","1");
		return Integer.parseInt(aProcess);
	}
}
