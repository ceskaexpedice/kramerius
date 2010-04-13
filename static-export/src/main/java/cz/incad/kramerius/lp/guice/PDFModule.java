package cz.incad.kramerius.lp.guice;

import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.impl.GeneratePDFServiceImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;


public class PDFModule extends AbstractModule {

	public static final String KK_PATH = "kk.path";

	@Override
	protected void configure() {
		bind(FedoraAccess.class).to(FedoraAccessImpl.class).in(Scopes.SINGLETON);
		bind(GeneratePDFService.class).to(GeneratePDFServiceImpl.class).in(Scopes.SINGLETON);
		bind(KConfiguration.class).toInstance(KConfiguration.getKConfiguration());
	}
}
