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

package cz.incad.feedrepo;

import java.io.InputStream;

/**
 * This interface represents basic repository item;
 * @author pavels
 */
public interface RepositoryObjectAbstraction {
    
    /**
     * Returns wrapped object
     * @return
     */
    public Object getWrappedObject();
    
    /**
     * Sets model to underlaying object
     * @param model Model of the object
     * @throws RepoAbstractionException
     */
    public void setModel(String model) throws RepoAbstractionException;

    /**
     * Returns model of the underlaying object
     * @return
     * @throws RepoAbstractionException
     */
    public String getModel() throws RepoAbstractionException;


    /**
     * Create new substream
     * @param streamId Stream id
     * @param mimeType Mimetype of the stream
     * @param input Binary content
     * @return
     * @throws RepoAbstractionException
     */
    public RepositoryObjectAbstraction createStream(String streamId, String mimeType, InputStream input) throws RepoAbstractionException;
}
