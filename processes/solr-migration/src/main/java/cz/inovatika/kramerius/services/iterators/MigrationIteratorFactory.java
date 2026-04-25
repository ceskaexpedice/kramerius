package cz.inovatika.kramerius.services.iterators;

import cz.inovatika.kramerius.services.iterators.config.SolrIteratorConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.util.logging.Logger;

/**
 * Factory class for creating instances of {@link MigrationIterator}.
 * <p>
 * This abstract factory provides a method to instantiate different implementations
 * of {@link MigrationIterator} based on a given class name. It also defines an abstract method
 * for creating an iterator with specific parameters.
 * </p>
 */
public abstract class MigrationIteratorFactory {

    public static final Logger LOGGER = Logger.getLogger(MigrationIteratorFactory.class.getName());

    /**
     * Creates an instance of {@link MigrationIterator} based on the provided parameters.
     * <p>
     * Implementations of this method should return a configured {@link MigrationIterator}
     * based on the provided timestamp, XML element configuration, and Jersey {@link Client}.
     * </p>
     *
     * @param iterator  An XML element containing configuration for the iterator.
     * @param client    A Jersey client used for communication with external services.
     * @return An instance of {@link MigrationIterator}.
     */
    public abstract MigrationIterator createMigrationIterator(SolrIteratorConfig config, CloseableHttpClient client);

    /**
     * Creates an instance of a concrete {@link MigrationIteratorFactory} implementation.
     * <p>
     * This method dynamically loads a factory class by its name and instantiates it.
     * The class must be a valid subclass of {@link MigrationIteratorFactory}.
     * </p>
     *
     * @param instanceName The fully qualified name of the factory class to instantiate.
     * @return An instance of the specified {@link MigrationIteratorFactory}.
     * @throws ClassNotFoundException If the specified class cannot be found.
     * @throws IllegalAccessException If access to the class or its constructor is denied.
     * @throws InstantiationException If the class cannot be instantiated.
     */
    public static MigrationIteratorFactory create(String instanceName) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        LOGGER.info(String.format("Creating factory %s", instanceName));
        Class<MigrationIteratorFactory> aClass = (Class<MigrationIteratorFactory>) Class.forName(instanceName);
        return aClass.newInstance();
    }

    public static MigrationIteratorFactory create(SolrIteratorConfig config) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        return create(config.getFactoryClz());
    }
}
