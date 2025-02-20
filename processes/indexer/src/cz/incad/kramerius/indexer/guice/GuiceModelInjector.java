package cz.incad.kramerius.indexer.guice;

import com.google.inject.*;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;

import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.statistics.StatisticsAccessLog;

public class GuiceModelInjector extends AbstractModule {

    private static Injector _injectorInstance;

    @Override
    protected void configure() {
        //bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessAkubraImpl.class).in(Scopes.SINGLETON);
        //bind(FedoraAccessBridge.class);
        //bind(SolrOperations.class);
       // bind(FedoraOperations.class);


    }

    @Provides
    public StatisticsAccessLog get() {
        return null;
    }

    public synchronized static Injector injector() {
        if (_injectorInstance == null) {
            _injectorInstance = Guice.createInjector(
                    new RepoModule(),
                    new ResourceIndexModule(),
                    new SolrModule(),
                    new GuiceModelInjector(),
                    new ResourceIndexModule(),
                    new RepoModule(),
                    new NullStatisticsModule()
                    //Injector injector = Guice.createInjector(new SolrModule(), new ResourceIndexModule(), new RepoModule(), new NullStatisticsModule(), new ImportModule());
            );
        }
        return _injectorInstance;
    }

    public static void main(String[] args) {
    }

}
