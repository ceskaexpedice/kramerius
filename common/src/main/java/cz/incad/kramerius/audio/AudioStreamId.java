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
package cz.incad.kramerius.audio;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Identifies one of audio datastreams of object in Fedora.
 *
 * @author Martin Řehánek <Martin.Rehanek at mzk.cz>
 */
public class AudioStreamId implements Serializable {

    //presneji je uuid takto: 8-4-4-4-12 (počet malych pismen)
    //pid ale nemusi byt nutne uuid, proto nechavam .*
    //cast za lomitkem odpovida datastreamu (MP3/OGG/WAV)
    static Pattern IMAGE_ID_PATTERN = Pattern.compile("/uuid:.*/[A-Z0-9]*");
    private final String pid;
    private final AudioFormat format;

    public AudioStreamId(String pid, AudioFormat format) {
        this.pid = pid;
        this.format = format;
    }

    /**
     * Creates audio stream id from path info obtained from HttpServletRequest
     *
     * @param pathInfo String in form
     * "/uuid:17646b3a-3b53-4b40-9c97-c711e42ccedb/MP3"
     * @return
     */
    public static AudioStreamId fromPathInfo(String pathInfo) {
        if (pathInfo == null || !IMAGE_ID_PATTERN.matcher(pathInfo).matches()) {
            throw new IllegalArgumentException("invalid resource " + pathInfo);
        }
        String[] parts = pathInfo.split("/");
        return new AudioStreamId(parts[1], AudioFormat.valueOf(parts[2]));
    }

    /**
     *
     * @return Fedora pid of object containing audio datastream
     */
    public String getPid() {
        return pid;
    }

    /**
     *
     * @return Format of audio datastream
     */
    public AudioFormat getFormat() {
        return format;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.pid != null ? this.pid.hashCode() : 0);
        hash = 47 * hash + (this.format != null ? this.format.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AudioStreamId other = (AudioStreamId) obj;
        if ((this.pid == null) ? (other.pid != null) : !this.pid.equals(other.pid)) {
            return false;
        }
        if ((this.format == null) ? (other.format != null) : !this.format.equals(other.format)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ImageId{" + "pid=" + pid + ", version=" + format + '}';
    }
}
