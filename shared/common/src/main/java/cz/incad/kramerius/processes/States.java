package cz.incad.kramerius.processes;

import java.util.List;

import cz.incad.kramerius.processes.utils.ProcessUtils;

/**
 * Processes states
 * 
 * @author pavels
 */
public enum States {

    /**
     * Not running process
     */
    NOT_RUNNING(0),

    /**
     * Running proces
     */
    RUNNING(1),

    /**
     * Correct finished proces
     */
    FINISHED(2),

    /**
     * FAiled with some errors
     */
    FAILED(3),

    /**
     * Killed process
     */
    KILLED(4),

    /**
     * Planned (process is waiting to start)
     */
    PLANNED(5),

    /**
     * Finished with some errors
     */
    WARNING(9),
    
    /*
     * WARNING(10)
     */
    
    /**
     * Batch process started (contains child processes and all of them are
     * PLANNED or RUNNING).
     */
    @Deprecated
    BATCH_STARTED(6),

    
    /**
     * Batch process failed (some of child process FAILED)
     */
    @Deprecated
    BATCH_FAILED(7),

    /**
     * Batch process finished (all child processes finished with state FINISH)
     */
    @Deprecated
    BATCH_FINISHED(8);

    

    /**
     * Load state from value
     * @param v Given value of state
     * @return
     */
    public static States load(int v) {
        for (States st : States.values()) {
            if (st.getVal() == v)
                return st;
        }
        return null;
    }

    private int val;

    private States(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

//    /**
//     * Calculate master batch process state
//     * @param childStates Child process states
//     * @return
//     */
//    public static States calculateBatchState(List<States> childStates) {
//        // ve stavu planned nebo running
//        if (one(childStates, FAILED)) {
//            return BATCH_FAILED;
//        } else {
//            if (one(childStates, PLANNED, RUNNING)) {
//                return BATCH_STARTED;
//            }
//            return BATCH_FINISHED;
//        }
//    }

    /**
     * Returns true if one of given childStates contains any expecting state
     * @param childStates Child states
     * @param exp Expecting states
     * @return
     */
    public static boolean one(List<States> childStates, States... exp) {
        for (States st : childStates) {
            if (expect(st, exp))
                return true;
        }
        return false;
    }

    /**
     * Returns true if all of given child states contains any expecting state
     * @param childStates Child states
     * @param exp Execting state
     * @return
     */
    public static boolean all(List<States> childStates, States... exp) {
        for (States st : childStates) {
            if (!expect(st, exp))
                return false;
        }
        return true;
    }

    /**
     * Returns true if given real state is one of expecting state
     * @param real Real state
     * @param expected Expecting states
     * @return
     */
    public static boolean expect(States real, States... expected) {
        for (States exp : expected) {
            if (real.equals(exp))
                return true;
        }
        return false;
    }

    
    
    /**
     * Returns true given state is not running state
     * @param realState
     * @return
     */
    public static boolean notRunningState(States realState) {
        return expect(realState, States.FAILED, States.FINISHED, States.KILLED, States.NOT_RUNNING, States.WARNING, States.BATCH_FAILED, States.BATCH_FINISHED, States.BATCH_STARTED);
    }
}
