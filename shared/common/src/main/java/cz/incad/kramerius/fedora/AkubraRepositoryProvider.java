package cz.incad.kramerius.fedora;

import com.google.inject.Provider;
import org.ceskaexpedice.akubra.AkubraRepository;
import org.ceskaexpedice.akubra.AkubraRepositoryFactory;
import org.ceskaexpedice.akubra.RepositoryConfiguration;
import org.ceskaexpedice.akubra.core.lock.hazelcast.HazelcastConfiguration;

/**
 * Provides connection to kramerius4 database
 *
 * @author pavels
 */
// TODO AK_NEW
public class AkubraRepositoryProvider implements Provider<AkubraRepository> {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(AkubraRepositoryProvider.class.getName());
    /*
    private static DataSource dataSource = createDataSource();

    private static DataSource createDataSource() {


        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setJdbcUrl(KConfiguration.getInstance().getJdbcUrl());
        ds.setUsername(KConfiguration.getInstance().getJdbcUserName());
        ds.setPassword(KConfiguration.getInstance().getJdbcUserPass());
        ds.setLeakDetectionThreshold(KConfiguration.getInstance().getConfiguration().getInt("jdbcLeakDetectionThreshold"));
        ds.setMaximumPoolSize(KConfiguration.getInstance().getConfiguration().getInt("jdbcMaximumPoolSize"));

        ds.setConnectionTimeout(KConfiguration.getInstance().getConfiguration().getInt("jdbcConnectionTimeout"));

        ds.setValidationTimeout(KConfiguration.getInstance().getConfiguration().getInt("jdbcValidationTimeout",30000));
        ds.setIdleTimeout(KConfiguration.getInstance().getConfiguration().getInt("jdbcIdleTimeout",600000));
        ds.setMaxLifetime(KConfiguration.getInstance().getConfiguration().getInt("jdbcMaxLifetime",1800000));

        int datasourceSocketTimeout = KConfiguration.getInstance().getConfiguration().getInt("datasourceSocketTimeout",30);
        ds.addDataSourceProperty("socketTimeout", datasourceSocketTimeout);
        ds.setKeepaliveTime(120000);

        return ds;
    }*/

    /*
    @Inject
    public AkubraRepositoryProvider() {
        super();
    }

     */

    @Override
    public AkubraRepository get() {
        HazelcastConfiguration hazelcastConfig = new HazelcastConfiguration.Builder()
                .hazelcastInstance("akubrasyncA")
                .hazelcastUser("dev")
                .build();
        String testRepoPath = "c:\\Users\\petr\\.kramerius4\\data\\";

        RepositoryConfiguration config = new RepositoryConfiguration.Builder()
                .processingIndexHost("http://localhost:8983/solr/processing")
                .objectStorePath(testRepoPath + "objectStore")
                .objectStorePattern("##/##")
                .datastreamStorePath(testRepoPath + "datastreamStore")
                .datastreamStorePattern("##/##")
                .cacheTimeToLiveExpiration(60)
                .hazelcastConfiguration(hazelcastConfig)
                .build();
        AkubraRepository akubraRepository = AkubraRepositoryFactory.createRepository(config);
        return akubraRepository;
        /*
        try {
            Connection connection = dataSource.getConnection();
            connection.setTransactionIsolation(KConfiguration.getInstance().getConfiguration().getInt("jdbcDefaultTransactionIsolationLevel",Connection.TRANSACTION_READ_COMMITTED));  //reset the default level (Process Manager sets it to SERIALIZABLE)
            return connection;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalStateException("Cannot get database connection from the pool.", e);
        }

         */
    }

}
