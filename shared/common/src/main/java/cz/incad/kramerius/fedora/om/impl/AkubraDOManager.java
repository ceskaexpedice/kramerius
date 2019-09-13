package cz.incad.kramerius.fedora.om.impl;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.*;
import com.qbizm.kramerius.imp.jaxb.*;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.akubraproject.BlobStore;
import org.akubraproject.fs.FSBlobStore;
import org.akubraproject.map.IdMapper;
import org.akubraproject.map.IdMappingBlobStore;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.fcrepo.server.errors.LowlevelStorageException;
import org.fcrepo.server.errors.ObjectAlreadyInLowlevelStorageException;
import org.fcrepo.server.errors.ObjectNotInLowlevelStorageException;
import org.fcrepo.server.storage.lowlevel.ICheckable;
import org.fcrepo.server.storage.lowlevel.ILowlevelStorage;
import org.fcrepo.server.storage.lowlevel.akubra.AkubraLowlevelStorage;
import org.fcrepo.server.storage.lowlevel.akubra.HashPathIdMapper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AkubraDOManager {
    public static final Logger LOGGER = Logger.getLogger(AkubraDOManager.class.getName());
    private ILowlevelStorage storage;

    private static HazelcastInstance hzInstance;
    private static IMap<String, Integer> pidLocks;
    private static ITopic<String> cacheInvalidator;

    private static Cache<String, DigitalObject> objectCache;
    private static final String DIGITALOBJECT_CACHE_ALIAS = "DigitalObjectCache";

    private static Unmarshaller unmarshaller = null;
    private static Marshaller marshaller = null;

    static {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(DigitalObject.class);

            unmarshaller = jaxbContext.createUnmarshaller();


            //JAXBContext jaxbdatastreamContext = JAXBContext.newInstance(DatastreamType.class);
            marshaller = jaxbContext.createMarshaller();


        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot init JAXB", e);
            throw new RuntimeException(e);
        }
        ClientConfig config = new ClientConfig();
        config.setInstanceName(KConfiguration.getInstance().getConfiguration().getString("hazelcast.instance"));
        GroupConfig groupConfig = config.getGroupConfig();
        groupConfig.setName(KConfiguration.getInstance().getConfiguration().getString("hazelcast.user"));
        hzInstance = HazelcastClient.newHazelcastClient(config);
        pidLocks = hzInstance.getMap("pidlocks");
        cacheInvalidator = hzInstance.getTopic("cacheInvalidator");
        cacheInvalidator.addMessageListener(new MessageListener<String>() {
            @Override
            public void onMessage(Message<String> message) {
                objectCache.remove(message.getMessageObject());
            }
        });
    }

    public AkubraDOManager(KConfiguration configuration, CacheManager cacheManager) throws IOException {
        try {
            this.storage = initLowLevelStorage(configuration);
            objectCache = cacheManager.getCache(DIGITALOBJECT_CACHE_ALIAS, String.class, DigitalObject.class);
            if (objectCache == null) {
                objectCache = cacheManager.createCache(DIGITALOBJECT_CACHE_ALIAS,
                        CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, DigitalObject.class,
                                ResourcePoolsBuilder.heap(3000))
                                .withExpiry(Expirations.timeToLiveExpiration(
                                        Duration.of(configuration.getCacheTimeToLiveExpiration(), TimeUnit.SECONDS))).build());
            }
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    private AkubraLowlevelStorage initLowLevelStorage(KConfiguration configuration) throws Exception {
        BlobStore fsObjectStore = new FSBlobStore(new URI("urn:example.org:fsObjectStore"), new File(configuration.getProperty("objectStore.path")));
        IdMapper fsObjectStoreMapper = new HashPathIdMapper(configuration.getProperty("objectStore.pattern"));
        BlobStore objectStore = new IdMappingBlobStore(new URI("urn:example.org:objectStore"), fsObjectStore, fsObjectStoreMapper);
        BlobStore fsDatastreamStore = new FSBlobStore(new URI("urn:example.org:fsDatastreamStore"), new File(configuration.getProperty("datastreamStore.path")));
        IdMapper fsDatastreamStoreMapper = new HashPathIdMapper(configuration.getProperty("datastreamStore.pattern"));
        BlobStore datastreamStore = new IdMappingBlobStore(new URI("urn:example.org:datastreamStore"), fsDatastreamStore, fsDatastreamStoreMapper);
        AkubraLowlevelStorage retval = new AkubraLowlevelStorage(objectStore, datastreamStore, true, true);
        return retval;
    }


    public DigitalObject readObjectFromStorage(String pid) throws IOException {
        DigitalObject retval = objectCache.get(pid);
        if (retval == null) {
            Object obj = null;
            try {
                InputStream inputStream = this.storage.retrieveObject(pid);
                synchronized (unmarshaller) {
                    obj = unmarshaller.unmarshal(inputStream);
                }
            } catch (ObjectNotInLowlevelStorageException ex) {
                return null;
            } catch (Exception e) {
                throw new IOException(e);
            }
            retval = (DigitalObject) obj;
            objectCache.put(pid, retval);
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
        try {
            return storage.retrieveObject(objectKey);
        } catch (LowlevelStorageException e) {
            throw new IOException(e);
        }
    }

    public void deleteObject(String pid) throws IOException {
        getWriteLock(pid);
        try {
            DigitalObject object = readObjectFromStorage(pid);
            for (DatastreamType datastreamType : object.getDatastream()) {
                removeManagedStream(datastreamType);
            }
            try {
                storage.removeObject(pid);
            } catch (LowlevelStorageException e) {
                LOGGER.warning("Could not remove object from Akubra: " + e);
            }
        } finally {
            invalidateCache(pid);
            releaseWriteLock(pid);
        }
    }

    public void deleteStream(String pid, String streamId) throws IOException {
        getWriteLock(pid);
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
                LOGGER.warning("Could not replace object in Akubra: " + e);
            }
        } finally {
            invalidateCache(pid);
            releaseWriteLock(pid);
        }
    }

    private void removeManagedStream(DatastreamType datastreamType) {
        if ("M".equals(datastreamType.getCONTROLGROUP())) {
            for (DatastreamVersionType datastreamVersionType : datastreamType.getDatastreamVersion()) {
                if ("INTERNAL_ID".equals(datastreamVersionType.getContentLocation().getTYPE())) {
                    try {
                        storage.removeDatastream(datastreamVersionType.getContentLocation().getREF());
                    } catch (LowlevelStorageException e) {
                        LOGGER.warning("Could not remove managed datastream from Akubra: " + e);
                    }
                }
            }
        }
    }

    public void commit(DigitalObject object, String streamId) throws IOException {
        getWriteLock(object.getPID());
        try {
            List<DatastreamType> datastreamList = object.getDatastream();
            Iterator<DatastreamType> iterator = datastreamList.iterator();
            while (iterator.hasNext()) {
                DatastreamType datastreamType = iterator.next();
                if (streamId != null && streamId.equals(datastreamType.getID())) {
                    convertManagedStream(object.getPID(), datastreamType);
                    break;
                } else {
                    convertManagedStream(object.getPID(), datastreamType);
                }
            }
            try {
                setLastModified(object);
                StringWriter stringWriter = new StringWriter();
                synchronized (marshaller) {
                    marshaller.marshal(object, stringWriter);
                }
                addOrReplaceObject(object.getPID(), new ByteArrayInputStream(stringWriter.toString().getBytes("UTF-8")));

            } catch (Exception e) {
                LOGGER.warning("Could not replace object in Akubra: " + e);
            }
        } finally {
            invalidateCache(object.getPID());
            releaseWriteLock(object.getPID());
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

    private void convertManagedStream(String pid, DatastreamType datastreamType) {
        if ("M".equals(datastreamType.getCONTROLGROUP())) {
            for (DatastreamVersionType datastreamVersionType : datastreamType.getDatastreamVersion()) {
                if (datastreamVersionType.getBinaryContent() != null) {
                    try {
                        String ref = pid + "+" + datastreamType.getID() + "+" + datastreamVersionType.getID();
                        addOrReplaceDatastream(ref, new ByteArrayInputStream(datastreamVersionType.getBinaryContent()));
                        datastreamVersionType.setBinaryContent(null);
                        ContentLocationType contentLocationType = new ContentLocationType();
                        contentLocationType.setTYPE("INTERNAL_ID");
                        contentLocationType.setREF(ref);
                        datastreamVersionType.setContentLocation(contentLocationType);
                    } catch (LowlevelStorageException e) {
                        LOGGER.warning("Could not remove managed datastream from Akubra: " + e);
                    }
                }
            }
        }
    }

    private void addOrReplaceObject(String pid, InputStream content) throws LowlevelStorageException {
        if (((ICheckable) storage).objectExists(pid)) {
            storage.replaceObject(pid, content, null);
        } else {
            storage.addObject(pid, content, null);
        }
    }

    private void addOrReplaceDatastream(String pid, InputStream content) throws LowlevelStorageException {
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


    private static void getWriteLock(String pid) {
        if (pid == null) {
            throw new IllegalArgumentException("pid cannot be null");
        }
        pidLocks.lock(pid);
    }

    private static void releaseWriteLock(String pid) {
        pidLocks.unlock(pid);
    }

    private static void invalidateCache(String pid) {
        cacheInvalidator.publish(pid);
    }


    public void shutdown(){
        hzInstance.shutdown();
    }
}
