package cz.incad.kramerius.processes;

public enum States {
	
	NOT_RUNNING(0), 
	RUNNING(1), 
	FINISHED(2), 
	FAILED(3), 
	KILLED(4);

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
