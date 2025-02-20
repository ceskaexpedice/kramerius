package cz.kramerius.searchIndex;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.impl.SolrAccessImplNewIndex;

public class SearchIndexModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SolrAccess.class).annotatedWith(Names.named("new-index")).to(SolrAccessImplNewIndex.class).in(Scopes.SINGLETON);
        bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessAkubraImpl.class).in(Scopes.SINGLETON);
    }
}
