package cz.incad.kramerius.ngwt.server;

import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.impl.GeneratePDFServiceImpl;
import cz.incad.kramerius.processes.database.DefaultConnectionProvider;
import cz.incad.kramerius.security.SecuredFedoraAccessImpl;
import cz.incad.kramerius.security.SecurityAcceptor;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class GwtModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessImpl.class).in(Scopes.SINGLETON);
		bind(KConfiguration.class).toInstance(KConfiguration.getKConfiguration());
		
	}
}
