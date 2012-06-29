package cz.incad.kramerius.processes;

import java.io.IOException;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;

import cz.incad.kramerius.processes.utils.PIDList;

public class GCCheckFoundCandidatesTask extends TimerTask {

	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger
			.getLogger(GCCheckFoundCandidatesTask.class.getName());
	
	private LRProcessManager lrProcessManager;
	private GCScheduler gcScheduler;
	private List<String> uuids;
	
	public GCCheckFoundCandidatesTask(LRProcessManager lrProcessManager,  GCScheduler gcScheduler, List<String> uuids,  long interval) {
		super();
		this.lrProcessManager = lrProcessManager;
		this.gcScheduler = gcScheduler;
		this.uuids = uuids;
	}

	@Override
	public void run() {
		Lock lock = lrProcessManager.getSynchronizingLock();
		try {
			List<String> pids = PIDList.createPIDList().getProcessesPIDS();
            try {
                lock.lock();
                for (String uuid : this.uuids) {
					LRProcess lr = this.lrProcessManager.getLongRunningProcess(uuid);
					if (lr.getProcessState().equals(States.RUNNING)) {
						if (lr.getPid()!=null) {
							if (!pids.contains(lr.getPid())) {
							    LOGGER.warning("changing state of process '"+lr.getUUID()+"' to FAILED");
							    lr.setProcessState(States.FAILED);
								this.lrProcessManager.updateLongRunningProcessState(lr);
							}
						} else {
							LOGGER.severe("cannot find pid for process '"+lr.getUUID()+"'");
							lr.setProcessState(States.FAILED);
							this.lrProcessManager.updateLongRunningProcessState(lr);
						}
					}
                }
            } finally {
                lock.unlock();
            }

			this.gcScheduler.scheduleFindGCCandidates();
			
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		} catch (InterruptedException e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	
}
