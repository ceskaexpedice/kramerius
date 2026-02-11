package cz.incad.kramerius.fedora;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.inject.Provider;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.AkubraRepositoryFactory;
import org.ceskaexpedice.akubra.config.RepositoryConfiguration;
import org.ceskaexpedice.hazelcast.HazelcastConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

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
        String waitTimeS = KConfiguration.getInstance().getConfiguration().getString("hazelcast.waitTime");
        Long waitTime = waitTimeS != null ? Long.parseLong(waitTimeS) : null;
        String leaseTimeS = KConfiguration.getInstance().getConfiguration().getString("hazelcast.leaseTime");
        Long leaseTime = leaseTimeS != null ? Long.parseLong(leaseTimeS) : null;

        // HAZELCAST SERVER ADDRESS
        String env = System.getenv("HAZELCAST_SERVER_ADDRESSES");
        List<String> envAddresses = env != null && !env.isEmpty()
                ? Arrays.asList(env.split(","))
                : new ArrayList<>();

        List<String> address = Lists.transform(KConfiguration.getInstance().getConfiguration().getList("hazelcast.server.addresses", new ArrayList<>()), Functions.toStringFunction());

        HazelcastConfiguration hazelcastConfig = new HazelcastConfiguration.Builder()
                .hazelcastClientConfigFile(hazelcastConfigFileS)
                .hazelcastInstance(hazelcastInstance)
                .setHazelcastServers( env != null && !env.isEmpty() ? envAddresses.toArray(new String[0]) : address.toArray(new String[0]))
                .hazelcastUser(hazelcastUser)
                .waitTimeSecs(waitTime)
                .leaseTimeSecs(leaseTime)
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
