package cz.incad.kramerius.processes;

import java.io.IOException;
import java.util.ArrayList;
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

    public GCCheckFoundCandidatesTask(LRProcessManager lrProcessManager,
            GCScheduler gcScheduler, List<String> uuids, long interval) {
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
                    LRProcess lr = this.lrProcessManager
                            .getLongRunningProcess(uuid);
                    if (lr.getProcessState().equals(States.RUNNING)) {
                        if (lr.getPid() != null) {
                            if (!pids.contains(lr.getPid())) {
                                LOGGER.warning("changing state of process '"
                                        + lr.getUUID() + "' to FAILED");
                                lr.setProcessState(States.FAILED);
                                lr.setFinishedTime(System.currentTimeMillis());
                                this.lrProcessManager
                                        .updateLongRunningProcessState(lr);
                                this.lrProcessManager
                                        .updateLongRunningProcessFinishedDate(lr);

                                updateMasterState(lr);

                            }
                        } else {
                            LOGGER.severe("cannot find pid for process '"
                                    + lr.getUUID() + "' -> change state to NOT_RUNNING");
                            lr.setProcessState(States.NOT_RUNNING);
                            lr.setFinishedTime(System.currentTimeMillis());
                            this.lrProcessManager
                                    .updateLongRunningProcessState(lr);
                            this.lrProcessManager
                                    .updateLongRunningProcessFinishedDate(lr);
                            updateMasterState(lr);
                        }
                    }
                }
            } finally {
                lock.unlock();
            }

            this.gcScheduler.scheduleFindGCCandidates();

        } catch (Throwable e) {
            this.gcScheduler.shutdown();
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void updateMasterState(LRProcess lr) {
        List<LRProcess> processes = this.lrProcessManager
                .getLongRunningProcessesByGroupToken(lr.getGroupToken());
        if (processes.size() > 1) {
            LOGGER.fine("calculating new master state");
            List<States> childStates = new ArrayList<States>();
            for (int i = 0, ll = processes.size(); i < ll; i++) {
                childStates.add(processes.get(i).getProcessState());
            }
            processes.get(0).setBatchState(
                    BatchStates.calculateBatchState(childStates));
            LOGGER.fine("calculated state '" + processes.get(0) + "'");
            this.lrProcessManager.updateLongRunninngProcessBatchState(processes
                    .get(0));
        }
    }

}
