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

/**
 * Cache for storing urls to Audio Repository by AudioStreamId.
 *
 * @author Martin Řehánek <Martin.Rehanek at mzk.cz>
 */
public interface RepositoryUrlCache {

    /**
     * Returns url to repository by audio stream id.
     *
     * @param id audio stream id (cache key)
     * @return url from cache or null if not found (cached value)
     */
    public URL getUrl(AudioStreamId id);

    /**
     * Stores url to repository by audio stream id.
     *
     * @param id cache key
     * @param url cache value
     */
    public void storeUrl(AudioStreamId id, URL url);

    /**
     * Releases all allocated resources, closes connections etc.
     */
    public void close();
}
