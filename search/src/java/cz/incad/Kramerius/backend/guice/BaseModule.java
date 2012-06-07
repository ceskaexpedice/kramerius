package cz.incad.Kramerius.backend.guice;

import java.io.File;
import java.sql.Connection;
import java.util.Locale;

import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import cz.incad.kramerius.Constants;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.MostDesirableImpl;
import cz.incad.kramerius.impl.SolrAccessImpl;
import cz.incad.kramerius.pdf.GeneratePDFService;
import cz.incad.kramerius.pdf.impl.GeneratePDFServiceImpl;
import cz.incad.kramerius.processes.GCScheduler;
import cz.incad.kramerius.processes.ProcessScheduler;
import cz.incad.kramerius.processes.database.Fedora3ConnectionProvider;
import cz.incad.kramerius.processes.database.Kramerius4ConnectionProvider;
import cz.incad.kramerius.processes.impl.GCSchedulerImpl;
import cz.incad.kramerius.processes.impl.ProcessSchedulerImpl;
import cz.incad.kramerius.relation.RelationService;
import cz.incad.kramerius.relation.impl.RelationServiceImpl;
import cz.incad.kramerius.security.SecuredFedoraAccessImpl;
import cz.incad.kramerius.service.DeleteService;
import cz.incad.kramerius.service.ExportService;
import cz.incad.kramerius.service.GoogleAnalytics;
import cz.incad.kramerius.service.METSService;
import cz.incad.kramerius.service.PolicyService;
import cz.incad.kramerius.service.XSLService;
import cz.incad.kramerius.service.impl.DeleteServiceImpl;
import cz.incad.kramerius.service.impl.ExportServiceImpl;
import cz.incad.kramerius.service.impl.GoogleAnalyticsImpl;
import cz.incad.kramerius.service.impl.METSServiceImpl;
import cz.incad.kramerius.service.impl.PolicyServiceImpl;
import cz.incad.kramerius.service.impl.XSLServiceImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;
import cz.incad.kramerius.virtualcollections.VirtualCollection;

/**
 * Base kramerius module
 */
public class BaseModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessImpl.class).in(Scopes.SINGLETON);
        bind(FedoraAccess.class).annotatedWith(Names.named("securedFedoraAccess")).to(SecuredFedoraAccessImpl.class).in(Scopes.SINGLETON);

        bind(SolrAccess.class).to(SolrAccessImpl.class).in(Scopes.SINGLETON);

        bind(METSService.class).to(METSServiceImpl.class);
        bind(KConfiguration.class).toInstance(KConfiguration.getInstance());

        bind(Connection.class).annotatedWith(Names.named("kramerius4")).toProvider(Kramerius4ConnectionProvider.class);

        bind(Locale.class).toProvider(LocalesProvider.class);

        bind(ProcessScheduler.class).to(ProcessSchedulerImpl.class).in(Scopes.SINGLETON);
        bind(GCScheduler.class).to(GCSchedulerImpl.class).in(Scopes.SINGLETON);

        bind(DeleteService.class).to(DeleteServiceImpl.class).in(Scopes.SINGLETON);
        bind(ExportService.class).to(ExportServiceImpl.class).in(Scopes.SINGLETON);
        bind(PolicyService.class).to(PolicyServiceImpl.class).in(Scopes.SINGLETON);
        bind(XSLService.class).to(XSLServiceImpl.class).in(Scopes.SINGLETON);


        // TODO: MOVE
        bind(LocalizationContext.class).toProvider(CustomLocalizedContextProvider.class);

        bind(MostDesirable.class).to(MostDesirableImpl.class);

        bind(VirtualCollection.class).toProvider(VirtualCollectionProvider.class);
        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
        bind(GoogleAnalytics.class).to(GoogleAnalyticsImpl.class).in(Scopes.SINGLETON);
        
    }
    
    
    @Provides
    @Named("fontsDir")
    public File getProcessFontsFolder() {
        String dirName = Constants.WORKING_DIR + File.separator + "fonts";
        return new File(dirName);
    }

}
