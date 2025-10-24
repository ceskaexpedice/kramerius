package cz.incad.kramerius.service.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import cz.incad.kramerius.relation.RelationService;
import cz.incad.kramerius.relation.impl.RelationServiceImpl;
import cz.incad.kramerius.service.SortingService;

public class SortingModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
    }
}
