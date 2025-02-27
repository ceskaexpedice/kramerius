package cz.incad.kramerius.fedora.om.impl;

import ca.thoughtwire.lock.DistributedLockService;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.*;
import com.qbizm.kramerius.imp.jaxb.*;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.akubraproject.BlobStore;
import org.akubraproject.fs.FSBlobStore;
import org.akubraproject.map.IdMapper;
import org.akubraproject.map.IdMappingBlobStore;
import org.apache.commons.io.IOUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.fcrepo.server.errors.LowlevelStorageException;
import org.fcrepo.server.errors.ObjectAlreadyInLowlevelStorageException;
import org.fcrepo.server.errors.ObjectNotInLowlevelStorageException;
import org.fcrepo.server.storage.ConnectionPool;
import org.fcrepo.server.storage.lowlevel.DefaultLowlevelStorage;
import org.fcrepo.server.storage.lowlevel.ICheckable;
import org.fcrepo.server.storage.lowlevel.ILowlevelStorage;
import org.fcrepo.server.storage.lowlevel.akubra.AkubraLowlevelStorage;
import org.fcrepo.server.storage.lowlevel.akubra.HashPathIdMapper;
import org.fcrepo.server.utilities.DDLConverter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AkubraDOManager {
    public static final Logger LOGGER = Logger.getLogger(AkubraDOManager.class.getName());
    private KConfiguration configuration = KConfiguration.getInstance();
    private ILowlevelStorage storage;

    private static HazelcastInstance hzInstance;
    //private static IMap<String, Integer> pidLocks;

    private static DistributedLockService lockService ;
    private static ITopic<String> cacheInvalidator;

    private static Cache<String, DigitalObject> objectCache;
    private static final String DIGITALOBJECT_CACHE_ALIAS = "DigitalObjectCache";

    private static BlockingQueue<Unmarshaller> unmarshallerPool = null;
    private static Marshaller marshaller = null;

    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DigitalObject.class);

            int unmarshallerPoolSize = KConfiguration.getInstance().getUnmarshallerPoolSize();
            unmarshallerPool = new LinkedBlockingQueue<>(unmarshallerPoolSize);
            for (int i = 0; i < unmarshallerPoolSize; i++) {
                unmarshallerPool.offer(jaxbContext.createUnmarshaller());
            }

            //JAXBContext jaxbdatastreamContext = JAXBContext.newInstance(DatastreamType.class);
            marshaller = jaxbContext.createMarshaller();


        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot init JAXB", e);
            throw new RuntimeException(e);
        }
        ClientConfig config = null;
        File configFile = KConfiguration.getInstance().findConfigFile("hazelcast.clientconfig");
        if (configFile != null) {
            try (FileInputStream configStream = new FileInputStream(configFile)) {
                config = new XmlClientConfigBuilder(configStream).build();
            } catch (IOException ex) {
                LOGGER.warning("Could not load Hazelcast config file " + configFile + ": " + ex);
            }
        }
        if (config == null) {
            config = new ClientConfig();
            config.setInstanceName(KConfiguration.getInstance().getConfiguration().getString("hazelcast.instance"));
            GroupConfig groupConfig = config.getGroupConfig();
            groupConfig.setName(KConfiguration.getInstance().getConfiguration().getString("hazelcast.user"));
        }
        hzInstance = HazelcastClient.newHazelcastClient(config);
        //pidLocks = hzInstance.getMap("pidlocks");
        lockService = DistributedLockService.newHazelcastLockService(hzInstance);
        cacheInvalidator = hzInstance.getTopic("cacheInvalidator");
        cacheInvalidator.addMessageListener(new MessageListener<String>() {
            @Override
            public void onMessage(Message<String> message) {
                if (objectCache != null && message != null) {
                    objectCache.remove(message.getMessageObject());
                }
            }
        });
    }

    public AkubraDOManager( CacheManager cacheManager) throws IOException {
        try {
            this.storage = initLowLevelStorage();
            if (cacheManager != null) {
                objectCache = cacheManager.getCache(DIGITALOBJECT_CACHE_ALIAS, String.class, DigitalObject.class);
                if (objectCache == null) {
                    objectCache = cacheManager.createCache(DIGITALOBJECT_CACHE_ALIAS,
                            CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, DigitalObject.class,
                                    ResourcePoolsBuilder.heap(3000))
                                    .withExpiry(Expirations.timeToLiveExpiration(
                                            Duration.of(configuration.getCacheTimeToLiveExpiration(), TimeUnit.SECONDS))).build());
                }
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    private ILowlevelStorage initLowLevelStorage() throws Exception {
        if (configuration.getConfiguration().getBoolean("legacyfs", false)) {
            return createDefaultLowLevelStorage();
        } else {
            return createAkubraLowLevelStorage();
        }
    }

    private AkubraLowlevelStorage createAkubraLowLevelStorage() throws Exception {
        BlobStore fsObjectStore = new FSBlobStore(new URI("urn:example.org:fsObjectStore"), new File(configuration.getProperty("objectStore.path")));
        IdMapper fsObjectStoreMapper = new HashPathIdMapper(configuration.getProperty("objectStore.pattern"));
        BlobStore objectStore = new IdMappingBlobStore(new URI("urn:example.org:objectStore"), fsObjectStore, fsObjectStoreMapper);
        BlobStore fsDatastreamStore = new FSBlobStore(new URI("urn:example.org:fsDatastreamStore"), new File(configuration.getProperty("datastreamStore.path")));
        IdMapper fsDatastreamStoreMapper = new HashPathIdMapper(configuration.getProperty("datastreamStore.pattern"));
        BlobStore datastreamStore = new IdMappingBlobStore(new URI("urn:example.org:datastreamStore"), fsDatastreamStore, fsDatastreamStoreMapper);
        AkubraLowlevelStorage retval = new AkubraLowlevelStorage(objectStore, datastreamStore, true, true);
        return retval;
    }

    private DefaultLowlevelStorage createDefaultLowLevelStorage() throws Exception {
        Map<String, Object> conf = new HashMap<>();
        conf.put("path_algorithm", configuration.getProperty("path_algorithm"));
        conf.put("object_store_base", configuration.getProperty("object_store_base"));
        conf.put("datastream_store_base", configuration.getProperty("datastream_store_base"));
        conf.put("path_registry", configuration.getProperty("path_registry"));
        conf.put("file_system", configuration.getProperty("file_system"));
        conf.put("backslashIsEscape", configuration.getProperty("backslash_is_escape"));
        conf.put("connectionPool", createConnectionPool());
        return new DefaultLowlevelStorage(conf);
    }

    private ConnectionPool createConnectionPool() throws Exception {
        return new ConnectionPool(
                configuration.getProperty("legacyfs.jdbcDriverClass"),
                configuration.getProperty("legacyfs.jdbcURL"),
                configuration.getProperty("legacyfs.dbUsername"),
                configuration.getProperty("legacyfs.dbPassword"),
                (DDLConverter) Class.forName(configuration.getProperty("legacyfs.ddlConverter")).newInstance(),
                configuration.getConfiguration().getInt("legacyfs.maxActive"),
                configuration.getConfiguration().getInt("legacyfs.maxIdle"),
                configuration.getConfiguration().getLong("legacyfs.maxWait"),
                configuration.getConfiguration().getInt("legacyfs.minIdle"),
                configuration.getConfiguration().getLong("legacyfs.minEvictableIdleTimeMillis"),
                configuration.getConfiguration().getInt("legacyfs.numTestsPerEvictionRun"),
                configuration.getConfiguration().getLong("legacyfs.timeBetweenEvictionRunsMillis"),
                configuration.getProperty("legacyfs.validationQuery"),
                configuration.getConfiguration().getBoolean("legacyfs.testOnBorrow"),
                configuration.getConfiguration().getBoolean("legacyfs.testOnReturn"),
                configuration.getConfiguration().getBoolean("legacyfs.testWhileIdle"),
                configuration.getConfiguration().getByte("legacyfs.whenExhaustedAction"));
    }


    /**
     * Loads and unmarshalls DigitalObject from Akubra storage, using cache if possible
     * @param pid
     * @return
     * @throws IOException
     */
    public DigitalObject readObjectFromStorage(String pid) throws IOException {
        return readObjectFromStorageOrCache(pid, true);
    }

    /**
     * Loads and unmarshalls fresh copy of DigitalObject from Akubra storage, bypassing the cache
     * Intended for use in FedoraAccess.getFoxml, which resolves internal managed datastreams to base64 binary content
     * @param pid
     * @return
     * @throws IOException
     */
    public DigitalObject readObjectCloneFromStorage(String pid) throws IOException {
        return readObjectFromStorageOrCache(pid, false);
    }

    private DigitalObject readObjectFromStorageOrCache(String pid, boolean useCache) throws IOException {
        DigitalObject retval = useCache ? objectCache.get(pid) : null;
        if (retval == null) {
            Object obj = null;
            Lock lock = getReadLock(pid);
            try (InputStream inputStream = this.storage.retrieveObject(pid);){
                Unmarshaller unmarshaller = unmarshallerPool.take();
                obj = unmarshaller.unmarshal(inputStream);
                unmarshallerPool.offer(unmarshaller);
            } catch (ObjectNotInLowlevelStorageException ex) {
                return null;
            } catch (Exception e) {
                throw new IOException(e);
            } finally {
                lock.unlock();
            }
            retval = (DigitalObject) obj;
            if (useCache) {
                objectCache.put(pid, retval);
            }
        }
        return retval;
    }

    public InputStream retrieveDatastream(String dsKey) throws IOException {
        try {
            return storage.retrieveDatastream(dsKey);
        } catch (LowlevelStorageException e) {
            throw new IOException(e);
        }
    }

    public InputStream retrieveObject(String objectKey) throws IOException {
        Lock lock = getReadLock(objectKey);
        try {
            return storage.retrieveObject(objectKey);
        } catch (LowlevelStorageException e) {
            throw new IOException(e);
        } finally {
            lock.unlock();
        }
    }

    public void deleteObject(String pid, boolean includingManagedDatastreams) throws IOException {
        Lock lock = getWriteLock(pid);
        try {
            DigitalObject object = readObjectFromStorage(pid);
            if(includingManagedDatastreams) {
                for (DatastreamType datastreamType : object.getDatastream()) {
                    removeManagedStream(datastreamType);
                }
            }
            try {
                storage.removeObject(pid);
            } catch (LowlevelStorageException e) {
                LOGGER.severe("Could not remove object from Akubra: " + e);
            }
        } finally {
            invalidateCache(pid);
            lock.unlock();
        }
    }

    public void deleteStream(String pid, String streamId) throws IOException {
        Lock lock = getWriteLock(pid);
        try {
            DigitalObject object = readObjectFromStorage(pid);
            List<DatastreamType> datastreamList = object.getDatastream();
            Iterator<DatastreamType> iterator = datastreamList.iterator();
            while (iterator.hasNext()) {
                DatastreamType datastreamType = iterator.next();
                if (streamId.equals(datastreamType.getID())) {
                    removeManagedStream(datastreamType);
                    iterator.remove();
                    break;
                }
            }
            try {
                setLastModified(object);
                StringWriter stringWriter = new StringWriter();
                synchronized (marshaller) {
                    marshaller.marshal(object, stringWriter);
                }
                addOrReplaceObject(pid, new ByteArrayInputStream(stringWriter.toString().getBytes("UTF-8")));

            } catch (Exception e) {
                LOGGER.severe("Could not replace object in Akubra: " + e+", pid:'"+pid+"'");
            }
        } finally {
            invalidateCache(pid);
            lock.unlock();
        }
    }

    private void removeManagedStream(DatastreamType datastreamType) {
        if ("M".equals(datastreamType.getCONTROLGROUP())) {
            for (DatastreamVersionType datastreamVersionType : datastreamType.getDatastreamVersion()) {
                if ("INTERNAL_ID".equals(datastreamVersionType.getContentLocation().getTYPE())) {
                    try {
                        storage.removeDatastream(datastreamVersionType.getContentLocation().getREF());
                    } catch (LowlevelStorageException e) {
                        LOGGER.severe("Could not remove managed datastream from Akubra: " + e);
                    }
                }
            }
        }
    }

    public void commit(DigitalObject object, String streamId) throws IOException {
        Lock lock = getWriteLock(object.getPID());
        try {
            List<DatastreamType> datastreamList = object.getDatastream();
            Iterator<DatastreamType> iterator = datastreamList.iterator();
            while (iterator.hasNext()) {
                DatastreamType datastream = iterator.next();
                ensureDsVersionCreatedDate(datastream);
                if (streamId != null && streamId.equals(datastream.getID())) {
                    convertManagedStream(object.getPID(), datastream);
                    break;
                } else {
                    convertManagedStream(object.getPID(), datastream);
                }
            }
            try {
                setLastModified(object);
                ensureCreatedDate(object);
                ensureActive(object);
                StringWriter stringWriter = new StringWriter();
                synchronized (marshaller) {
                    marshaller.marshal(object, stringWriter);
                }
                addOrReplaceObject(object.getPID(), new ByteArrayInputStream(stringWriter.toString().getBytes("UTF-8")));

            } catch (Exception e) {
                LOGGER.severe("Could not replace object in Akubra: " + e);
            }
        } finally {
            invalidateCache(object.getPID());
            lock.unlock();
        }
    }

    public InputStream marshallObject(DigitalObject object) {
        try {
            StringWriter stringWriter = new StringWriter();
            synchronized (marshaller) {
                marshaller.marshal(object, stringWriter);
            }
            return new ByteArrayInputStream(stringWriter.toString().getBytes("UTF-8"));
        } catch (Exception e) {
            LOGGER.severe("Could not marshall object: " + e);
            throw new RuntimeException(e);
        }
    }


    private void setLastModified(DigitalObject object) {
        boolean propertyExists = false;
        List<PropertyType> propertyTypeList = object.getObjectProperties().getProperty();
        for (PropertyType propertyType : propertyTypeList) {
            if ("info:fedora/fedora-system:def/view#lastModifiedDate".equals(propertyType.getNAME())) {
                propertyType.setVALUE(AkubraUtils.currentTimeString());
                propertyExists = true;
                break;
            }
        }
        if (!propertyExists) {
            propertyTypeList.add(AkubraUtils.createProperty("info:fedora/fedora-system:def/view#lastModifiedDate", AkubraUtils.currentTimeString()));
        }
    }

    private void ensureCreatedDate(DigitalObject object) {
        boolean propertyExists = false;
        List<PropertyType> propertyTypeList = object.getObjectProperties().getProperty();
        for (PropertyType propertyType : propertyTypeList) {
            if ("info:fedora/fedora-system:def/model#createdDate".equals(propertyType.getNAME())) {
                propertyExists = true;
                break;
            }
        }
        if (!propertyExists) {
            propertyTypeList.add(AkubraUtils.createProperty("info:fedora/fedora-system:def/model#createdDate", AkubraUtils.currentTimeString()));
        }
    }

    private void ensureActive(DigitalObject object) {
        boolean propertyExists = false;
        List<PropertyType> propertyTypeList = object.getObjectProperties().getProperty();
        for (PropertyType propertyType : propertyTypeList) {
            if ("info:fedora/fedora-system:def/model#state".equals(propertyType.getNAME())) {
                propertyExists = true;
                break;
            }
        }
        if (!propertyExists) {
            propertyTypeList.add(AkubraUtils.createProperty("info:fedora/fedora-system:def/model#state", "Active"));
        }
    }

    private void ensureDsVersionCreatedDate(DatastreamType datastream) {
        if (datastream != null) {
            for (DatastreamVersionType datastreamVersion : datastream.getDatastreamVersion()) {
                XMLGregorianCalendar created = datastreamVersion.getCREATED();
                if (created == null) {
                    datastreamVersion.setCREATED(AkubraUtils.getCurrentXMLGregorianCalendar());
                }
            }
        }
    }

    private void convertManagedStream(String pid, DatastreamType datastream) {
        if ("M".equals(datastream.getCONTROLGROUP())) {
            for (DatastreamVersionType datastreamVersion : datastream.getDatastreamVersion()) {
                if (datastreamVersion.getBinaryContent() != null) {
                    try {
                        String ref = pid + "+" + datastream.getID() + "+" + datastreamVersion.getID();
                        addOrReplaceDatastream(ref, new ByteArrayInputStream(datastreamVersion.getBinaryContent()));
                        datastreamVersion.setBinaryContent(null);
                        ContentLocationType contentLocationType = new ContentLocationType();
                        contentLocationType.setTYPE("INTERNAL_ID");
                        contentLocationType.setREF(ref);
                        datastreamVersion.setContentLocation(contentLocationType);
                    } catch (LowlevelStorageException e) {
                        LOGGER.severe("Could not remove managed datastream from Akubra: " + e);
                    }
                }
            }
        }
    }

    public void resolveArchivedDatastreams(DigitalObject object) {
        for (DatastreamType datastreamType : object.getDatastream()) {
            resolveArchiveManagedStream(datastreamType);
        }

    }

    private void resolveArchiveManagedStream(DatastreamType datastream) {
        if ("M".equals(datastream.getCONTROLGROUP())) {
            for (DatastreamVersionType datastreamVersion : datastream.getDatastreamVersion()) {
                try {
                    InputStream stream = retrieveDatastream(datastreamVersion.getContentLocation().getREF());
                    datastreamVersion.setBinaryContent(IOUtils.toByteArray(stream));
                    datastreamVersion.setContentLocation(null);
                } catch (Exception ex) {
                    LOGGER.severe("Could not resolve archive managed datastream: " + ex);
                }
            }
        }
    }

    public void addOrReplaceObject(String pid, InputStream content) throws LowlevelStorageException {
        if (((ICheckable) storage).objectExists(pid)) {
            storage.replaceObject(pid, content, null);
        } else {
            storage.addObject(pid, content, null);
        }
    }

    public void addOrReplaceDatastream(String pid, InputStream content) throws LowlevelStorageException {
        if (storage instanceof AkubraLowlevelStorage) {
            if (((AkubraLowlevelStorage) storage).datastreamExists(pid)) {
                storage.replaceDatastream(pid, content, null);
            } else {
                storage.addDatastream(pid, content, null);
            }
        } else {
            try {
                storage.addDatastream(pid, content, null);
            } catch (ObjectAlreadyInLowlevelStorageException oailse) {
                storage.replaceDatastream(pid, content, null);
            }
        }
    }


    public static Lock getWriteLock(String pid) {
        if (pid == null) {
            throw new IllegalArgumentException("pid cannot be null");
        }
        ReadWriteLock lock = lockService.getReentrantReadWriteLock(pid);
        lock.writeLock().lock();
        return lock.writeLock();
    }

    public static Lock getReadLock(String pid) {
        if (pid == null) {
            throw new IllegalArgumentException("pid cannot be null");
        }
        ReadWriteLock lock = lockService.getReentrantReadWriteLock(pid);
        lock.readLock().lock();
        return lock.readLock();
    }

    private static void invalidateCache(String pid) {
        cacheInvalidator.publish(pid);
    }


    public static void shutdown() {
        if (lockService != null) {
            lockService.shutdown();
        }
        if (hzInstance != null) {
            hzInstance.shutdown();
        }
    }
}
