/*
 * Copyright (C) 2012 Martin Řehánek <rehan at mzk.cz>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.audio.urlMapping;


import cz.incad.kramerius.FedoraAccess;
import cz.incad.kramerius.Initializable;
import cz.incad.kramerius.audio.AudioStreamId;
import cz.incad.kramerius.audio.XpathEvaluator;

import cz.incad.kramerius.utils.conf.KConfiguration;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;
import org.w3c.dom.Document;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This implementation gets PID+DS -> URL from Fedora. Urls are present in
 * objects' datastreams as externally referenced datastreams. Manager reads the
 * datastream's xml content, obtains the url and serves it to client. It also
 * caches found mapping by means of Ehcache. 
 *
 *
 * @author Martin Řehánek <Martin.Rehanek at mzk.cz>
 */
public class CachingFedoraUrlManager implements RepositoryUrlManager, Initializable {

    private static final Logger LOGGER = Logger.getLogger(CachingFedoraUrlManager.class.getName());

    private final XPathExpression dsLocation;

    @Inject
    @Named("securedFedoraAccess")
    private FedoraAccess fedoraAccess;

    private final CacheManager cacheManager;

    private Cache<AudioStreamId, URL> cache;

    private static final String CACHE_ALIAS = "audioRepositoryUrlCache";

    @Override
    public final void init() {
        //nothing here, initialization in constructor
    }

    @Inject
    public CachingFedoraUrlManager(CacheManager cacheManager, KConfiguration configuration) throws IOException {
        LOGGER.log(Level.INFO, "initializing {0}", CachingFedoraUrlManager.class.getName());
        this.dsLocation = createDsLocationExpression();
        this.cacheManager = cacheManager;

        cache = cacheManager.getCache(CACHE_ALIAS, AudioStreamId.class, URL.class);
        if (cache == null) {
            cache = cacheManager.createCache(CACHE_ALIAS,
                    CacheConfigurationBuilder.newCacheConfigurationBuilder(AudioStreamId.class, URL.class,
                            ResourcePoolsBuilder.heap(1000).offheap(32, MemoryUnit.MB))
                            .withExpiry(Expirations.timeToLiveExpiration(
                                    Duration.of(configuration.getCacheTimeToLiveExpiration(), TimeUnit.SECONDS))).build());
        }
    }

    private XPathExpression createDsLocationExpression() {
        try {
            XpathEvaluator xpathEvaluator = new XpathEvaluator();
            //return xpathEvaluator.createExpression("//fedora-management:dsLocation");
            return xpathEvaluator.createExpression("//*[local-name()='dsLocation']");
        } catch (XPathExpressionException ex) {
            //should never happen unless someone breaks xpath expressions
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public URL getAudiostreamRepositoryUrl(AudioStreamId id) throws IOException {
        URL urlFromCache = cache.get(id);
        if (urlFromCache != null) { //cache hit
            return urlFromCache;
        } else { //cache miss
            URL urlFromFedora = getUrlFromFedora(id);
            if (urlFromFedora != null) {
                cache.put(id, urlFromFedora);
            }
            return urlFromFedora;
       }
    }

    private URL getUrlFromFedora(AudioStreamId id) throws IOException {
        LOGGER.log(Level.FINE, "getting url for {0}", id);
        try {
            Document datastreamXml = fedoraAccess.getDataStreamXmlAsDocument(id.getPid(), id.getFormat().name());
            URL url = urlFromDatastream(datastreamXml);
            LOGGER.log(Level.FINE, "found url {0} for {1}", new Object[]{url, id});
            return url;
        } catch (SecurityException ex) {
            LOGGER.log(Level.INFO, ex.getMessage());
            return null;
        }
    }

    private URL urlFromDatastream(Document datastreamXml) throws MalformedURLException {
        try {
            String xpathResult = (String) dsLocation.evaluate(datastreamXml, XPathConstants.STRING);
            return new URL(xpathResult);
        } catch (XPathExpressionException ex) {
            //should never happen unless someone breaks xpath expressions
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    //
    @Override
    public void close() {
        LOGGER.log(Level.INFO, "destroying {0}", CachingFedoraUrlManager.class.getName());
        if (cache != null) {
            cacheManager.removeCache(CACHE_ALIAS);
        }
    }
}
