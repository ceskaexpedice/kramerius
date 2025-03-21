package cz.incad.kramerius.fedora;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredAkubraRepositoryImpl;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.AkubraRepositoryFactory;
import org.ceskaexpedice.akubra.config.HazelcastConfiguration;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;

import java.io.File;

public class SecuredAkubraRepositoryProvider implements Provider<AkubraRepository> {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(SecuredAkubraRepositoryProvider.class.getName());

    private final AkubraRepository akubraRepository;
    private final SolrAccess solrAccess;
    private final RightsResolver rightsResolver;

    @Inject
    public SecuredAkubraRepositoryProvider(AkubraRepository akubraRepository,
                                           @Named("new-index") SolrAccess solrAccess,
                                           RightsResolver rightsResolver) {
        this.akubraRepository = akubraRepository;
        this.solrAccess = solrAccess;
        this.rightsResolver = rightsResolver;
    }

    @Override
    public AkubraRepository get() {
        return new SecuredAkubraRepositoryImpl(akubraRepository, solrAccess, rightsResolver);
    }
}
