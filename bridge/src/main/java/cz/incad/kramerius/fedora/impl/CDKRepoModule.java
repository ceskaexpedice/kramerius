package cz.incad.kramerius.fedora.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import cz.incad.kramerius.FedoraAccess;

public class CDKRepoModule extends AbstractModule {

    @Override
    protected void configure() {
        this.bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessProxyAkubraImpl.class).in(Scopes.SINGLETON);
    }
}
