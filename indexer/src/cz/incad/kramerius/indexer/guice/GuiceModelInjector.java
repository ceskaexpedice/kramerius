package cz.incad.kramerius.indexer.guice;

import java.io.ByteArrayInputStream;

import org.w3c.dom.Document;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;

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
        bind(FedoraAccessBridge.class);
        bind(SolrOperations.class);
        bind(FedoraOperations.class);
    }

    @Provides
    public StatisticsAccessLog get() {
        return null;
    }

    public synchronized static Injector injector() {
        if (_injectorInstance == null) {
            _injectorInstance = Guice.createInjector(
                    new RepoModule(), new GuiceModelInjector());
        }
        return _injectorInstance;
    }

}
