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
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author Martin Řehánek <rehan at mzk.cz>
 */
public class MockUrlManager implements RepositoryUrlManager {

    @Override
    public URL getAudiostreamRepositoryUrl(AudioStreamId id) throws IOException {
        return new URL("http://iris.mzk.cz/cache/audio/files/124.mp3");
    }

    @Override
    public void close() {
        //nothing
    }
}
