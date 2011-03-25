/*
 * Copyright (C) 2010 Pavel Stastny
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.imaging;

/**
 * Enumerates image streams 
 */
public enum ImageStreams {
    
    IMG_FULL("IMG_FULL"), IMG_PREVIEW("IMG_PREVIEW"), IMG_THUMB("IMG_THUMB");

    private String stream;

    private ImageStreams(String stream) {
        this.stream = stream;
    }

    public String getStreamName() {
        return stream;
    }
}
