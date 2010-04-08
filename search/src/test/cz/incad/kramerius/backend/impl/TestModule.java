package cz.incad.kramerius.backend.impl;

import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import cz.incad.Kramerius.backend.impl.FedoraAccessImpl;
import cz.incad.Kramerius.backend.pdf.GeneratePDFService;
import cz.incad.Kramerius.backend.pdf.impl.GeneratePDFServiceImpl;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.processes.database.DefaultConnectionProvider;
import cz.incad.kramerius.utils.JNDIUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.utils.IKeys;

public class TestModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(FedoraAccess.class).to(FedoraAccessImpl.class).in(Scopes.SINGLETON);
		//bind(GeneratePDFService.class).to(GeneratePDFServiceImpl.class).in(Scopes.SINGLETON);
		bind(GeneratePDFService.class).to(GeneratePDFServiceImpl.class).in(Scopes.SINGLETON);
		bind(KConfiguration.class).toInstance(KConfiguration.getKConfiguration("web/kk.xml"));
		// konekce.. vymenit za jndi
		bind(Connection.class).toProvider(DefaultConnectionProvider.class);
	}
}
