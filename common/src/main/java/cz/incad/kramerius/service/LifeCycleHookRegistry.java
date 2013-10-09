package cz.incad.kramerius.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.inject.Inject;

public class LifeCycleHookRegistry {

	public static Logger LOGGER = Logger.getLogger(LifeCycleHookRegistry.class.getName());
	
	private List<LifeCycleHook> hooks = new ArrayList<LifeCycleHook>();
	
	@Inject
	public LifeCycleHookRegistry(Set<LifeCycleHook> hooks) {
		for (LifeCycleHook sh : hooks) {
			this.hooks.add(sh);
		}
	}
	
	public void shutdownNotification() {
		for (LifeCycleHook sh : this.hooks) {
			LOGGER.info("shutdown inform :"+sh.getClass().getName());
			sh.shutdownNotification();
		}
	}

	public void startNotification() {
		for (LifeCycleHook sh : this.hooks) {
			LOGGER.info("startup inform :"+sh.getClass().getName());
			sh.startNotification();
		}
	}
}
