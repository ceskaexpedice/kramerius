/*
 * Copyright (C) 2010 Jan Pokorsky
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

package cz.incad.kramerius.editor.server;

import java.io.IOException;

/**
 * Provides access to various remote services without own java interface.
 *
 * @author Jan Pokorsky
 */
public interface RemoteServices {

    /**
     * calls Kramerius reindex service
     * @param pid pid to reindex
     */
    void reindex(String pid);

    /**
     * fetches title form Dublin Core
     * @param pid pid to query
     * @return title
     * @throws IOException service out of order
     */
    String fetchDCName(String pid) throws IOException;
}
