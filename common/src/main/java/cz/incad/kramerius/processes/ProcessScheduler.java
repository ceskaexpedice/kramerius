package cz.incad.kramerius.processes;



public interface ProcessScheduler {

	
	public void scheduleNextTask();

	public void initRuntimeParameters(String applicationLib, String lrServlet);
	
	public String getApplicationLib();
	
	public String getLrServlet();
}
