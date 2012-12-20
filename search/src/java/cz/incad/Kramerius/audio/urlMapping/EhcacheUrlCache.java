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
package cz.incad.Kramerius.audio.urlMapping;

import cz.incad.Kramerius.audio.AudioStreamId;
import java.net.URL;
import java.util.logging.Logger;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
 * Ehcache implementation. Size of cache and other options can be configured in
 * file ehcache.xml.
 *
 * @see http://ehcache.org/
 * @author Martin Řehánek <Martin.Rehanek at mzk.cz>
 */
public class EhcacheUrlCache implements RepositoryUrlCache {

    private static final String CACHE_NAME = "audioRepositoryUrlCache";
    private static final Cache cache;
    private static final Logger LOGGER = Logger.getLogger(RepositoryUrlCache.class.getName());

    static {
        CacheManager.create();
        CacheManager manager = CacheManager.getInstance();
        cache = manager.getCache(CACHE_NAME);
    }

    @Override
    public URL getUrl(AudioStreamId id) {
        Element result = cache.get(id);
        if (result == null) {
            return null;
        } else {
            return (URL) result.getValue();
        }
    }

    @Override
    public void storeUrl(AudioStreamId id, URL url) {
        Element element = new Element(id, url);
        cache.put(element);
    }

    @Override
    public void close() {
        LOGGER.info("shutting down ehcache cache manager");
        CacheManager.getInstance().shutdown();
    }
}
