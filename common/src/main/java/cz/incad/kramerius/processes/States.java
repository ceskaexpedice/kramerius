package cz.incad.kramerius.processes;

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
	PLANNED(5);

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
}
