package cz.incad.kramerius.processes;

import java.util.List;

import cz.incad.kramerius.processes.utils.ProcessUtils;

/**
 * Processes states
 * @author pavels
 */
public enum States {
	
    
    /**
     * Not running state = process just has been created
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
	 * Planned = process has been created and waiting to start
	 */
	PLANNED(5),
	
	
    BATCH_STARTED(6),
    
    BATCH_FAILED(7),
    
    BATCH_FINISHED(8);

	
	public static States load(int v) {
		for (States st : States.values()) {
			if (st.getVal() == v) return st;
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

    public static States calculateBatchState(List<States> childStates) {
        // ve stavu planned nebo running 
        if (one(childStates, FAILED)) {
            return BATCH_FAILED;
        } else {
            if (one(childStates,PLANNED, RUNNING)) {
                return BATCH_STARTED;
            }
            return BATCH_FINISHED;
        }
    }

    public static boolean one(List<States> childStates, States... exp) {
        for (States st : childStates) {
            if (expect(st, exp)) return true; 
        }
        return false;
    }

    public static boolean all(List<States> childStates, States ... exp) {
        for (States st : childStates) {
            if (!expect(st, exp)) return false; 
        }
        return true;
    }

    public static boolean expect(States real, States ... expected) {
        for (States exp : expected) {
            if (real.equals(exp)) return true;
        }
        return false;
    }
}
