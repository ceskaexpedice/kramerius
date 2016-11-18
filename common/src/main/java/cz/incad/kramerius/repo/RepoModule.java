package cz.incad.kramerius.repo;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.impl.FCRepo4AccessImpl;
import cz.incad.kramerius.impl.FedoraAccessImpl;
import cz.incad.kramerius.impl.JackRabbitRepoAccessImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;

/**
 * Repo module 
 * @author pstastny
 */
public class RepoModule extends AbstractModule {
    
    public static enum _FedoraEnum {
        standard {
            @Override
            public void binding(RepoModule bm) {
                bm.bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FedoraAccessImpl.class)
                        .in(Scopes.SINGLETON);
            }
        },
        fcrepo4 {
            @Override
            public void binding(RepoModule bm) {
                bm.bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess")).to(FCRepo4AccessImpl.class)
                        .in(Scopes.SINGLETON);
            }
        },
        jackrabbit {
            @Override
            public void binding(RepoModule bm) {
                bm.bind(FedoraAccess.class).annotatedWith(Names.named("rawFedoraAccess"))
                        .to(JackRabbitRepoAccessImpl.class).in(Scopes.SINGLETON);
            }
        };

        public abstract void binding(RepoModule bm);
    }

    @Override
    protected void configure() {

        String underlayingRepo = KConfiguration.getInstance().getConfiguration().getString("fedora.implementation",
                RepoModule._FedoraEnum.standard.name());
        // establish underalying repository
        RepoModule._FedoraEnum.valueOf(underlayingRepo).binding(this);

    }
}
