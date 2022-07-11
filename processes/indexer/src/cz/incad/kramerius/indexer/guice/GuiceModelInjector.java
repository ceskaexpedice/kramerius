package cz.incad.kramerius.indexer.guice;

import java.io.ByteArrayInputStream;

import com.google.inject.*;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.impl.FedoraAccessAkubraImpl;
import cz.incad.kramerius.resourceindex.ResourceIndexModule;
import cz.incad.kramerius.solr.SolrModule;
import cz.incad.kramerius.statistics.NullStatisticsModule;
import org.w3c.dom.Document;

import cz.incad.kramerius.indexer.FedoraOperations;
import cz.incad.kramerius.indexer.SolrOperations;
import cz.incad.kramerius.indexer.fa.FedoraAccessBridge;
import cz.incad.kramerius.fedora.RepoModule;
import cz.incad.kramerius.statistics.StatisticsAccessLog;
import cz.incad.kramerius.utils.XMLUtils;

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
