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

import cz.incad.kramerius.audio.AudioStreamId;

import java.io.IOException;
import java.net.URL;

/**
 * RepositoryUrlManager handles mapping of audio datastreams of objects to URLs
 * in audio repository.
 *
 * @author Martin Řehánek <rehan at mzk.cz>
 */
public interface RepositoryUrlManager {

    /**
     * Returns URL (in audio repository) of given datastream of object. If no
     * such object or it's datastream exists, null is returned.
     *
     * @param id object and audio version identifier
     * @return URL URL to repisotory or null if no such object or datastream is
     * found.
     * @throws IOException
     */
    URL getAudiostreamRepositoryUrl(AudioStreamId id) throws IOException;

    /**
     * Release resources.
     */
    void close();
}
