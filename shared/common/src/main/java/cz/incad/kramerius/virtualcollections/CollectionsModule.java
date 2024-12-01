package cz.incad.kramerius.virtualcollections;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.EhCacheProvider;
import cz.incad.kramerius.fedora.impl.FedoraAccessAkubraImpl;
//import cz.incad.kramerius.virtualcollections.impl.fedora.FedoraCollectionsManagerImpl;
import org.ehcache.CacheManager;

public class CollectionsModule extends AbstractModule {

    @Override
    protected void configure() {
        //bind(CollectionsManager.class).annotatedWith(Names.named("fedora")).to(FedoraCollectionsManagerImpl.class);
    }

}
