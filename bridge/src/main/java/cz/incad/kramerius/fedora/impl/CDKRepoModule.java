package cz.incad.kramerius.fedora.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.repository.KrameriusRepositoryApi;

public class CDKRepoModule extends AbstractModule {

    @Override
    protected void configure() {
        // namapovani fedora access
        this.bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessProxyAkubraImpl.class).in(Scopes.SINGLETON);
        bind(KrameriusRepositoryApi.class).to(KrameriusRepositoryApiProxyImpl.class);
    }
}
