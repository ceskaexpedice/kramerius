package cz.inovatika.kramerius.fedora.om.processingindex;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import cz.incad.kramerius.fedora.om.resourceindex.ProcessingIndexFeeder;

/**
 * Created by pstastny on 10/19/2017.
 */
public class ResourceIndexModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IResourceIndex.class).to(SolrResourceIndex.class).in(Scopes.SINGLETON);
        bind(ProcessingIndexFeeder.class);
    }
}
