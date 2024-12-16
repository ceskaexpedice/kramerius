package cz.incad.kramerius.fedora;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import cz.incad.kramerius.fedora.impl.RepositoryAccessImpl;
import cz.incad.kramerius.fedora.utils.EhCacheProvider;
import org.ehcache.CacheManager;

/**
 * Binding repo module
 * @author pstastny
 */
public class RepoModule extends AbstractModule {
    
    @Override
    protected void configure() {
        this.bind(RepositoryAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(RepositoryAccessImpl.class).in(Scopes.SINGLETON);
        this.bind(CacheManager.class).annotatedWith(Names.named("akubraCacheManager")).toProvider(EhCacheProvider.class).in(Scopes.SINGLETON);
    }

}
