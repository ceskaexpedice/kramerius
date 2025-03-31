package cz.incad.kramerius.fedora;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import com.google.inject.name.Names;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.SolrAccessImplNewIndex;
import cz.incad.kramerius.security.SecuredAkubraRepository;
import org.ceskaexpedice.akubra.AkubraRepository;

/**
 * Binding repo module
 * @author pstastny
 */
public class RepoModule extends AbstractModule {
    
    @Override
    protected void configure() {
        bind(AkubraRepository.class).toProvider(AkubraRepositoryProvider.class).in(Scopes.SINGLETON);
    }

}
