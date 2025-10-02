package cz.incad.kramerius.processes.scheduler;


/**
 * Scheduler for scheduling and starting processes
 * @author pavels
 */
public interface ProcessScheduler {
    
    /**
     * Schedule next task
     */
	public void scheduleNextTask();

	public void init();

	/**
	 * Shutdown scheduler
	 */
	public void shutdown();
	
}
