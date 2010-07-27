package cz.incad.kramerius.processes;


public interface ProcessScheduler {
	
	public void scheduleNextTask();

	public void init(String applicationLib, String lrServlet);
	
	public String getApplicationLib();
	
	public String getLrServlet();
}
