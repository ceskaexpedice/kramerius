package org.kramerius;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import cz.incad.kramerius.relation.RelationService;
import cz.incad.kramerius.relation.impl.RelationServiceImpl;
import cz.incad.kramerius.service.FOXMLAppendLicenseService;
import cz.incad.kramerius.service.SortingService;
import cz.incad.kramerius.service.impl.FOXMLAppendLicenseImpl;

public class ImportModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
        bind(FOXMLAppendLicenseService.class).to(FOXMLAppendLicenseImpl.class).in(Scopes.SINGLETON);
        bind(RelationService.class).to(RelationServiceImpl.class).in(Scopes.SINGLETON);
    }
}
