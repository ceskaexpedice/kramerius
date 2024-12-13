package cz.incad.kramerius.virtualcollections;

import com.google.inject.AbstractModule;
//import cz.incad.kramerius.virtualcollections.impl.fedora.FedoraCollectionsManagerImpl;


public class CollectionsModule extends AbstractModule {

    @Override
    protected void configure() {
        //bind(CollectionsManager.class).annotatedWith(Names.named("fedora")).to(FedoraCollectionsManagerImpl.class);
    }

}
