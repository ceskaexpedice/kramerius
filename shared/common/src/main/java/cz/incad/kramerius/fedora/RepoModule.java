package cz.incad.kramerius.fedora;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.impl.FedoraAccessAkubraImpl;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ehcache.CacheManager;

/**
 * Binding repo module
 * @author pstastny
 */
public class RepoModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(AkubraRepository.class).toProvider(AkubraRepositoryProvider.class).in(Scopes.SINGLETON);
        // TODO AK_NEW this.bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessAkubraImpl.class).in(Scopes.SINGLETON);
        // TODO AK_NEW this.bind(CacheManager.class).annotatedWith(Names.named("akubraCacheManager")).toProvider(EhCacheProvider.class).in(Scopes.SINGLETON);
    }

}
