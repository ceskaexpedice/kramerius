/*
 * Copyright (C) 2016 Pavel Stastny
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

package cz.incad.kramerius.fedora.om;

import org.w3c.dom.Document;

import java.io.InputStream;
import java.util.Date;

/**
 * This interface represents basic repository item;
 * @author pavels
 */
public interface RepositoryObject {

    /**
     * Sets model to underlaying object
     * @param model Model of the object
     * @throws RepositoryException
     */
    public void setModel(String model) throws RepositoryException;

    /**
     * Returns model of the underlaying object
     * @return
     * @throws RepositoryException
     */
    public String getModel() throws RepositoryException;

    /**
     * Get path within repository
     * @return
     */
    public String getPath();


    /**
     * Create new substream
     * @param streamId Stream id
     * @param mimeType Mimetype of the stream
     * @param input Binary content
     * @return
     * @throws RepositoryException
     */
    public RepositoryDatastream createStream(String streamId, String mimeType, InputStream input) throws RepositoryException;

    public RepositoryDatastream createRedirectedStream(String streamId, String url) throws RepositoryException;

    public RepositoryDatastream getStream(String streamId) throws RepositoryException;

    public Date getLastModified() throws RepositoryException;

    public Document getMetadata() throws RepositoryException;
}
