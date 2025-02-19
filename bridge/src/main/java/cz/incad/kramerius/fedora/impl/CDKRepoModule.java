package cz.incad.kramerius.fedora.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import cz.incad.kramerius.fedora.AkubraRepositoryProvider;
import org.ceskaexpedice.akubra.AkubraRepository;

public class CDKRepoModule extends AbstractModule {

    @Override
    protected void configure() {
        // namapovani fedora access
        // TODO AK_NEW
        //this.bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessProxyAkubraImpl.class).in(Scopes.SINGLETON);
        //bind(KrameriusRepositoryApi.class).to(KrameriusRepositoryApiProxyImpl.class);
        bind(AkubraRepository.class).toProvider(AkubraRepositoryProvider.class).in(Scopes.SINGLETON);
    }
}
