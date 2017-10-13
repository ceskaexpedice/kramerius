package cz.incad.kramerius.fedora;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.fedora.impl.Fedora4AccessImpl;
import cz.incad.kramerius.fedora.impl.FedoraAccessImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Binding repo module
 * @author pstastny
 */
public class RepoModule extends AbstractModule {
    
    @Override
    protected void configure() {

        this.bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(Fedora4AccessImpl.class)
                .in(Scopes.SINGLETON);

    }
}
