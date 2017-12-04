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

import java.io.IOException;

/**
 * Deep zoom flag service
 * @author pavels
 *
 */
public interface DeepZoomFlagService {

    
    /**
     * Delete deepzoom flag
     * @param pid PID of object
     * @throws IOException IO error has been occurred
     */
    public void deleteFlagToPID(final String pid) throws IOException;
    
    /**
     * Sets deepzoom flag
     * @param pid PID of object
     * @param tilesUrl Tiles url
     * @throws IOException IO error has been occurred
     */
    public void setFlagToPID(String pid, String tilesUrl) throws IOException;
}
