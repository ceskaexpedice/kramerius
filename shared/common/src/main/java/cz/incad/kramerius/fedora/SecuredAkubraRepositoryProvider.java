package cz.incad.kramerius.fedora;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import cz.incad.kramerius.SolrAccess;
import cz.incad.kramerius.security.RightsResolver;
import cz.incad.kramerius.security.SecuredAkubraRepository;
import cz.incad.kramerius.security.SecuredAkubraRepositoryImpl;
import org.ceskaexpedice.akubra.AkubraRepository;

public class SecuredAkubraRepositoryProvider implements Provider<SecuredAkubraRepository> {

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
    public SecuredAkubraRepository get() {
        return new SecuredAkubraRepositoryImpl(akubraRepository, solrAccess, rightsResolver);
    }
}
