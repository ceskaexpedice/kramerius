package cz.incad.kramerius.backend.impl;

import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.impl.GeneratePDFServiceImpl;
import cz.incad.kramerius.utils.JNDIUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.utils.IKeys;

public class TestModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(FedoraAccess.class).to(FedoraAccessImpl.class).in(Scopes.SINGLETON);
		//bind(GeneratePDFService.class).to(GeneratePDFServiceImpl.class).in(Scopes.SINGLETON);
		bind(GeneratePDFService.class).to(GeneratePDFServiceImpl.class).in(Scopes.SINGLETON);
		bind(KConfiguration.class).toInstance(KConfiguration.getInstance());
		// konekce.. vymenit za jndi
		//bind(Connection.class).toProvider(DefaultConnectionProvider.class);
	}
}
