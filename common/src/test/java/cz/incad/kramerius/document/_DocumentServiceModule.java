package cz.incad.kramerius.document;

import java.util.Locale;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.document.impl.DocumentServiceImpl;
import cz.incad.kramerius.service.ResourceBundleService;
import cz.incad.kramerius.utils.conf.KConfiguration;

class _DocumentServiceModule extends AbstractModule {

    private Locale locale;
    private FedoraAccess fedoraAccess;
    private ResourceBundleService resourceBundleService;
    private SolrAccess solrAccess;
    private KConfiguration kConfig;
    
    
    public _DocumentServiceModule(Locale locale, FedoraAccess fedoraAccess, ResourceBundleService resourceBundleService, SolrAccess solrAccess, KConfiguration kConf) {
        super();
        this.locale = locale;
        this.fedoraAccess = fedoraAccess;
        this.resourceBundleService = resourceBundleService;
        this.solrAccess = solrAccess;
        this.kConfig = kConf;
    }

    @Override
    protected void configure() {
        bind(FedoraAccess.class).annotatedWith(Names.named("securedFedoraAccess")).toInstance(this.fedoraAccess);
        bind(SolrAccess.class).toInstance(this.solrAccess);
        bind(ResourceBundleService.class).toInstance(this.resourceBundleService);
        
        bind(DocumentService.class).to(DocumentServiceImpl.class);
        bind(KConfiguration.class).toInstance(this.kConfig);
    }
    
    @Provides
    public Locale getLocale() {
        return this.locale;
    }
}