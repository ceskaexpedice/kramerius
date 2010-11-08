package cz.incad.kramerius.backend.impl;

import java.sql.Connection;

import org.easymock.EasyMock;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.impl.GeneratePDFServiceImpl;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.service.TextsService;
import cz.incad.kramerius.utils.JNDIUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.utils.IKeys;

public class ModuleForSearchTests extends AbstractModule {

	@Override
	protected void configure() {
	    bind(FedoraAccess.class).annotatedWith(Names.named("securedFedoraAccess")).toInstance(EasyMock.createMock(FedoraAccess.class));
		bind(KConfiguration.class).toInstance(KConfiguration.getInstance());
	}
}
