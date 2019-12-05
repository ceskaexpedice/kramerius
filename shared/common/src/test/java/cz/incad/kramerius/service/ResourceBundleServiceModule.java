package cz.incad.kramerius.service;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import cz.incad.kramerius.service.impl.ResourceBundleServiceImpl;

public class ResourceBundleServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(String.class).annotatedWith(Names.named("workingDir")).toInstance(System.getProperty("user.dir"));
		bind(ResourceBundleService.class).to(ResourceBundleServiceImpl.class);
	}
}
