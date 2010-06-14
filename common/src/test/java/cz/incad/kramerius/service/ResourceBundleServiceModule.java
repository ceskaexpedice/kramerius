package cz.incad.kramerius.service;

import java.sql.Connection;
import java.util.Locale;


import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.impl.GeneratePDFServiceImpl;
import cz.incad.kramerius.processes.database.JNDIConnectionProvider;
import cz.incad.kramerius.security.SecuredFedoraAccessImpl;
import cz.incad.kramerius.security.SecurityAcceptor;
import cz.incad.kramerius.service.impl.DeleteServiceImpl;
import cz.incad.kramerius.service.impl.ExportServiceImpl;
import cz.incad.kramerius.service.impl.PolicyServiceImpl;
import cz.incad.kramerius.service.impl.ResourceBundleServiceImpl;
import cz.incad.kramerius.service.impl.TextsServiceImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ResourceBundleServiceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(String.class).annotatedWith(Names.named("workingDir")).toInstance(System.getProperty("user.dir"));
		bind(ResourceBundleService.class).to(ResourceBundleServiceImpl.class);
	}
}
