package cz.incad.kramerius.imaging;

import java.sql.Connection;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import cz.incad.kramerius.ConProvider4T;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.MostDesirable;
import cz.incad.kramerius.imaging.impl.FileSystemCacheServiceImpl;
import cz.incad.kramerius.imaging.impl.TileSupportImpl;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.MostDesirableImpl;
import cz.incad.kramerius.processes.DefinitionManager;
import cz.incad.kramerius.processes.LRProcessManager;
import cz.incad.kramerius.processes.impl.DatabaseProcessManager;
import cz.incad.kramerius.processes.impl.LRProcessDefinitionManagerImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;

public class ImagingModuleForTest extends AbstractModule {

    @Override
    protected void configure() {
        KConfiguration testConf = KConfiguration.getInstance();
        bind(KConfiguration.class).toInstance(testConf);
        // "kramerius4"
        bind(Connection.class).annotatedWith(Names.named("kramerius4")).toProvider(ConProvider4T.class);
        bind(FedoraAccess.class).annotatedWith(Names.named("securedFedoraAccess")).to(FedoraAccessImpl.class);

        // long running process modul
        bind(DefinitionManager.class).to(LRProcessDefinitionManagerImpl.class);
        bind(LRProcessManager.class).to(DatabaseProcessManager.class);

        bind(String.class).annotatedWith(Names.named("LIBS")).toInstance("<uknown>");

        bind(MostDesirable.class).to(MostDesirableImpl.class);

        bind(DeepZoomTileSupport.class).to(TileSupportImpl.class);
        bind(DeepZoomCacheService.class).to(FileSystemCacheServiceImpl.class);
    }

}
