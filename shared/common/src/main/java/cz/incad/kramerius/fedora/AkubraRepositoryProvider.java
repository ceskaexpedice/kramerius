package cz.incad.kramerius.fedora;

import com.google.inject.Provider;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.AkubraRepositoryFactory;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;

import java.io.File;

public class AkubraRepositoryProvider implements Provider<AkubraRepository> {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AkubraRepositoryProvider.class.getName());

    @Override
    public AkubraRepository get() {
        String objectPath = KConfiguration.getInstance().getProperty("objectStore.path");
        String objectPattern = KConfiguration.getInstance().getProperty("objectStore.pattern");
        String datastreamStorePath = KConfiguration.getInstance().getProperty("datastreamStore.path");
        String datastreamStorePattern = KConfiguration.getInstance().getProperty("datastreamStore.pattern");

        String solrProcessingHost = KConfiguration.getInstance().getSolrProcessingHost();

        File hazelcastConfigFile = KConfiguration.getInstance().findConfigFile("hazelcast.clientconfig");
        String hazelcastConfigFileS = (hazelcastConfigFile != null && hazelcastConfigFile.exists()) ? hazelcastConfigFile.getAbsolutePath() : null;
        String hazelcastInstance = KConfiguration.getInstance().getConfiguration().getString("hazelcast.instance");
        String hazelcastUser = KConfiguration.getInstance().getConfiguration().getString("hazelcast.user");

        HazelcastConfiguration hazelcastConfig = new HazelcastConfiguration.Builder()
                .hazelcastClientConfigFile(hazelcastConfigFileS)
                .hazelcastInstance(hazelcastInstance)
                .hazelcastUser(hazelcastUser)
                .build();
        RepositoryConfiguration config = new RepositoryConfiguration.Builder()
                .processingIndexHost(solrProcessingHost)
                .objectStorePath(objectPath)
                .objectStorePattern(objectPattern)
                .datastreamStorePath(datastreamStorePath)
                .datastreamStorePattern(datastreamStorePattern)
                .hazelcastConfiguration(hazelcastConfig)
                .build();
        AkubraRepository akubraRepository = AkubraRepositoryFactory.createRepository(config);
        return akubraRepository;
    }

}
