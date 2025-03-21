package cz.incad.kramerius.fedora.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import cz.incad.kramerius.fedora.AkubraRepositoryProvider;
import cz.incad.kramerius.fedora.SecuredAkubraRepositoryProvider;
import org.ceskaexpedice.akubra.AkubraRepository;

public class CDKRepoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AkubraRepository.class).toProvider(AkubraRepositoryProvider.class).in(Scopes.SINGLETON);
        bind(AkubraRepository.class)
                .annotatedWith(Names.named("securedAkubraAccess"))
                .toProvider(SecuredAkubraRepositoryProvider.class)
                .in(Scopes.SINGLETON);
    }
}
