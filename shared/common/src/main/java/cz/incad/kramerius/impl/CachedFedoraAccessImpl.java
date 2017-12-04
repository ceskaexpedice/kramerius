package cz.incad.kramerius.impl;

import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.ProcessSubtreeException;
import cz.incad.kramerius.StreamHeadersObserver;
import cz.incad.kramerius.TreeNodeProcessor;
import cz.incad.kramerius.fedora.om.Repository;
import cz.incad.kramerius.fedora.om.RepositoryException;
import cz.incad.kramerius.utils.FedoraUtils;
import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * CachedFedoraAccessImpl
 *
 * @author Martin Rumanek
 */
public class CachedFedoraAccessImpl  implements FedoraAccess {

    private static Cache<String, Document> xmlscache;
    private static Cache<String, Boolean> existsCache;
    private static Cache<String, Date> lastModifiedCache;


    private static final String XMLS_CACHE_ALIAS = "FedoraXMLSCache";
    private static final String EXISTS_CACHE_ALIAS = "FedoraExistsCache";
    private static final String LAST_MODIFIED_CACHE_ALIAS = "FedoraLastmodifiedCache";


    private FedoraAccess fedoraAccess;

    @Inject
    public CachedFedoraAccessImpl(KConfiguration configuration, @Named("rawFedoraAccess") FedoraAccess fedoraAccess,
                                  CacheManager cacheManager) throws IOException {

        this.fedoraAccess = fedoraAccess;

        xmlscache = cacheManager.getCache(XMLS_CACHE_ALIAS, String.class, Document.class);
        if (xmlscache == null) {
            xmlscache = cacheManager.createCache(XMLS_CACHE_ALIAS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Document.class,
                            ResourcePoolsBuilder.heap(3000).offheap(32, MemoryUnit.MB))
                            .withExpiry(Expirations.timeToLiveExpiration(
                                    Duration.of(configuration.getCacheTimeToLiveExpiration(), TimeUnit.SECONDS))).build());
        }

        existsCache = cacheManager.getCache(EXISTS_CACHE_ALIAS, String.class, Boolean.class);
        if (existsCache == null) {
            existsCache = cacheManager.createCache(EXISTS_CACHE_ALIAS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Boolean.class,
                            ResourcePoolsBuilder.heap(3000).offheap(1, MemoryUnit.MB))
                            .withExpiry(Expirations.timeToLiveExpiration(
                                    Duration.of(configuration.getCacheTimeToLiveExpiration(), TimeUnit.SECONDS))).build());
        }

        lastModifiedCache = cacheManager.getCache(LAST_MODIFIED_CACHE_ALIAS, String.class, Date.class);
        if (lastModifiedCache == null) {
            lastModifiedCache = cacheManager.createCache(LAST_MODIFIED_CACHE_ALIAS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Date.class,
                            ResourcePoolsBuilder.heap(3000).offheap(1, MemoryUnit.MB))
                            .withExpiry(Expirations.timeToLiveExpiration(
                                    Duration.of(configuration.getCacheTimeToLiveExpiration(), TimeUnit.SECONDS))).build());
        }

    }

    private String cacheKey(String pid, String stream) {
        return pid +"/"+stream;
    }

    @Override
    public Document getRelsExt(String pid) throws IOException {
        Document relsExt = xmlscache.get(cacheKey(pid, FedoraUtils.RELS_EXT_STREAM));
        if (relsExt != null) { //xmlscache hit
            return relsExt;
        } else { //xmlscache miss
            relsExt = this.fedoraAccess.getRelsExt(pid);
            xmlscache.put(cacheKey(pid, FedoraUtils.RELS_EXT_STREAM), relsExt);
            return relsExt;
        }
    }

    @Override
    public Document getBiblioMods(String pid) throws IOException {
        Document mods = xmlscache.get(cacheKey(pid, FedoraUtils.BIBLIO_MODS_STREAM));
        if (mods != null) {
            return mods;
        } else {
            mods = this.fedoraAccess.getBiblioMods(pid);
            xmlscache.put(cacheKey(pid, FedoraUtils.BIBLIO_MODS_STREAM), mods);
        }
        return mods;
    }

    @Override
    public Document getDC(String pid) throws IOException {
        Document dc = xmlscache.get(cacheKey(pid, FedoraUtils.DC_STREAM));
        if (dc != null) {
            return dc;
        } else {
            dc = this.fedoraAccess.getDC(pid);
            xmlscache.put(cacheKey(pid, FedoraUtils.DC_STREAM), dc);
        }
        return dc;
    }

    @Override
    public String getKrameriusModelName(Document relsExt) throws IOException {
        return fedoraAccess.getKrameriusModelName(relsExt);
    }

    @Override
    public String getKrameriusModelName(String pid) throws IOException {
        return fedoraAccess.getKrameriusModelName(pid);
    }

    @Override
    public List<String> getModelsOfRel(Document relsExt) {
        return fedoraAccess.getModelsOfRel(relsExt);
    }

    @Override
    public List<String> getModelsOfRel(String pid) throws IOException {
        return fedoraAccess.getModelsOfRel(pid);
    }

    @Override
    public String getDonator(Document relsExt) {
        return fedoraAccess.getDonator(relsExt);
    }

    @Override
    public String getDonator(String pid) throws IOException {
        return fedoraAccess.getDonator(pid);
    }


    @Override
    public String findFirstViewablePid(String pid) throws IOException {
        return fedoraAccess.findFirstViewablePid(pid);
    }

    @Override
    @Deprecated
    public boolean getFirstViewablePath(List<String> pids, List<String> models) throws IOException {
        return fedoraAccess.getFirstViewablePath(pids, models);
    }

    @Override
    @Deprecated
    public List<Element> getPages(String pid, boolean deep) throws IOException {
        return fedoraAccess.getPages(pid, deep);
    }

    @Override
    @Deprecated
    public List<Element> getPages(String pid, Element rootElementOfRelsExt) throws IOException {
        return fedoraAccess.getPages(pid, rootElementOfRelsExt);
    }

    @Override
    public InputStream getSmallThumbnail(String pid) throws IOException {
        return fedoraAccess.getSmallThumbnail(pid);
    }

    @Override
    public Document getSmallThumbnailProfile(String pid) throws IOException {
        return fedoraAccess.getSmallThumbnailProfile(pid);
    }

    @Override
    public String getSmallThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        return fedoraAccess.getSmallThumbnailMimeType(pid);
    }

    @Override
    public boolean isFullthumbnailAvailable(String pid) throws IOException {
        Boolean bool = existsCache.get(cacheKey(pid, FedoraUtils.IMG_PREVIEW_STREAM));
        if (bool != null) {
             return bool.booleanValue();
        } else {
            bool = this.fedoraAccess.isFullthumbnailAvailable(pid);
            existsCache.put(cacheKey(pid, FedoraUtils.IMG_PREVIEW_STREAM), bool);
        }
        return bool;
    }

    @Override
    public InputStream getFullThumbnail(String pid) throws IOException {
        return fedoraAccess.getFullThumbnail(pid);
    }

    @Override
    public String getFullThumbnailMimeType(String pid) throws IOException, XPathExpressionException {
        return fedoraAccess.getFullThumbnailMimeType(pid);
    }

    @Override
    public InputStream getImageFULL(String pid) throws IOException {
        return fedoraAccess.getImageFULL(pid);
    }

    @Override
    public Document getImageFULLProfile(String pid) throws IOException {
        return fedoraAccess.getImageFULLProfile(pid);
    }

    @Override
    public String getImageFULLMimeType(String pid) throws IOException, XPathExpressionException {
        return fedoraAccess.getImageFULLMimeType(pid);
    }

    @Override
    public boolean isImageFULLAvailable(String pid) throws IOException {
        Boolean bool = existsCache.get(cacheKey(pid, FedoraUtils.IMG_FULL_STREAM));
        if (bool != null) {
            return bool.booleanValue();
        } else {
            bool = this.fedoraAccess.isImageFULLAvailable(pid);
            existsCache.put(cacheKey(pid, FedoraUtils.IMG_FULL_STREAM), bool);
        }
        return bool;
    }

    @Override
    public boolean isStreamAvailable(String pid, String streamName) throws IOException {
        Boolean bool = existsCache.get(cacheKey(pid, streamName));
        if (bool != null) {
            return bool.booleanValue();
        } else {
            bool = this.fedoraAccess.isStreamAvailable(pid, streamName);
            existsCache.put(cacheKey(pid, streamName), bool);
        }
        return bool;
    }

    @Override
    public boolean isObjectAvailable(String pid) throws IOException {
        Boolean bool = existsCache.get(pid);
        if (bool != null) {
            return bool.booleanValue();
        } else {
            bool = this.fedoraAccess.isObjectAvailable(pid);
            existsCache.put(pid, bool);
        }
        return bool;
    }

    @Override
    public boolean isContentAccessible(String pid) throws IOException {
        return fedoraAccess.isContentAccessible(pid);
    }

    @Override
    public Repository getInternalAPI() throws RepositoryException {
        // clear caches
        return this.fedoraAccess.getInternalAPI();
    }


    @Override
    public void processSubtree(String pid, TreeNodeProcessor processor) throws ProcessSubtreeException, IOException {
        fedoraAccess.processSubtree(pid, processor);
    }

    @Override
    public Set<String> getPids(String pid) throws IOException {
        return fedoraAccess.getPids(pid);
    }

    @Override
    public InputStream getDataStream(String pid, String datastreamName) throws IOException {
        return fedoraAccess.getDataStream(pid, datastreamName);
    }

    @Override
    public void observeStreamHeaders(String pid, String datastreamName, StreamHeadersObserver streamObserver) throws IOException {
        fedoraAccess.observeStreamHeaders(pid, datastreamName, streamObserver);
    }

    @Override
    public InputStream getDataStreamXml(String pid, String datastreamName) throws IOException {
        return fedoraAccess.getDataStreamXml(pid, datastreamName);
    }

    @Override
    public Document getDataStreamXmlAsDocument(String pid, String datastreamName) throws IOException {
        Document doc = xmlscache.get(cacheKey(pid, datastreamName));
        if (doc != null) {
            return doc;
        } else {
            doc = this.fedoraAccess.getDataStreamXmlAsDocument(pid,datastreamName);
            xmlscache.put(cacheKey(pid, datastreamName), doc);
        }
        return doc;
    }

    @Override
    public String getMimeTypeForStream(String pid, String datastreamName) throws IOException {
        return fedoraAccess.getMimeTypeForStream(pid, datastreamName);
    }

    @Override
    public String getFedoraVersion() throws IOException {
        return fedoraAccess.getFedoraVersion();
    }

    @Override
    public Document getStreamProfile(String pid, String stream) throws IOException {
        return fedoraAccess.getStreamProfile(pid, stream);
    }

    @Override
    public Document getObjectProfile(String pid) throws IOException {
        return fedoraAccess.getObjectProfile(pid);
    }

    @Override
    public InputStream getFedoraDataStreamsList(String pid) throws IOException {
        return fedoraAccess.getFedoraDataStreamsList(pid);
    }

    @Override
    public Document getFedoraDataStreamsListAsDocument(String pid) throws IOException {
        return fedoraAccess.getFedoraDataStreamsListAsDocument(pid);
    }

    @Override
    public Date getStreamLastmodifiedFlag(String pid, String stream) throws IOException {
        Date date = lastModifiedCache.get(cacheKey(pid, stream));
        if (date != null) {
            return date;
        } else {
            date = this.fedoraAccess.getStreamLastmodifiedFlag(pid, stream);
            lastModifiedCache.put(cacheKey(pid, stream), date);
        }
        return date;
    }

    @Override
    public Date getObjectLastmodifiedFlag(String pid) throws IOException {
        Date date = lastModifiedCache.get(pid);
        if (date != null) {
            return date;
        } else {
            date = this.fedoraAccess.getObjectLastmodifiedFlag(pid);
            lastModifiedCache.put(pid, date);
        }
        return date;
    }

    @Override
    public List<Map<String, String>> getStreamsOfObject(String pid) throws IOException {
        return fedoraAccess.getStreamsOfObject(pid);
    }


    @Override
    public InputStream getFoxml(String pid) throws IOException {
        return this.fedoraAccess.getFoxml(pid);
    }

    @Override
    public Repository getTransactionAwareInternalAPI() throws RepositoryException {
        return this.fedoraAccess.getTransactionAwareInternalAPI();
    }


}
