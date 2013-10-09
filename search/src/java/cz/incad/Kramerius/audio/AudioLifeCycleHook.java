package cz.incad.Kramerius.audio;

import java.util.logging.Logger;

import com.google.inject.Inject;

import cz.incad.Kramerius.audio.urlMapping.RepositoryUrlManager;
import cz.incad.kramerius.service.LifeCycleHook;

public class AudioLifeCycleHook implements LifeCycleHook {

	public static Logger LOGGER = Logger.getLogger(AudioLifeCycleHook.class.getName());
	
	@Inject
	RepositoryUrlManager repositoryUrlManager;
	
	@Override
	public void shutdownNotification() {
		LOGGER.info("shutting down repositoryUrlManager");
		this.repositoryUrlManager.close();
	}
	

	@Override
	public void startNotification() {
		
	}

}
