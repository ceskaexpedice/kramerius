package cz.incad.kramerius.service;

/**
 * Everyone who can be informed about start and shutdown
 * @author pavels
 */
public interface LifeCycleHook {
	
	/**
	 * Shutdown notification
	 */
	public void shutdownNotification();
	
	/**
	 * Start notification
	 */
	public void startNotification();
}


