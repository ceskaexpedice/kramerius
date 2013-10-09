package cz.incad.kramerius.processes;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

import cz.incad.kramerius.service.LifeCycleHook;

public class SchedulersLifeCycleHook implements LifeCycleHook {
	
	public static final Logger LOGGER = Logger.getLogger(SchedulersLifeCycleHook.class.getName());
	
    @Inject
    ProcessScheduler processScheduler;
    
    @Inject
    GCScheduler gcScheduler;
	
	@Override
	public void shutdownNotification() {
		LOGGER.info("shutting down process schedulers");
		this.gcScheduler.shutdown();
		this.processScheduler.shutdown();

	}

	@Override
	public void startNotification() {
		LOGGER.info("starting process schedulers");
		// find dead process
        this.gcScheduler.scheduleFindGCCandidates();
        // find process to start
        this.processScheduler.scheduleNextTask();
}
	
	
}
