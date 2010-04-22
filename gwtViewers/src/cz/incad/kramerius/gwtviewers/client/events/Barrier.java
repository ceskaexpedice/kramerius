package cz.incad.kramerius.gwtviewers.client.events;

public class Barrier {
	
	private int bound = 0;
	private int pocitadlo = 0;
	private BarrierAction action;
	
	public Barrier(int bound, BarrierAction action) {
		super();
		this.bound = bound;
		this.action = action;
	}
	
	public void bound() {
		this.pocitadlo +=1; 
		boolean runAction = false;
		if (pocitadlo == this.bound) {
			this.action.action();
			runAction = true;
		}
		//barrierMessage(this.pocitadlo, runAction);
	}

	
}
